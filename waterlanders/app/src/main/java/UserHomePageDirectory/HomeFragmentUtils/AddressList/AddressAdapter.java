package UserHomePageDirectory.HomeFragmentUtils.AddressList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import UserHomePageDirectory.HomeFragmentUtils.AddedItems;
import UserHomePageDirectory.HomeFragmentUtils.OrderConfirmation;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ContactViewHolder> {
    private List<DeliveryDetails> deliveryDetails;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;
    private AddedItems addedItems;

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName, mobileNumber, orderAddress;

        public ContactViewHolder(View view) {
            super(view);
            fullName = view.findViewById(R.id.Full_name);
            mobileNumber = view.findViewById(R.id.mobile_number);
            orderAddress = view.findViewById(R.id.order_address);
        }
    }

    public AddressAdapter(List<DeliveryDetails> deliveryDetails, Context context, AddedItems addedItems) {
        this.deliveryDetails = deliveryDetails;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.context = context;
        this.addedItems = addedItems;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_selection, parent, false);
        return new ContactViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        DeliveryDetails deliveryDetailsList = deliveryDetails.get(position);
        holder.fullName.setText(deliveryDetailsList.getFullName());
        holder.mobileNumber.setText(deliveryDetailsList.getPhoneNumber());
        holder.orderAddress.setText(deliveryDetailsList.getDeliveryAddress());

        // if the item is clicked then
        // set the setIsDefaultAddress of the item to 1 then
        // set the other items isDefaultAddress to 0
        // then save it to the database
        // then get a new intent then proceed to the next page

        // Set onClickListener for the itemView
        holder.itemView.setOnClickListener(view -> {
            // Update isDefaultAddress for the clicked item and reset others
            for (int i = 0; i < deliveryDetails.size(); i++) {
                if (i == position) {
                    deliveryDetails.get(i).setIsDefaultAddress(1);
                } else {
                    deliveryDetails.get(i).setIsDefaultAddress(0);
                }
            }

            // Save the updated list to Firestore
            saveUpdatedAddressesToFireStore();

            // Start the next activity (OrderConfirmation)
            Intent intent = new Intent(context, OrderConfirmation.class);
            intent.putExtra("addedItems", addedItems);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return deliveryDetails.size();
    }

    private void saveUpdatedAddressesToFireStore() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            db.collection("users").document(userId)
                .update("deliveryDetails", deliveryDetails)
                .addOnSuccessListener(aVoid -> {
                    // Log success or show a message
                    Log.d("AddressSelection", "Default address updated successfully.");
                    Toast.makeText(context, "Default address updated successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Log.d("AddressSelection", "Error: ", e);
                    Toast.makeText(context, "There was an error updating your address. Error: "+ e, Toast.LENGTH_SHORT).show();
                });
        }
    }
}
