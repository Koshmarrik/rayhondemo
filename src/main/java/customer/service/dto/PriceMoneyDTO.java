package customer.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by roman rasskazov on 11.06.2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceMoneyDTO {

    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
