package customer.service;

import customer.Runner;
import customer.service.dto.ItemDTO;
import customer.service.dto.ItemListDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.log4j.Logger;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * Uses SquareConsumer to retrieve data from Square API, parse, processes, stores output data to provided stream
 *
 * Created by roman rasskazov on 11.06.2015.
 */
public class SquareService {

    private static final String COMMA = ",";
    private static final String EQUAL = "=";
    private static final String SPACE = " ";
    private static final String KPV = "kpv ";
    private Logger log = Logger.getLogger(getClass());

    private static final String ITEM_NOT_FOUND = "Item with id = {0} is not found";
    private static final String ITEM_MESSAGE = "Item with {0} id : {1}";
    private static final String NEW_ITEM_MESSAGE = "New item: ";

    private static final String HEADERS = "Item ID,Name,Category,Description,Variant 1 - Name,Variant 1 - Price," +
            "Variant 1 - SKU";
    private static final String UPDATE_PATTERN = "{0} {1} ({2}): {3} -> {4}";
    private static final String DELETED_MESSAGE = "{0} - Deleted";
    private static final String FIELD_NAME = "Name";
    private static final String FIELD_CATEGORY = "Category";
    private static final String FIELD_DESCRIPTION = "Description";
    private static final String FIELD_VARIATION_NAME = "Variation.name";
    private static final String FIELD_VARIATION_PRICE = "Variation.price";
    private static final String FIELD_VARIATION_SKU = "Variation.SKU";

    private static final String HIGHLIGHT_MESSAGE = "* ItemdId {0} has {1}={2}";

    private SquareConsumer consumer = new SquareConsumer();

    private Map<String, Item> itemMap;
    private static SquareService instance;

    private SquareService(){

    }

    public static SquareService getInstance() {
        if (instance == null){
            instance = new SquareService();
        }
        return instance;
    }

    /**
     * This method also removes dublicates
     * @param list
     * @return
     */
    private Map<String,Item> convertToMap(List<ItemDTO> list){
        Map<String, Item> map = new HashMap<String, Item>();
        for (ItemDTO itemDTO : list){
            map.put(itemDTO.getId(), new Item(itemDTO));
        }
        return map;
    }

