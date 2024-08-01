package UserHomePageDirectory;

import android.util.Log;

public class GetItems {

    String item_name;
    int item_price;
    String item_img;
    String item_id;

    public GetItems() {
    }

    public GetItems(String item_name, int item_price, String item_img) {
        this.item_name = item_name;
        this.item_price = item_price;
        this.item_img = item_img;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public int getItem_price() {
        return item_price;
    }

    public void setItem_price(int item_price) {
        this.item_price = item_price;
    }

    public String getItem_img() {
        return item_img;
    }

    public void setItem_img(String item_img) {
        this.item_img = item_img;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

}
