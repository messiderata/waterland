package UserHomePageDirectory.OrderTrackingUtils.Messages;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;

public class MessagesConstructor implements Parcelable {

    private List<Map<String, Object>> chats;

    public MessagesConstructor() {
    }

    public MessagesConstructor(List<Map<String, Object>> chats) {
        this.chats = chats;
    }

    protected MessagesConstructor(Parcel in){
        chats = (List<Map<String, Object>>) in.readSerializable();
    }

    public static final Creator<MessagesConstructor> CREATOR = new Creator<MessagesConstructor>() {
        @Override
        public MessagesConstructor createFromParcel(Parcel in) {
            return new MessagesConstructor(in);
        }

        @Override
        public MessagesConstructor[] newArray(int size) {
            return new MessagesConstructor[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable((Serializable) chats);
    }

    public List<Map<String, Object>> getChats() {
        return chats;
    }

    public void setChats(List<Map<String, Object>> chats) {
        this.chats = chats;
    }
}
