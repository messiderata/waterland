package DeliveryHomePageDirectory.onDelivery;

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


public class AdapterOnDeliveryOrders extends RecyclerView.Adapter<AdapterOnDeliveryOrders.OnDeliveryOrdersViewHolder>{

    List<GetOnDeliveryOrders> onDeliveryOrders;
    Context context;

    private static final String TAG = "AdapterOnDeliveryOrders";

    public AdapterOnDeliveryOrders(List<GetOnDeliveryOrders> onDeliveryOrders, Context context) {
        this.onDeliveryOrders = onDeliveryOrders;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterOnDeliveryOrders.OnDeliveryOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_your_deliveries, parent, false);
        return new AdapterOnDeliveryOrders.OnDeliveryOrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterOnDeliveryOrders.OnDeliveryOrdersViewHolder holder, int position) {
        GetOnDeliveryOrders onDelivery_orders = onDeliveryOrders.get(position);
        Log.d(TAG, "->> getCurrent_location: " + onDelivery_orders.getOrder_status());
        Log.d(TAG, "->> getDate_ordered: " + onDelivery_orders.getDate_ordered());
        Log.d(TAG, "->> getDelivery_id: " + onDelivery_orders.getDelivery_id());
        Log.d(TAG, "->> getOrder_icon: " + onDelivery_orders.getOrder_icon());
        Log.d(TAG, "->> getOrder_id: " + onDelivery_orders.getOrder_id());
        Log.d(TAG, "->> getOrder_items: " + onDelivery_orders.getOrder_items());
        Log.d(TAG, "->> getTotal_amount: " + onDelivery_orders.getTotal_amount());
        Log.d(TAG, "->> getUser_address: " + onDelivery_orders.getUser_address());
        Log.d(TAG, "->> getUser_id: " + onDelivery_orders.getUser_id());

        Timestamp timestamp = onDelivery_orders.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date dateOrdered = new Date(dateOrderedMillis);

        String formatOrderID = "Order ID: " + onDelivery_orders.getOrder_id();
        String formatOrderedDate = "Ordered Date: " + dateOrdered;
        String formatUserAddress = "Address: " + onDelivery_orders.getUser_address();
        String formatOrderAmount = "Total Amount: ₱" + onDelivery_orders.getTotal_amount();
        holder.txt_order_id.setText(formatOrderID);
        holder.txt_ordered_date.setText(formatOrderedDate);
        holder.txt_order_address.setText(formatUserAddress);
        holder.txt_order_amount.setText(formatOrderAmount);

        String gsUrl = onDelivery_orders.getOrder_icon();
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
            Intent intent = new Intent(context, DetailsOnDeliveryOrders.class);
            intent.putExtra("date_ordered", dateOrderedMillis);
            intent.putExtra("delivery_id", onDelivery_orders.getDelivery_id());
            intent.putExtra("order_icon", gsUrl);
            intent.putExtra("order_id", onDelivery_orders.getOrder_id());
            intent.putExtra("order_items", (ArrayList<Map<String, Object>>) onDelivery_orders.getOrder_items());
            intent.putExtra("order_status", onDelivery_orders.getOrder_status());
            intent.putExtra("total_amount", onDelivery_orders.getTotal_amount());
            intent.putExtra("user_address", onDelivery_orders.getUser_address());
            intent.putExtra("user_id", onDelivery_orders.getUser_id());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return onDeliveryOrders.size();
    }

    public static class OnDeliveryOrdersViewHolder extends RecyclerView.ViewHolder {
        TextView txt_order_id, txt_ordered_date, txt_order_address, txt_order_amount;
        ImageView imv_order_img;

        public OnDeliveryOrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_order_id = itemView.findViewById(R.id.order_id);
            txt_ordered_date = itemView.findViewById(R.id.ordered_date);
            txt_order_address = itemView.findViewById(R.id.order_address);
            txt_order_amount = itemView.findViewById(R.id.order_amount);
            imv_order_img = itemView.findViewById(R.id.order_img);

        }
    }
}
