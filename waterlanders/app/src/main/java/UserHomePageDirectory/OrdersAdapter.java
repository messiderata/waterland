package UserHomePageDirectory;

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

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private final List<GetItems> itemsList;
    private final Context context;

    public OrdersAdapter(List<GetItems> itemsList, Context context) {
        this.itemsList = itemsList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrdersAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_order_confirmation_items, parent, false);
        return new OrdersAdapter.OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.OrderViewHolder holder, int position) {
        GetItems item = itemsList.get(position);
        holder.txt_item_name.setText(item.getItem_name());

        // Format price with peso sign
        String priceWithCurrency = "₱" + item.getItem_price();
        holder.txt_item_price.setText(priceWithCurrency);

        String gsUrl = item.getItem_img();
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
        return itemsList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txt_item_name, txt_item_price;
        ImageView imv_item_img;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_item_name = itemView.findViewById(R.id.item_name);
            txt_item_price = itemView.findViewById(R.id.item_price);
            imv_item_img = itemView.findViewById(R.id.item_img);
        }
    }
}
