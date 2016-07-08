package customer.service.dto;

import org.restlet.resource.Get;

/**
 * Used to parse Square output as JSON by Restlet Client
 *
 * Created by roman rasskazov on 11.06.2015.
 */

public interface ItemListResource {
    @Get("json")
    ItemListDTO getItemList();
}