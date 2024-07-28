package DeliveryHomePageDirectory.deliveredOrders;

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



public class AdapterDeliveredOrders extends RecyclerView.Adapter<AdapterDeliveredOrders.DeliveredOrdersViewHolder>{
    List<GetDeliveredOrders> deliveredOrders;
    Context context;

    private static final String TAG = "AdapterDeliveredOrders";

    public AdapterDeliveredOrders(List<GetDeliveredOrders> deliveredOrders, Context context) {
        this.deliveredOrders = deliveredOrders;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterDeliveredOrders.DeliveredOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_your_deliveries, parent, false);
        return new AdapterDeliveredOrders.DeliveredOrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDeliveredOrders.DeliveredOrdersViewHolder holder, int position) {
        GetDeliveredOrders delivered_orders = deliveredOrders.get(position);
        Log.d(TAG, "->> getDate_delivered: " + delivered_orders.getDate_delivered());
        Log.d(TAG, "->> getDate_ordered: " + delivered_orders.getDate_ordered());
        Log.d(TAG, "->> getDelivery_id: " + delivered_orders.getDelivery_id());
        Log.d(TAG, "->> getOrder_icon: " + delivered_orders.getOrder_icon());
        Log.d(TAG, "->> getOrder_id: " + delivered_orders.getOrder_id());
        Log.d(TAG, "->> getOrder_items: " + delivered_orders.getOrder_items());
        Log.d(TAG, "->> getOrder_status: " + delivered_orders.getOrder_status());
        Log.d(TAG, "->> getTotal_amount: " + delivered_orders.getTotal_amount());
        Log.d(TAG, "->> getUser_address: " + delivered_orders.getUser_address());
        Log.d(TAG, "->> getUser_confirmation: " + delivered_orders.getUser_confirmation());
        Log.d(TAG, "->> getUser_id: " + delivered_orders.getUser_id());

        Timestamp timestamp = delivered_orders.getDate_ordered();
        long dateOrderedMillis = timestamp.toDate().getTime();
        Date dateOrdered = new Date(dateOrderedMillis);

        String formatOrderID = "Order ID: " + delivered_orders.getOrder_id();
        String formatOrderedDate = "Ordered Date: " + dateOrdered;
        String formatUserAddress = "Address: " + delivered_orders.getUser_address();
        String formatOrderAmount = "Total Amount: ₱" + delivered_orders.getTotal_amount();
        holder.txt_order_id.setText(formatOrderID);
        holder.txt_ordered_date.setText(formatOrderedDate);
        holder.txt_order_address.setText(formatUserAddress);
        holder.txt_order_amount.setText(formatOrderAmount);

        String gsUrl = delivered_orders.getOrder_icon();
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
            Timestamp deliveredTimestamp = delivered_orders.getDate_delivered();
            long dateDeliveredMillis = deliveredTimestamp.toDate().getTime();

            Intent intent = new Intent(context, DetailsDeliveredOrders.class);
            intent.putExtra("date_delivered", dateDeliveredMillis);
            intent.putExtra("date_ordered", dateOrderedMillis);
            intent.putExtra("delivery_id", delivered_orders.getDelivery_id());
            intent.putExtra("order_icon", gsUrl);
            intent.putExtra("order_id", delivered_orders.getOrder_id());
            intent.putExtra("order_items", (ArrayList<Map<String, Object>>) delivered_orders.getOrder_items());
            intent.putExtra("order_status", delivered_orders.getOrder_status());
            intent.putExtra("total_amount", delivered_orders.getTotal_amount());
            intent.putExtra("user_address", delivered_orders.getUser_address());
            intent.putExtra("user_confirmation", delivered_orders.getUser_confirmation());
            intent.putExtra("user_id", delivered_orders.getUser_id());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return deliveredOrders.size();
    }

    public static class DeliveredOrdersViewHolder extends RecyclerView.ViewHolder{
        TextView txt_order_id, txt_ordered_date, txt_order_address, txt_order_amount;
        ImageView imv_order_img;

        public DeliveredOrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_order_id = itemView.findViewById(R.id.order_id);
            txt_ordered_date = itemView.findViewById(R.id.ordered_date);
            txt_order_address = itemView.findViewById(R.id.order_address);
            txt_order_amount = itemView.findViewById(R.id.order_amount);
            imv_order_img = itemView.findViewById(R.id.order_img);
        }
    }
}
