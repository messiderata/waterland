package DeliveryHomePageDirectory;

import android.content.Context;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;


public class PendingOrderAdapter extends RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>{
    List<GetPendingOrder> orderItems;
    Context context;

    private static final String TAG = "GetPendingOrder";

    public PendingOrderAdapter(List<GetPendingOrder> orderItems, Context context) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public PendingOrderAdapter.PendingOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_your_deliveries, parent, false);
        return new PendingOrderAdapter.PendingOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrderAdapter.PendingOrderViewHolder holder, int position) {
        GetPendingOrder order_items = orderItems.get(position);
        Log.d(TAG, "->> getDate_ordered: " + order_items.getDate_ordered());
        Log.d(TAG, "->> getOrder_items: " + order_items.getOrder_items());
        Log.d(TAG, "->> getTotal_amount: " + order_items.getTotal_amount());
        Log.d(TAG, "->> getUser_address: " + order_items.getUser_address());
        Log.d(TAG, "->> getUser_id: " + order_items.getUser_id());
        Log.d(TAG, "->> getOrder_icon: " + order_items.getOrder_icon());
        Log.d(TAG, "->> getOrder_id: " + order_items.getOrder_id());

        holder.txt_order_id.setText(String.valueOf(order_items.getOrder_id()));
        holder.txt_order_address.setText(String.valueOf(order_items.getUser_address()));
        holder.txt_order_amount.setText(String.valueOf(order_items.getTotal_amount()));

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
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class PendingOrderViewHolder extends RecyclerView.ViewHolder {
        TextView txt_order_id, txt_order_address, txt_order_amount;
        ImageView imv_order_img;

        public PendingOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_order_id = itemView.findViewById(R.id.order_id);
            txt_order_address = itemView.findViewById(R.id.order_address);
            txt_order_amount = itemView.findViewById(R.id.order_amount);
            imv_order_img = itemView.findViewById(R.id.order_img);
        }
    }
}
