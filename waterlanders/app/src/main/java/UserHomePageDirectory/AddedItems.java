package UserHomePageDirectory;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddedItems implements Serializable {
    private String userId;
    private int totalAmount;
    private List<Map<String, Object>> cartItems;

    private static final String TAG = "AddedItems";

    public AddedItems(String userId) {
        this.userId = userId;
        this.totalAmount = 0;
        this.cartItems = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public int getTotalAmount() {
        totalAmount = 0;

        for (Map<String, Object> item : cartItems) {
            Integer itemTotalPrice = (Integer) item.get("item_total_price");

            if (itemTotalPrice != null) {
                totalAmount += itemTotalPrice;
            }
        }

        return totalAmount;
    }

    public List<Map<String, Object>> getCartItems() {
        return cartItems;
    }

    public boolean isItemInCart(String itemID) {
        for (Map<String, Object> item : cartItems) {
            if (itemID.equals(item.get("item_id"))) {
                return true;
            }
        }
        return false;
    }

    public void addItem(GetItems items, int totalPrice, int itemQuantity) {
        Map<String, Object> added_item = new HashMap<>();
        added_item.put("item_id", items.getItem_id());
        added_item.put("item_img", items.getItem_img());
        added_item.put("item_name", items.getItem_name());
        added_item.put("item_price", items.getItem_price());
        added_item.put("item_order_quantity", itemQuantity);
        added_item.put("item_total_price", totalPrice);

        cartItems.add(added_item);
    }

    public void updateItemQuantity(String itemId, int totalItemPrice, int itemQuantity) {
        for (Map<String, Object> item : cartItems) {
            if (itemId.equals(item.get("item_id"))) {
                item.put("item_order_quantity", itemQuantity);
                item.put("item_total_price", totalItemPrice);
                return;
            }
        }
    }

    public void removeItem(String itemId, int itemPrice, int itemQuantity) {
        Iterator<Map<String, Object>> iterator = cartItems.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> item = iterator.next();
            if (itemId.equals(item.get("item_id"))) {
                if (itemQuantity > 0) {
                    item.put("item_order_quantity", itemQuantity);

                    // Update the item_total_price
                    int currentTotalPrice = (Integer) item.get("item_total_price");
                    item.put("item_total_price", currentTotalPrice - itemPrice);

                    Log.d("CartManager", "Updated Item: " +
                            "ID: " + item.get("item_id") + ", " +
                            "New Quantity: " + itemQuantity + ", " +
                            "New Total Price: " + (currentTotalPrice - itemPrice));
                } else {
                    // Remove the item if quantity is 0
                    iterator.remove();
                    Log.d("CartManager", "Removed Item: ID " + itemId);
                }
                return;
            }
        }
    }

    public String getOrderIcon() {
        String orderIcon = "";

        for (Map<String, Object> item : cartItems) {
            String firstOrderIcon = (String) item.get("item_img");

            if (firstOrderIcon != null) {
                orderIcon = firstOrderIcon;
                return orderIcon;
            }
        }

        return orderIcon;
    }

    public void logCartItems() {
        for (Map<String, Object> item : cartItems) {
            Log.d("CartManager", "Item: " +
                    "ID: " + item.get("item_id") + ", " +
                    "Image: " + item.get("item_img") + ", " +
                    "Name: " + item.get("item_name") + ", " +
                    "Price: " + item.get("item_price") + ", " +
                    "Quantity: " + item.get("item_order_quantity") + ", " +
                    "Total Price: " + item.get("item_total_price"));
        }
    }
}