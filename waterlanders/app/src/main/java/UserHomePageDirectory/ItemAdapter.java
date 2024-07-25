package UserHomePageDirectory;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.waterlanders.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    List<GetItems> itemsList;
    private Context context;
    private OnTotalAmountChangeListener onTotalAmountChangeListener;
    private AddedItems addedItems;

    public ItemAdapter(List<GetItems> itemsList, Context context, String userId) {
        this.itemsList = itemsList;
        this.context = context;
        this.addedItems = new AddedItems(userId);
    }

    public void setOnTotalAmountChangeListener(OnTotalAmountChangeListener listener) {
        this.onTotalAmountChangeListener = listener;
    }

    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_user_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {
        GetItems items = itemsList.get(position);
        holder.txt_item_name.setText(items.getItem_name());

        // Format price with peso sign
        String priceWithCurrency = "₱" + items.getItem_price();
        holder.txt_item_price.setText(priceWithCurrency);

        Log.d("GLIDE", "txt_item_name: " + items.getItem_name());
        Log.d("GLIDE", "txt_item_price: " + items.getItem_price());
        Log.d("GLIDE", "item_img: " + items.getItem_img());

        String gsUrl = items.getItem_img();
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

        // Set up initial quantity and total price
        holder.txt_item_quantity.setText(String.valueOf(0));
        holder.txt_item_quantity_price.setText("₱0");

        // Set up click listeners for buttons
        holder.btn_increase.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.txt_item_quantity.getText().toString());
            currentQuantity++;
            holder.txt_item_quantity.setText(String.valueOf(currentQuantity));

            int itemPrice = items.getItem_price();
            updateTotalPrice(holder, itemPrice, currentQuantity);
            addedItems.addItem(items.getItem_id(), itemPrice);
            notifyTotalAmountChange();
        });

        holder.btn_decrease.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.txt_item_quantity.getText().toString());
            if (currentQuantity > 0) {
                currentQuantity--;
                holder.txt_item_quantity.setText(String.valueOf(currentQuantity));

                int itemPrice = items.getItem_price();
                updateTotalPrice(holder, itemPrice, currentQuantity);
                if (currentQuantity == 0) {
                    addedItems.removeItem(items.getItem_id(), itemPrice);
                }
                notifyTotalAmountChange();
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("ItemAdapter", "Item list size: " + itemsList.size());
        return itemsList.size();
    }

    private void updateTotalPrice(ItemViewHolder holder, int itemPrice, int quantity) {
        int totalPrice = itemPrice * quantity;
        String priceWithCurrency = "₱" + totalPrice;
        holder.txt_item_quantity_price.setText(priceWithCurrency);
    }

    private void notifyTotalAmountChange() {
        if (onTotalAmountChangeListener != null) {
            onTotalAmountChangeListener.onTotalAmountChange(addedItems.getTotalAmount());
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txt_item_name, txt_item_price, txt_item_quantity, txt_item_quantity_price;
        ImageView imv_item_img;
        Button btn_increase, btn_decrease;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_item_name = itemView.findViewById(R.id.item_name);
            txt_item_price = itemView.findViewById(R.id.item_price);
            imv_item_img = itemView.findViewById(R.id.item_img);

            txt_item_quantity = itemView.findViewById(R.id.item_quantity);
            txt_item_quantity_price = itemView.findViewById(R.id.item_quantity_price);
            btn_increase = itemView.findViewById(R.id.item_btn_increase);
            btn_decrease = itemView.findViewById(R.id.item_btn_decrease);
        }
    }

    public interface OnTotalAmountChangeListener {
        void onTotalAmountChange(int totalAmount);
    }

    public AddedItems getAddedItems() {
        return addedItems;
    }

}