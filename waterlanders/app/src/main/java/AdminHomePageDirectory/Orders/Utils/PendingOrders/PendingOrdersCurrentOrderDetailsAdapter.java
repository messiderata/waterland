package AdminHomePageDirectory.Orders.Utils.PendingOrders;

import android.content.Context;
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

import AdminHomePageDirectory.Orders.Utils.OrderedItemsConstructor;

public class PendingOrdersCurrentOrderDetailsAdapter extends RecyclerView.Adapter<PendingOrdersCurrentOrderDetailsAdapter.PendingOrdersCurrentOrderDetailsViewHolder>{

    Context context;
    List<OrderedItemsConstructor> orderedItemsConstructorList;

    public PendingOrdersCurrentOrderDetailsAdapter(Context context, List<OrderedItemsConstructor> orderedItemsConstructorList) {
        this.context = context;
        this.orderedItemsConstructorList = orderedItemsConstructorList;
    }

    public static class PendingOrdersCurrentOrderDetailsViewHolder extends RecyclerView.ViewHolder {
        ImageView itemIMG;
        TextView itemName;
        TextView itemID;
        TextView itemPrice;
        TextView itemQuantity;
        TextView totalAmount;

        public PendingOrdersCurrentOrderDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemIMG = itemView.findViewById(R.id.item_img);
            itemName = itemView.findViewById(R.id.item_name);
            itemID = itemView.findViewById(R.id.item_id);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemQuantity = itemView.findViewById(R.id.item_quantity);
            totalAmount = itemView.findViewById(R.id.total_amount);
        }
    }

    @NonNull
    @Override
    public PendingOrdersCurrentOrderDetailsAdapter.PendingOrdersCurrentOrderDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_admin_orders_order_list_card, parent, false);
        return new PendingOrdersCurrentOrderDetailsAdapter.PendingOrdersCurrentOrderDetailsViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return orderedItemsConstructorList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrdersCurrentOrderDetailsAdapter.PendingOrdersCurrentOrderDetailsViewHolder holder, int position) {
        OrderedItemsConstructor currentOrder = orderedItemsConstructorList.get(position);

        // display the icon
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(currentOrder.getItem_img());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(context).load(uri).into(holder.itemIMG);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
        });

        // set text details
        holder.itemName.setText(String.format(currentOrder.getItem_name()));
        holder.itemID.setText(String.format("ITEM ID: " + currentOrder.getItem_id()));
        holder.itemPrice.setText(String.format("ITEM PRICE: ₱" + currentOrder.getItem_price()));
        holder.itemQuantity.setText(String.format("QTY: " + currentOrder.getItem_order_quantity()));
        holder.totalAmount.setText(String.format("₱" + currentOrder.getItem_total_price()));
    }

}
