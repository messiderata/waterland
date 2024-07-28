package AdminHomePageDirectory.SalesReport;

import android.content.Context;
import android.util.Log;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import UserHomePageDirectory.GetItems;


public class AdapterSalesReport extends RecyclerView.Adapter<AdapterSalesReport.DailySalesReportViewHolder>{
    List<GetItems> items;
    Context context;
    String mode;
    List<String> itemIds;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "AdapterDailySalesReport";

    public AdapterSalesReport(List<GetItems> items, Context context, String mode, List<String> itemIds) {
        this.items = items;
        this.context = context;
        this.mode = mode;
        this.itemIds = itemIds;
    }

    @NonNull
    @Override
    public AdapterSalesReport.DailySalesReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_sales_report, parent, false);
        return new AdapterSalesReport.DailySalesReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSalesReport.DailySalesReportViewHolder holder, int position) {
        GetItems curr_items = items.get(position);
        Log.d(TAG, "->> getItem_name: " + curr_items.getItem_name());
        Log.d(TAG, "->> getItem_price: " + curr_items.getItem_price());
        Log.d(TAG, "->> getItem_img: " + curr_items.getItem_img());
        Log.d(TAG, "->> getItem_id: " + curr_items.getItem_id());

        String formatItemName = "Item Name: " + curr_items.getItem_name();
        String formatItemID = "Item ID: " + curr_items.getItem_id();
        String formatItemPrice = "Item Price: " + curr_items.getItem_price();

        holder.itemNameTextView.setText(formatItemName);
        holder.itemIDTextView.setText(formatItemID);
        holder.itemPriceTextView.setText(formatItemPrice);

        String gsUrl = curr_items.getItem_img();
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
                        .into(holder.itemImgImageView);
            }).addOnFailureListener(exception -> {
                // Handle any errors
                Log.e("GLIDE", "Error getting download URL", exception);
                holder.itemImgImageView.setImageResource(R.drawable.ic_launcher_background);
            });
        } else {
            // Handle case where item_img is null or empty
            holder.itemImgImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        setSales(curr_items.getItem_id(), holder.itemTotalSaleTextView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class DailySalesReportViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImgImageView;
        TextView itemNameTextView;
        TextView itemIDTextView;
        TextView itemPriceTextView;
        TextView itemTotalSaleTextView;

        public DailySalesReportViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImgImageView = itemView.findViewById(R.id.item_img);
            itemNameTextView = itemView.findViewById(R.id.item_name);
            itemIDTextView = itemView.findViewById(R.id.item_id);
            itemPriceTextView = itemView.findViewById(R.id.item_price);
            itemTotalSaleTextView = itemView.findViewById(R.id.itemTotalSale);
        }
    }

    private void setSales(String itemId, TextView itemTotalSaleTextView) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        db.collection("deliveredOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                int dailyTotal = 0;
                int monthlyTotal = 0;
                int annualTotal = 0;

                for (DocumentSnapshot document : documents) {
                    // Get the date and check if it matches the current day
                    Timestamp timestamp = document.getTimestamp("date_delivered");
                    if (timestamp != null) {
                        Calendar docCalendar = Calendar.getInstance();
                        docCalendar.setTime(timestamp.toDate());

                        int docDay = docCalendar.get(Calendar.DAY_OF_MONTH);
                        int docMonth = docCalendar.get(Calendar.MONTH) + 1;
                        int docYear = docCalendar.get(Calendar.YEAR);

                        // Assuming `order_items` is a List of Maps in the document
                        List<Map<String, Object>> orderItems = (List<Map<String, Object>>) document.get("order_items");

                        // Check if orderItems is not null
                        if (orderItems != null) {
                            for (Map<String, Object> itemMap : orderItems) {
                                // Assuming each map has a key "item_id" that stores the item ID
                                String itemIds = (String) itemMap.get("item_id");
                                Object itemPriceObj = itemMap.get("item_price");
                                int itemPrice = getItemPrice(itemPriceObj);

                                if (itemId.contains(itemIds)) {
                                    // Only consider documents from the current day
                                    if (docYear == currentYear) {
                                        annualTotal += itemPrice;

                                        if (docMonth == currentMonth) {
                                            monthlyTotal += itemPrice;

                                            if (docDay == currentDay) {
                                                dailyTotal += itemPrice;
                                            }
                                        }

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                // Update the total sales TextView
                switch (mode){
                    case "DAILY":
                        itemTotalSaleTextView.setText(String.format("₱"+ dailyTotal));
                        break;
                    case "MONTHLY":
                        itemTotalSaleTextView.setText(String.format("₱"+ monthlyTotal));
                        break;
                    case "ANNUAL":
                        itemTotalSaleTextView.setText(String.format("₱"+ annualTotal));
                        break;
                }

            } else {
                Toast.makeText(context, "Failed to retrieve sales data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static int getItemPrice(Object itemPriceObj) {
        int itemPrice = 0;

        if (itemPriceObj instanceof Long) {
            itemPrice = ((Long) itemPriceObj).intValue();
        } else if (itemPriceObj instanceof Double) {
            itemPrice = ((Double) itemPriceObj).intValue();
        } else if (itemPriceObj instanceof Integer) {
            itemPrice = (Integer) itemPriceObj;
        }
        return itemPrice;
    }
}
