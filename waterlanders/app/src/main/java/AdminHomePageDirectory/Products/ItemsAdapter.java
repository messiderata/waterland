package AdminHomePageDirectory.Products;

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

import java.io.Serializable;
import java.util.List;


public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.itemsAdapterViewHolder> {
    Context context;
    List<ItemsConstructor> itemsConstructorList;

    // constructor method to initialize the objects
    // from ProductsFragment.java to this file
    public ItemsAdapter(Context context, List<ItemsConstructor> itemsConstructorList) {
        this.context = context;
        this.itemsConstructorList = itemsConstructorList;
    }

    // main method where you can initialize objects from your layout file
    public static class itemsAdapterViewHolder extends RecyclerView.ViewHolder{
        ImageView productItemIMG;
        TextView productName;
        TextView productID;
        ImageView productEditButton;
        ImageView productDeleteButton;

        public itemsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            productItemIMG = itemView.findViewById(R.id.product_item_img);
            productName = itemView.findViewById(R.id.product_name);
            productID = itemView.findViewById(R.id.product_id);
            productEditButton = itemView.findViewById(R.id.product_edit_button);
            productDeleteButton = itemView.findViewById(R.id.product_delete_button);
        }
    }

    // this method links your layout xml file
    @NonNull
    @Override
    public ItemsAdapter.itemsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_admin_products_cardview, parent, false);
        return new ItemsAdapter.itemsAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return itemsConstructorList.size();
    }

    // this method where all the work is done
    // from displaying the cards to the recycler view
    // to displaying the details to the cards such as name, id, etc.
    @Override
    public void onBindViewHolder(@NonNull ItemsAdapter.itemsAdapterViewHolder holder, int position) {
        ItemsConstructor cardItems = itemsConstructorList.get(position);

        // display the icon
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(cardItems.getItem_img());
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            // Load image using Glide with the download URL
            Glide.with(context).load(uri).into(holder.productItemIMG);
        }).addOnFailureListener(exception -> {
            // Handle any errors
            Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
        });

        // set the item name and item id
        holder.productName.setText(String.format(cardItems.getItem_name()));
        holder.productID.setText(String.format(cardItems.getItem_id()));

        // set event listeners for the edit and delete button for each item cards
        holder.productEditButton.setOnClickListener(v -> {
            Intent showEditActivityIntent = new Intent(context, EditItem.class);
            showEditActivityIntent.putExtra("current_item", (Serializable) cardItems);
            context.startActivity(showEditActivityIntent);
        });

        holder.productDeleteButton.setOnClickListener(v -> {
            Intent showDeleteActivityIntent = new Intent(context, DeleteItem.class);
            showDeleteActivityIntent.putExtra("current_item", (Serializable) cardItems);
            context.startActivity(showDeleteActivityIntent);
        });
    }

}
