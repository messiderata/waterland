package UserHomePageDirectory.AddressList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.waterlanders.R;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ContactViewHolder> {
    private List<Contact> contactList;

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName, mobileNumber, orderAddress;

        public ContactViewHolder(View view) {
            super(view);
            fullName = view.findViewById(R.id.Full_name);
            mobileNumber = view.findViewById(R.id.mobile_number);
            orderAddress = view.findViewById(R.id.order_address);
        }
    }

    public AddressAdapter(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_selection, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.fullName.setText(contact.getFullName());
        holder.mobileNumber.setText(contact.getMobileNumber());
        holder.orderAddress.setText(contact.getAddress());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }
}
