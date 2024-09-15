package UserHomePageDirectory.HomeFragmentUtils.AddressList;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeliveryDetails implements Serializable {
    private String fullName;
    private String phoneNumber;
    private String deliveryAddress;
    private int isDefaultAddress;

    // Constructor
    public DeliveryDetails(String fullName, String phoneNumber, String deliveryAddress, int isDefaultAddress) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.deliveryAddress = deliveryAddress;
        this.isDefaultAddress = isDefaultAddress;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    // Setters (optional, if you need to modify the values later)
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public int getIsDefaultAddress() {
        return isDefaultAddress;
    }

    public void setIsDefaultAddress(int isDefaultAddress) {
        this.isDefaultAddress = isDefaultAddress;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fullName", fullName);
        map.put("phoneNumber", phoneNumber);
        map.put("deliveryAddress", deliveryAddress);
        map.put("isDefaultAddress", isDefaultAddress);
        return map;
    }
}
