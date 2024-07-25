package UserHomePageDirectory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class AddedItems implements Serializable {
    private String userId;
    private Set<String> itemIds;
    private int totalAmount;

    public AddedItems(String userId) {
        this.userId = userId;
        this.itemIds = new HashSet<>();
        this.totalAmount = 0;
    }

    public String getUserId() {
        return userId;
    }

    public Set<String> getItemIds() {
        return itemIds;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void addItem(String itemId, int itemPrice) {
        if (itemIds.add(itemId)) {
            totalAmount += itemPrice;
        }
    }

    public void removeItem(String itemId, int itemPrice) {
        if (itemIds.remove(itemId)) {
            totalAmount -= itemPrice;
        }
    }
}
