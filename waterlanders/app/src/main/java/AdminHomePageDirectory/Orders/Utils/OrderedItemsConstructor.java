package AdminHomePageDirectory.Orders.Utils;

public class OrderedItemsConstructor {
    private String item_id;
    private String item_img;
    private String item_name;
    private int item_order_quantity;
    private int item_price;
    private int item_total_price;

    public OrderedItemsConstructor() {
    }

    public OrderedItemsConstructor(String item_id, String item_img, String item_name, int item_order_quantity, int item_price, int item_total_price) {
        this.item_id = item_id;
        this.item_img = item_img;
        this.item_name = item_name;
        this.item_order_quantity = item_order_quantity;
        this.item_price = item_price;
        this.item_total_price = item_total_price;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getItem_img() {
        return item_img;
    }

    public void setItem_img(String item_img) {
        this.item_img = item_img;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public int getItem_order_quantity() {
        return item_order_quantity;
    }

    public void setItem_order_quantity(int item_order_quantity) {
        this.item_order_quantity = item_order_quantity;
    }

    public int getItem_price() {
        return item_price;
    }

    public void setItem_price(int item_price) {
        this.item_price = item_price;
    }

    public int getItem_total_price() {
        return item_total_price;
    }

    public void setItem_total_price(int item_total_price) {
        this.item_total_price = item_total_price;
    }
}
