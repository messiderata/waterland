package AdminHomePageDirectory.Orders.Utils.CancelOrders;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;

public class CancelledOrdersConstructor implements Parcelable {
    private String accountStatus;
    private String additional_message;
    private String date_delivery;
    private Timestamp date_ordered;
    private Map<String, Object> delivery_address;
    private Map<String, Object> gcash_payment_details;
    private String isPaid;
    private String mode_of_payment;
    private String order_icon;
    private String order_id;
    private List<Map<String, Object>> order_items;
    private String order_status;
    private String search_term;
    private int total_amount;
    private String user_id;
    private String formattedDateOrdered;
    private String cancel_reason;

    public CancelledOrdersConstructor() {
    }

    public CancelledOrdersConstructor(String accountStatus, String additional_message, String date_delivery, Timestamp date_ordered, Map<String, Object> delivery_address, Map<String, Object> gcash_payment_details, String isPaid, String mode_of_payment, String order_icon, String order_id, List<Map<String, Object>> order_items, String order_status, String search_term, int total_amount, String user_id, String cancel_reason) {
        this.accountStatus = accountStatus;
        this.additional_message = additional_message;
        this.date_delivery = date_delivery;
        this.date_ordered = date_ordered;
        this.delivery_address = delivery_address;
        this.gcash_payment_details = gcash_payment_details;
        this.isPaid = isPaid;
        this.mode_of_payment = mode_of_payment;
        this.order_icon = order_icon;
        this.order_id = order_id;
        this.order_items = order_items;
        this.order_status = order_status;
        this.search_term = search_term;
        this.total_amount = total_amount;
        this.user_id = user_id;
        this.cancel_reason = cancel_reason;
    }

    protected CancelledOrdersConstructor(Parcel in) {
        accountStatus = in.readString();
        additional_message = in.readString();
        date_delivery = in.readString();
        date_ordered = in.readParcelable(Timestamp.class.getClassLoader());
        delivery_address = (Map<String, Object>) in.readSerializable();
        gcash_payment_details = (Map<String, Object>) in.readSerializable();
        isPaid = in.readString();
        mode_of_payment = in.readString();
        order_icon = in.readString();
        order_id = in.readString();
        order_items = (List<Map<String, Object>>) in.readSerializable();
        order_status = in.readString();
        search_term = in.readString();
        total_amount = in.readInt();
        user_id = in.readString();
        cancel_reason = in.readString();
    }

    public static final Creator<CancelledOrdersConstructor> CREATOR = new Creator<CancelledOrdersConstructor>() {
        @Override
        public CancelledOrdersConstructor createFromParcel(Parcel in) {
            return new CancelledOrdersConstructor(in);
        }

        @Override
        public CancelledOrdersConstructor[] newArray(int size) {
            return new CancelledOrdersConstructor[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountStatus);
        dest.writeString(additional_message);
        dest.writeString(date_delivery);
        dest.writeParcelable(date_ordered, flags);
        dest.writeSerializable((Serializable) delivery_address);
        dest.writeSerializable((Serializable) gcash_payment_details);
        dest.writeString(isPaid);
        dest.writeString(mode_of_payment);
        dest.writeString(order_icon);
        dest.writeString(order_id);
        dest.writeSerializable((Serializable) order_items);
        dest.writeString(order_status);
        dest.writeString(search_term);
        dest.writeInt(total_amount);
        dest.writeString(user_id);
        dest.writeString(cancel_reason);
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getAdditional_message() {
        return additional_message;
    }

    public void setAdditional_message(String additional_message) {
        this.additional_message = additional_message;
    }

    public String getDate_delivery() {
        return date_delivery;
    }

    public void setDate_delivery(String date_delivery) {
        this.date_delivery = date_delivery;
    }

    public Timestamp getDate_ordered() {
        return date_ordered;
    }

    public void setDate_ordered(Timestamp date_ordered) {
        this.date_ordered = date_ordered;
    }

    public Map<String, Object> getDelivery_address() {
        return delivery_address;
    }

    public void setDelivery_address(Map<String, Object> delivery_address) {
        this.delivery_address = delivery_address;
    }

    public Map<String, Object> getGcash_payment_details() {
        return gcash_payment_details;
    }

    public void setGcash_payment_details(Map<String, Object> gcash_payment_details) {
        this.gcash_payment_details = gcash_payment_details;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    public String getMode_of_payment() {
        return mode_of_payment;
    }

    public void setMode_of_payment(String mode_of_payment) {
        this.mode_of_payment = mode_of_payment;
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

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getSearch_term() {
        return search_term;
    }

    public void setSearch_term(String search_term) {
        this.search_term = search_term;
    }

    public int getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(int total_amount) {
        this.total_amount = total_amount;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFormattedDateOrdered() {
        return formattedDateOrdered;
    }

    public void setFormattedDateOrdered(String formattedDateOrdered) {
        this.formattedDateOrdered = formattedDateOrdered;
    }

    public String getCancel_reason() {
        return cancel_reason;
    }

    public void setCancel_reason(String cancel_reason) {
        this.cancel_reason = cancel_reason;
    }
}