    /**
     * Convert item to CSV string
     * @throws IOException
     */
    private String printItem(Item item) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.withRecordSeparator(System.lineSeparator());
        CharArrayWriter result = new CharArrayWriter();
        CSVPrinter printer = new CSVPrinter(result, csvFormat);
        List fields = new ArrayList();
        fields.add(item.getId());
        fields.add(item.getName());
        fields.add(item.getCategory());
        fields.add(item.getDescription());
        fields.add(item.getVariationName());
        fields.add(item.getVariationPrice());
        fields.add(item.getVariationSKU());
        printer.printRecord(fields);
        return result.toString();
    }

    /**
     * Request Square Item API, writes output to stream provided
     * @param out stream to write result
     * @param mode
     * @param descriptionMap
     * @throws IOException
     */
    synchronized public void printItems(PrintStream out, int mode, Map<String, String> descriptionMap) throws IOException {
        ItemListDTO itemList = consumer.listItemsPaginated();
        itemMap = convertToMap(itemList);
        log.debug("Found " + itemMap.size() + " unique items");

        out.println(HEADERS);
        log.debug(HEADERS);

        for (Item item : itemMap.values()){
            String csv = printItem(item);
            out.print(csv);
            log.info(csv.substring(0, csv.length() - 1));
            if (mode == Runner.MODE2){
                for (String descriptionFieldName : descriptionMap.keySet()) {
                    String descriptionFieldValue = descriptionMap.get(descriptionFieldName);
                    if (descriptionContainsPair(item.getDescription(), descriptionFieldName,
                            descriptionFieldValue)) {
                        String message = MessageFormat.format(HIGHLIGHT_MESSAGE, item.getId(), descriptionFieldName, descriptionFieldValue);
                        out.println(message);
                        log.info(message);
                    }
                }

            }
        }
        out.println();
    }

    /**
     * If oldString and newString are different, produces Item update message
     * @param changedFields Set to manage already matched keys. Add fieldName here if record changed.
     * @param descriptionFieldName to report matched kvp
     * @param descriptionFieldValue to report matched kvp
     * @param out stream to write message
     * @param fieldName checked value field name
     * @param itemId itemId, used for compose output string
     * @param oldString old String value
     * @param newString new String value
     */
    private void checkStringForChanges(Set<String> changedFields, String descriptionFieldName, String descriptionFieldValue, PrintStream out, String fieldName, String itemId, String oldString, String newString) {
        boolean changed;
        if (oldString == null && newString == null) {
            changed = false;
        } else if (oldString == null || newString == null){
            changed = true;
        } else {
            changed = !newString.equals(oldString);
        }
        if (changed) {
            changedFields.add(fieldName);
            StringBuilder kpv = new StringBuilder();
            if (descriptionFieldName != null){
                kpv.append(KPV);
                kpv.append(descriptionFieldName);
                kpv.append(EQUAL);
                kpv.append(descriptionFieldValue);
                kpv.append(COMMA);
                kpv.append(SPACE);
            }
            String message = MessageFormat.format(UPDATE_PATTERN, kpv.toString(), itemId, fieldName, oldString, newString);
            out.println(message);
            log.info(message);
        }
    }

    /**
     * Checks if item is updated, stores update message to out stream
     *
     * @return if change in Price or Name occured returns TrackItemFields for changed Item, null otherwise
     */
    private TrackItemFields checkItemsforChanges(Item oldItem, Item newItem, PrintStream out) {
        Set<String> changedFields = checkItemsforChangedFields(oldItem, newItem, out, null, null);
        if (changedFields.contains(FIELD_NAME) || changedFields.contains(FIELD_VARIATION_PRICE)){
            return new TrackItemFields(newItem.getName(), newItem.getVariationPrice());
        }
        return null;
    }

    /**
     * Checks if item is updated, stores update message to out stream
     *
     * @return if change in Price or Name occured returns TrackItemFields for changed Item, null otherwise
     */
    private Set<String> checkItemsforChangedFields(Item oldItem, Item newItem, PrintStream out, String descriptionFieldName, String descriptionFieldValue) {
        Set<String> changedFields = new HashSet<String>();
        checkStringForChanges(changedFields, descriptionFieldName, descriptionFieldValue, out, FIELD_NAME, oldItem.getId(), oldItem.getName(), newItem.getName());
        checkStringForChanges(changedFields, descriptionFieldName, descriptionFieldValue, out, FIELD_VARIATION_PRICE, oldItem.getId(), oldItem.getVariationPrice(), newItem.getVariationPrice());
        checkStringForChanges(changedFields, descriptionFieldName, descriptionFieldValue, out, FIELD_CATEGORY, oldItem.getId(), oldItem.getCategory(), newItem.getCategory());
        checkStringForChanges(changedFields, descriptionFieldName, descriptionFieldValue, out, FIELD_DESCRIPTION, oldItem.getId(), oldItem.getDescription(), newItem.getDescription());
        checkStringForChanges(changedFields, descriptionFieldName, descriptionFieldValue, out, FIELD_VARIATION_NAME, oldItem.getId(), oldItem.getVariationName(), newItem.getVariationName());
        checkStringForChanges(changedFields, descriptionFieldName, descriptionFieldValue, out, FIELD_VARIATION_SKU, oldItem.getId(), oldItem.getVariationSKU(), newItem.getVariationSKU());
        return changedFields;
    }

    /**
     * Compare two maps of items, records all changes to out stream
     */
    private void checkItemListforChanges(Map<String, Item> oldItemsMap, Map<String, Item> newItemsMap,
                                         PrintStream out) throws IOException {
        for (Item newItem : newItemsMap.values()){
            Item oldItem = oldItemsMap.get(newItem.getId());
            if (oldItem != null){
                checkItemsforChanges(oldItem, newItem, out);
            } else {
                //item is created
                String csv = printItem(newItem);
                out.println(csv);
                log.info(NEW_ITEM_MESSAGE + csv);
            }
        }
        //check for deleted items
        for (String oldId : oldItemsMap.keySet()){
            if (!newItemsMap.containsKey(oldId)) {
                String message = MessageFormat.format(DELETED_MESSAGE, oldId);
                out.println(message);
                log.info(message);
            }
        }
    }

    /**
     * Compare two maps of items, records all changes to out stream
     *
     * @param descriptionMap if specified, print only items with description containing this fields, values
     */
    private void checkItemListforChanges(Map<String, Item> oldItemsMap, ItemListDTO newItemsList, PrintStream out,
                                         Map<String, String> descriptionMap) throws IOException {
        //set to store already found keys to make not more than one match for each key
        Set<String> alreadyFound = new HashSet<String>();
        for (ItemDTO newItemDTO : newItemsList) {
            if (newItemDTO.getDescription() == null) {
                continue;
            }
            //check new Item for description kvp entries
            for (String descriptionFieldName : descriptionMap.keySet()) {
                String descriptionFieldValue = descriptionMap.get(descriptionFieldName);
                if (!alreadyFound.contains(descriptionFieldName)) {
                    if (descriptionContainsPair(newItemDTO.getDescription(), descriptionFieldName,
                            descriptionFieldValue)) {
                        //new Item contains pair, check old items
                        Item newItem = new Item(newItemDTO);
                        Item oldItem = oldItemsMap.get(newItem.getId());
                        //print changes if description check is not required or description contains the field
                        if (oldItem.getDescription() != null && descriptionContainsPair(oldItem.getDescription(),
                                descriptionFieldName, descriptionFieldValue)) {
                            //if changes in newItem and oldItem found add description Field Name to already Found Set
                            if (!checkItemsforChangedFields(oldItem, newItem, out, descriptionFieldName, descriptionFieldValue).isEmpty()) {
                                alreadyFound.add(descriptionFieldName);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean descriptionContainsPair(String description, String name, String value){
        for (String token : description.split(COMMA)) {
            String[] tokens2 = token.split(EQUAL);
            if (tokens2.length == 2 && tokens2[0].trim().equalsIgnoreCase(name) && tokens2[1].trim().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Request for last item list, compares with old one, put changes in output
     */
    synchronized public void checkChanges(PrintStream out) {
        try {
            ItemListDTO itemList = consumer.listItemsPaginated();
            Map<String, Item> newItemMap = convertToMap(itemList);
            checkItemListforChanges(itemMap, newItemMap, out);
            itemMap = newItemMap;
        } catch (IOException e){
            log.error(e.getMessage(), e);
            out.print("ERROR:" + e.getMessage());
        }
    }

    /**
     * Request for last item list, compares with old one, put changes in output.
     * @param out stream to output changes
     * @param descriptionMap if specified, print only items with description containing this fields, values
     */
    synchronized public void checkChangesWithDescription(PrintStream out, Map<String, String> descriptionMap) {
        try {
            ItemListDTO itemList = consumer.listItemsPaginated();
            checkItemListforChanges(itemMap, itemList, out, descriptionMap);
            itemMap = convertToMap(itemList);
        } catch (IOException e){
            log.error(e.getMessage(), e);
            out.print("ERROR:" + e.getMessage());
        }
    }

    public void getItemAndPrint(PrintStream out, String id) throws IOException {
        Item item = itemMap.get(id);
        if (item != null){
            String csv = printItem(item);
            out.println(MessageFormat.format(ITEM_MESSAGE, id, csv));
        } else {
            String message = MessageFormat.format(ITEM_NOT_FOUND, id);
            out.println(message);
            log.error(message);
        }
    }

}
