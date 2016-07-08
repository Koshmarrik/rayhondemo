package customer.service;

import customer.service.dto.ItemDTO;
import customer.service.dto.PriceMoneyDTO;

/**
 * Decorator class, simplifies access to ItemDTO fields
 *
 * Created by roman rasskazov on 12.06.2015.
 */
public class Item {

    private ItemDTO itemDTO;

    public Item(ItemDTO item){
        itemDTO = item;
    }

    public String getId() {
        return itemDTO.getId();
    }

    public String getName() {
        return itemDTO.getName();
    }

    public String getCategory() {
        return itemDTO.getCategory().getName();
    }

    public String getDescription() {
        return itemDTO.getDescription();
    }

    public String getVariationName() {
        return itemDTO.getVariations() == null || itemDTO.getVariations().isEmpty() ? null : itemDTO.getVariations().get(0).getName();
    }

    public String getVariationPrice() {
        PriceMoneyDTO priceMoneyDTO = itemDTO.getVariations() == null || itemDTO.getVariations().isEmpty() ? null :
                itemDTO.getVariations().get(0).getPrice_money();
        Double price = priceMoneyDTO == null ? null : priceMoneyDTO.getAmount();
        return price == null ? null : price.toString();
    }

    public String getVariationSKU() {
        return itemDTO.getVariations() == null || itemDTO.getVariations().isEmpty() ? null : itemDTO.getVariations().get(0).getSku();
    }

}
