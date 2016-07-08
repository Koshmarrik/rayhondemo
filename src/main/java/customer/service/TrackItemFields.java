package customer.service;

/**
 * Contains name and price of changed item
 *
 * Created by roman rasskazov on 15.06.2015.
 */
public class TrackItemFields {

    private final String name;

    private final String price;

    public TrackItemFields(String name, String price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }
}
