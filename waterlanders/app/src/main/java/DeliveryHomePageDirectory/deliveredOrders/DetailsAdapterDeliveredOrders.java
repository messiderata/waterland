package DeliveryHomePageDirectory.deliveredOrders;

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

import java.util.ArrayList;
import java.util.Map;


public class DetailsAdapterDeliveredOrders extends RecyclerView.Adapter<DetailsAdapterDeliveredOrders.DeliveredOrdersDetailsViewHolder>{
    private final ArrayList<Map<String, Object>> orderItems;
    private final Context context;

    public DetailsAdapterDeliveredOrders(Context context, ArrayList<Map<String, Object>> orderItems) {
        this.orderItems = orderItems;
        this.context = context;
    }

    @NonNull
    @Override
    public DetailsAdapterDeliveredOrders.DeliveredOrdersDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_pending_order_details_card, parent, false);
        return new DetailsAdapterDeliveredOrders.DeliveredOrdersDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailsAdapterDeliveredOrders.DeliveredOrdersDetailsViewHolder holder, int position) {
        Map<String, Object> orderItem = orderItems.get(position);
        String itemName = (String) orderItem.get("item_name");
        String itemIcon = (String) orderItem.get("item_img");
        String itemID = (String) orderItem.get("item_id");
        String itemPrice = String.valueOf(orderItem.get("item_price"));

        holder.txt_item_name.setText("Item Name: " + itemName);
        holder.txt_item_price.setText("Price: ₱" + itemPrice);
        holder.txt_item_id.setText("Item ID: " + itemID);

        if (itemIcon != null && !itemIcon.isEmpty()) {
            // Convert gs:// URL to https:// URL
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(itemIcon);
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Log.d("GLIDE", "Download URL: " + uri.toString());
                Glide.with(context)
                        .load(uri.toString())
                        .override(60, 60)
                        .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                        .error(R.drawable.ic_launcher_background) // Error image
                        .into(holder.imv_item_img);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("GLIDE", "Error getting download URL", exception);
                holder.imv_item_img.setImageResource(R.drawable.ic_launcher_background);
            });
        } else {
            // Handle case where item_img is null or empty
            holder.imv_item_img.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    public static class DeliveredOrdersDetailsViewHolder extends RecyclerView.ViewHolder{
        TextView txt_item_price, txt_item_id, txt_item_name;
        ImageView imv_item_img;

        public DeliveredOrdersDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_item_name = itemView.findViewById(R.id.item_name);
            txt_item_id = itemView.findViewById(R.id.item_id);
            txt_item_price = itemView.findViewById(R.id.item_price);
            imv_item_img = itemView.findViewById(R.id.item_img);
        }
    }
}
