package customer.service;

import customer.service.dto.ItemListDTO;
import customer.service.dto.ItemListResource;
import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.util.Series;

import java.io.IOException;
import java.util.Map;

/**
 * Manages SquareAPI requests
 *
 * Created by roman rasskazov on 11.06.2015.
 */
public class SquareConsumer {

    private Logger log = Logger.getLogger(getClass());

    private static final String HEADER_RESPONSE_TIME = "X-Response-Time";

    private static final String SQUARE_URL = "https://connect.squareup.com/v1/me/items";
    private static final String ATTRIBUTE_HEADERS = "org.restlet.http.headers";
    private static final String AUTHORIZATION_KEY = "Bearer pKl7Xksj-ZBSH1IVB93bIQ";
    private static final String HEADER_LINK = "Link";

    //restlet client
    private Client client;

    public SquareConsumer(){
        //register Json Response parser
        Engine.getInstance().getRegisteredConverters().add(new JacksonConverter());
        client = new Client(new Context(), Protocol.HTTP);
    }

    /**
     * Gets items list from Square API
     */
    public ItemListDTO listItemsPaginated() throws IOException {
        //initial run with API url
        String link = SQUARE_URL;
        ItemListDTO itemList = new ItemListDTO();
        while (link != null) {
            log.debug("Request url: " + link);

            SquareClientResource resource = prepareClient(link);

            //run request, add response to result list
            ItemListResource itemListResource = resource.wrap(ItemListResource.class);
            itemList.addAll(itemListResource.getItemList());

            //get Link from response header
            link = getLink(resource);
        }
        log.debug("Square returned " + itemList.size() + " items");
        return itemList;
    }

    /**
     * Looks for "Link" header in response, extracts link from it.
     */
    private String getLink(SquareClientResource resource) {
        String link = null;
        Series<Header> responseHeaders = (Series<Header>) resource.getResponseAttributes().get(ATTRIBUTE_HEADERS);
        Map responseMap = responseHeaders.getValuesMap();
        if (responseMap != null){
            String responseTime = (String) responseMap.get(HEADER_RESPONSE_TIME);
            if (responseTime != null) {
                log.debug("Square response time: " + responseTime);
            }
            link = (String) responseMap.get(HEADER_LINK);
            if (link != null) {
                int start = link.indexOf("<");
                int end = link.indexOf(">");
                if (start >= 0 && end >= 0) {
                    link = link.substring(start + 1, end);
                }
            }
        }
        return link;
    }

    /**
     * Prepare ClientResource with required headers
     */
    private SquareClientResource prepareClient(String link) {
        SquareClientResource resource = new SquareClientResource(link);
        //use the same client for all requests
        resource.setNext(client);

        //set authorization header
        Series<Header> headers = (Series<Header>) resource.getRequestAttributes().get(ATTRIBUTE_HEADERS);
        if (headers == null) {
            headers = new Series<Header>(Header.class);
            resource.getRequestAttributes().put(ATTRIBUTE_HEADERS, headers);
        }
        headers.add(HeaderConstants.HEADER_AUTHORIZATION, AUTHORIZATION_KEY);
        headers.add(HeaderConstants.HEADER_ACCEPT, MediaType.APPLICATION_JSON.getName());
        return resource;
    }
}
