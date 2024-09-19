package UserHomePageDirectory.HomeFragmentUtils;

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
    private final Context context;
    private OnTotalAmountChangeListener onTotalAmountChangeListener;
    private final AddedItems addedItems;

    // constructor to initialize items from the retrieve and display items
    // from the 'HomeFragment' base on the items in the database
    public ItemAdapter(List<GetItems> itemsList, Context context) {
        this.itemsList = itemsList;
        this.context = context;
        this.addedItems = new AddedItems();
    }

    // initialize objects base on the 'layout_user_item' to
    // display and manipulate their value later on
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextView itemPrice;
        TextView itemQuantity;
        TextView itemQuantityPrice;
        ImageView itemIMG;
        Button increaseButton;
        Button decreaseButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemPrice = itemView.findViewById(R.id.item_price);
            itemIMG = itemView.findViewById(R.id.item_img);

            itemQuantity = itemView.findViewById(R.id.item_quantity);
            itemQuantityPrice = itemView.findViewById(R.id.item_quantity_price);
            increaseButton = itemView.findViewById(R.id.item_btn_increase);
            decreaseButton = itemView.findViewById(R.id.item_btn_decrease);
        }
    }

    // this method is responsible from connecting the 'itemAdapter' java file
    // to 'layout_user_item' layout
    @NonNull
    @Override
    public ItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_user_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    // this method where all the manipulation and displaying of values are made
    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ItemViewHolder holder, int position) {
        GetItems items = itemsList.get(position);

        // display the item icon
        // this first convert the firebase storage link to
        // downloadable link to display properly the icon of the item
        // using Glide library
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
                        .into(holder.itemIMG);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("GLIDE", "Error getting download URL", exception);
                holder.itemIMG.setImageResource(R.drawable.ic_launcher_background);
            });
        } else {
            // Handle case where item_img is null or empty
            holder.itemIMG.setImageResource(R.drawable.ic_launcher_background);
        }

        // set text values base on the data in items in itemList
        holder.itemName.setText(items.getItem_name());
        holder.itemPrice.setText(String.format("₱" + items.getItem_price()));
        // Set up initial quantity and total price
        holder.itemQuantity.setText(String.valueOf(0));
        holder.itemQuantityPrice.setText("₱0");

        // click listener for '+' button
        // if the button is clicked
        // the 'itemPrice', 'itemQuantity', and 'itemQuantityPrice' will be updated
        holder.increaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.itemQuantity.getText().toString());
            currentQuantity++;
            holder.itemQuantity.setText(String.valueOf(currentQuantity));

            int[] prices = updateTotalPrice(holder, items, currentQuantity, "btn_increase");
            int totalItemPrice = prices[0];

            // check the item is already in the cartItems
            // if item is already in the cart then
            // update the 'item_order_quantity' and 'item_total_price'
            // else add the item to the cart
            if (addedItems.isItemInCart(items.getItem_id())){
                addedItems.updateItemQuantity(items.getItem_id(), totalItemPrice, currentQuantity);
            } else {
                addedItems.addItem(items, totalItemPrice, currentQuantity);
            }

            notifyTotalAmountChange();
            addedItems.logCartItems();
        });

        // click listener for '-' button
        // same flow in the previous button but this time values are decrease
        holder.decreaseButton.setOnClickListener(v -> {
            int currentQuantity = Integer.parseInt(holder.itemQuantity.getText().toString());
            if (currentQuantity > 0) {
                currentQuantity--;
                holder.itemQuantity.setText(String.valueOf(currentQuantity));

                int[] prices = updateTotalPrice(holder, items, currentQuantity, "btn_decrease");
                int itemPrice = prices[1];

                addedItems.removeItem(items.getItem_id(), itemPrice, currentQuantity);

                notifyTotalAmountChange();
                addedItems.logCartItems();
            }
        });
    }

    // this method will compute for the total price for each item
    // some items has 'discount' on them and this method
    // is also the one who is responsible for computing the item price
    // to have correct total amount
    private int[] updateTotalPrice(ItemViewHolder holder, GetItems items, int quantity, String mode) {
        int itemPrice = items.getItem_price();
        int totalPrice = itemPrice * quantity;
        String priceWithCurrency = "₱" + totalPrice;
        holder.itemQuantityPrice.setText(priceWithCurrency);

        String currItemID = items.getItem_id();
        if (currItemID.equals("Pp4FPWv56jS2cJcWOLlE")){
            int totalDiscount = (quantity / 3) * 5;
            totalPrice -= totalDiscount;
            priceWithCurrency = "₱" + totalPrice;
            holder.itemQuantityPrice.setText(priceWithCurrency);

            if (mode.equals("btn_decrease")){
                int previousQuantity = quantity + 1;
                if (previousQuantity % 3 == 0) {
                    itemPrice -= 5;
                }
            }
        }

        return new int[]{totalPrice, itemPrice};
    }

    // helper constructor to display the updated price
    // whenever there are changes in the item price
    public void setOnTotalAmountChangeListener(OnTotalAmountChangeListener listener) {
        this.onTotalAmountChangeListener = listener;
    }

    private void notifyTotalAmountChange() {
        if (onTotalAmountChangeListener != null) {
            onTotalAmountChangeListener.onTotalAmountChange(addedItems.getTotalAmount(), addedItems);
        }
    }

    public interface OnTotalAmountChangeListener {
        void onTotalAmountChange(int totalAmount, AddedItems addedItems);
    }

    public AddedItems getAddedItems() {
        return addedItems;
    }

}
