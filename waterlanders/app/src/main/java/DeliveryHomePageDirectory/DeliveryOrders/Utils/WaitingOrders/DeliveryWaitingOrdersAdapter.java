package DeliveryHomePageDirectory.DeliveryOrders.Utils.WaitingOrders;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.waterlanders.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;


public class DeliveryWaitingOrdersAdapter extends RecyclerView.Adapter<DeliveryWaitingOrdersAdapter.DeliveryWaitingOrdersAdapterViewHolder> {
    Context context;
    List<PendingOrdersConstructor> pendingOrdersConstructorList;

    public DeliveryWaitingOrdersAdapter(Context context, List<PendingOrdersConstructor> pendingOrdersConstructorList) {
        this.context = context;
        this.pendingOrdersConstructorList = pendingOrdersConstructorList;
    }

    public static class DeliveryWaitingOrdersAdapterViewHolder extends RecyclerView.ViewHolder{
        ImageView orderIMG;
        TextView orderID;
        TextView userID;
        TextView orderPrice;
        TextView searchTerm;
        TextView dateOrdered;
        TextView dateDelivery;

        public DeliveryWaitingOrdersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIMG = itemView.findViewById(R.id.order_img);
            orderID = itemView.findViewById(R.id.order_id);
            userID = itemView.findViewById(R.id.user_id);
            orderPrice = itemView.findViewById(R.id.order_price);
            searchTerm = itemView.findViewById(R.id.search_term);
            dateOrdered = itemView.findViewById(R.id.date_ordered);
            dateDelivery = itemView.findViewById(R.id.date_delivery);
        }
    }

    @NonNull
    @Override
    public DeliveryWaitingOrdersAdapter.DeliveryWaitingOrdersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View view = LayoutInflater.from(context).inflate(R.layout.layout_pending_orders_card, parent, false);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_admin_pending_orders_card, parent, false);
        return new DeliveryWaitingOrdersAdapter.DeliveryWaitingOrdersAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return pendingOrdersConstructorList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryWaitingOrdersAdapter.DeliveryWaitingOrdersAdapterViewHolder holder, int position) {
        PendingOrdersConstructor orderCard = pendingOrdersConstructorList.get(position);

        // display the icon
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(orderCard.getOrder_icon());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(context).load(uri).into(holder.orderIMG);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
        });

        // set text values.
        holder.orderID.setText(String.format("ORDER ID: " + orderCard.getOrder_id()));
        holder.userID.setText(String.format("USER ID: " + orderCard.getUser_id()));
        holder.orderPrice.setText(String.format("₱" + orderCard.getTotal_amount()));

        holder.searchTerm.setText(String.format("SEARCH TERM: " + orderCard.getSearch_term()));

        holder.dateOrdered.setText(String.valueOf(orderCard.getFormattedDateOrdered()));
        holder.dateDelivery.setText(String.valueOf(orderCard.getDate_delivery()));

        holder.itemView.setOnClickListener(v -> {
            Intent showCurrentOrderDetailsIntent = new Intent(context, DeliveryWaitingOrdersCurrentOrderDetails.class);
            showCurrentOrderDetailsIntent.putExtra("current_order", orderCard);
            context.startActivity(showCurrentOrderDetailsIntent);
        });
    }

}
