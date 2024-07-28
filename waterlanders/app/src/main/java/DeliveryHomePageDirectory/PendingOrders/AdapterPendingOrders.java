package DeliveryHomePageDirectory.PendingOrders;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class AdapterPendingOrders extends RecyclerView.Adapter<AdapterPendingOrders.PendingOrderViewHolder>{
    List<GetPendingOrder> orderItems;
    Context context;

    private static final String TAG = "GetPendingOrder";

    public AdapterPendingOrders(List<GetPendingOrder> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterPendingOrders.PendingOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_your_deliveries, parent, false);
        return new AdapterPendingOrders.PendingOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPendingOrders.PendingOrderViewHolder holder, int position) {
        GetPendingOrder order_items = orderItems.get(position);
        Log.d(TAG, "->> getDate_ordered: " + order_items.getDate_ordered());
        Log.d(TAG, "->> getOrder_icon: " + order_items.getOrder_icon());
        Log.d(TAG, "->> getOrder_id: " + order_items.getOrder_id());
        Log.d(TAG, "->> getOrder_items: " + order_items.getOrder_items());
        Log.d(TAG, "->> getTotal_amount: " + order_items.getTotal_amount());
        Log.d(TAG, "->> getUser_address: " + order_items.getUser_address());
        Log.d(TAG, "->> getUser_id: " + order_items.getUser_id());

        Timestamp timestamp = order_items.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date dateOrdered = new Date(dateOrderedMillis);
        Log.d(TAG, "->> dateOrdered: " + dateOrdered);

        String formatOrderID = "Order ID: " + order_items.getOrder_id();
        String formatOrderedDate = "Ordered Date: " + dateOrdered;
        String formatUserAddress = "Address: " + order_items.getUser_address();
        String formatOrderAmount = "Total Amount: ₱" + order_items.getTotal_amount();
        holder.txt_order_id.setText(formatOrderID);
        holder.txt_ordered_date.setText(formatOrderedDate);
        holder.txt_order_address.setText(formatUserAddress);
        holder.txt_order_amount.setText(formatOrderAmount);

        String gsUrl = order_items.getOrder_icon();
        if (gsUrl != null && !gsUrl.isEmpty()) {
            // Convert gs:// URL to https:// URL
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(gsUrl);
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d("GLIDE", "Download URL: " + uri.toString());
                Glide.with(context)
                        .load(uri.toString())
                        .override(60, 60)
                        .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                        .error(R.drawable.ic_launcher_background) // Error image
                        .into(holder.imv_order_img);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("GLIDE", "Error getting download URL", exception);
                holder.imv_order_img.setImageResource(R.drawable.ic_launcher_background);
            });
        } else {
            // Handle case where item_img is null or empty
            holder.imv_order_img.setImageResource(R.drawable.ic_launcher_background);
        }

        // Set click listener for item view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsPendingOrders.class);
            intent.putExtra("date_ordered", dateOrderedMillis);
            intent.putExtra("order_icon", gsUrl);
            intent.putExtra("order_id", order_items.getOrder_id());
            intent.putExtra("order_items", (ArrayList<Map<String, Object>>) order_items.getOrder_items());
            intent.putExtra("total_amount", order_items.getTotal_amount());
            intent.putExtra("user_address", order_items.getUser_address());
            intent.putExtra("user_id", order_items.getUser_id());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class PendingOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txt_order_id, txt_ordered_date, txt_order_address, txt_order_amount;
        ImageView imv_order_img;

        public PendingOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_order_id = itemView.findViewById(R.id.order_id);
            txt_ordered_date = itemView.findViewById(R.id.ordered_date);
            txt_order_address = itemView.findViewById(R.id.order_address);
            txt_order_amount = itemView.findViewById(R.id.order_amount);
            imv_order_img = itemView.findViewById(R.id.order_img);
        }
    }
}
