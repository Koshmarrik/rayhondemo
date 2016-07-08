package customer.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by roman rasskazov on 11.06.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemListDTO extends ArrayList<ItemDTO> {
}
