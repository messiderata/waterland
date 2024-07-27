package DeliveryHomePageDirectory.onDelivery;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class GetOnDeliveryOrders {

    private String current_location;
    private Timestamp date_ordered;
    private String delivery_id;
    private String order_icon;
    private String order_id;
    private List<Map<String, Object>> order_items;
    private int total_amount;
    private String user_address;
    private String user_id;

    public GetOnDeliveryOrders() {
    }

    public GetOnDeliveryOrders(String current_location, Timestamp date_ordered, String delivery_id, String order_icon, String order_id, List<Map<String, Object>> order_items, int total_amount, String user_address, String user_id) {
        this.current_location = current_location;
        this.date_ordered = date_ordered;
        this.delivery_id = delivery_id;
        this.order_icon = order_icon;
        this.order_id = order_id;
        this.order_items = order_items;
        this.total_amount = total_amount;
        this.user_address = user_address;
        this.user_id = user_id;
    }

    public String getCurrent_location() {
        return current_location;
    }

    public void setCurrent_location(String current_location) {
        this.current_location = current_location;
    }

    public Timestamp getDate_ordered() {
        return date_ordered;
    }

    public void setDate_ordered(Timestamp date_ordered) {
        this.date_ordered = date_ordered;
    }

    public String getDelivery_id() {
        return delivery_id;
    }

    public void setDelivery_id(String delivery_id) {
        this.delivery_id = delivery_id;
    }

    public String getOrder_icon() {
        return order_icon;
    }

    public void setOrder_icon(String order_icon) {
        this.order_icon = order_icon;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public List<Map<String, Object>> getOrder_items() {
        return order_items;
    }

    public void setOrder_items(List<Map<String, Object>> order_items) {
        this.order_items = order_items;
    }

    public int getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(int total_amount) {
        this.total_amount = total_amount;
    }

    public String getUser_address() {
        return user_address;
    }

    public void setUser_address(String user_address) {
        this.user_address = user_address;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
