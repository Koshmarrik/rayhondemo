package customer.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by roman rasskazov on 11.06.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariationDTO {

    private String name;

    private PriceMoneyDTO price_money;

    private String sku;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PriceMoneyDTO getPrice_money() {
        return price_money;
    }

    public void setPrice_money(PriceMoneyDTO price_money) {
        this.price_money = price_money;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
