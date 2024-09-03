package AdminHomePageDirectory.SalesReport;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;


import com.bumptech.glide.Glide;
import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class DashboardSalesReport extends AppCompatActivity {

    private String activityTitleIntent;
    private String salesReportIconIntent;
    private String totalOrdersTextIntent;
    private String currentDateTextIntent;
    private String totalSalesTextInput;

    private ImageView backButton;
    private TextView activityTitle;

    private ImageView salesReportIcon;
    private TextView totalOrdersText;
    private TextView currentDateText;

    private TableLayout tableLayoutItems;

    private TextView totalSalesText;
    private TextView totalSalesValue;
    private int totalSalesValueInt;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard_sales_report);
        initializeObjectIds();
        initializeIntents();
        setTextContents();

        populateTable();

        // event listener for back button
        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    private void initializeObjectIds(){
        backButton = findViewById(R.id.back_button);
        activityTitle = findViewById(R.id.activity_title);

        salesReportIcon = findViewById(R.id.sales_report_icon);
        totalOrdersText = findViewById(R.id.total_orders_text);
        currentDateText = findViewById(R.id.current_date_text);

        tableLayoutItems = findViewById(R.id.table_layout_items);

        totalSalesText = findViewById(R.id.total_sales_text);
        totalSalesValue = findViewById(R.id.total_sales_value);

        db = FirebaseFirestore.getInstance();
    }

    private void initializeIntents(){
        Intent intent = getIntent();
        activityTitleIntent = (String) intent.getSerializableExtra("activity_title");
        salesReportIconIntent = (String) intent.getSerializableExtra("sales_report_icon");
        totalOrdersTextIntent = (String) intent.getSerializableExtra("total_orders_text");
        currentDateTextIntent = (String) intent.getSerializableExtra("current_date_text");
        totalSalesTextInput = (String) intent.getSerializableExtra("total_sales_text");
    }

    private void setTextContents(){
        activityTitle.setText(activityTitleIntent);
        totalOrdersText.setText(totalOrdersTextIntent);
        currentDateText.setText(currentDateTextIntent);
        totalSalesText.setText(totalSalesTextInput);

        int resId = getResources().getIdentifier(salesReportIconIntent, "drawable", getPackageName());
        if (resId != 0) { // Check if the resource exists
            salesReportIcon.setImageResource(resId);
        }
    }

    // this method is basically populate the table row base on the number of items
    // in the firebase firestore 'items' table
    // then it will compute for the number of sold items
    // base on the items sold in 'deliveredOrders' collection
    // then it will compute for the total sales for each item
    // finally it will compute the total sales for the total sales
    private void populateTable(){
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> itemsList = task.getResult().getDocuments();
                totalSalesValueInt = 0;

                final int totalItemCount = itemsList.size();
                final int[] processedItemCount = {0};

                for (int i = 0; i < itemsList.size(); i++) {
                    DocumentSnapshot itemDoc = itemsList.get(i);
                    String itemId = itemDoc.getId();
                    String itemName = itemDoc.getString("item_name");
                    String itemImgUrl = itemDoc.getString("item_img");

                    int finalI = i;
                    calculateTotalSoldAndSales(itemId, totalSold -> {
                        TableRow row = new TableRow(this);
                        row.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        // Set background color to #D1EAF9
                        row.setBackgroundColor(Color.parseColor("#D1EAF9"));
                        row.setPadding(0, dpToPx(5), 0, dpToPx(5));

                        // Set a bottom border using a drawable
                        GradientDrawable border = new GradientDrawable();
                        border.setColor(Color.parseColor("#D1EAF9")); // Background color
                        border.setStroke(2, Color.BLACK); // Border bottom color and width
                        row.setBackground(border);

                        // "No" Column
                        TextView noColumn = new TextView(this);
                        noColumn.setText(String.valueOf(finalI + 1));
                        noColumn.setTextColor(ContextCompat.getColor(this, R.color.black));
                        noColumn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        noColumn.setLayoutParams(new TableRow.LayoutParams(
                                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(noColumn);

                        // "Product" Column - Combine Item Name and Image
                        LinearLayout productLayout = new LinearLayout(this);
                        productLayout.setOrientation(LinearLayout.HORIZONTAL);
                        productLayout.setLayoutParams(new TableRow.LayoutParams(
                                0, TableRow.LayoutParams.WRAP_CONTENT, 3f));

                        // Set layout parameters with margin for the product image
                        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(100, 100); // Set image size
                        imageLayoutParams.setMargins(0, 0, dpToPx(5), 0); // Add 5dp margin to the end (right side)

                        // Create and set the ImageView
                        ImageView productImage = new ImageView(this);
                        productImage.setLayoutParams(imageLayoutParams);

                        // Convert the GCS URL to a downloadable URL
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReferenceFromUrl(itemImgUrl);
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Load image using Glide with the download URL
                            Glide.with(this).load(uri).into(productImage);
                        }).addOnFailureListener(exception -> {
                            // Handle any errors
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        });

                        TextView productText = new TextView(this);
                        productText.setText(itemName);
                        productText.setTextColor(ContextCompat.getColor(this, R.color.black));
                        productText.setSingleLine(false);
                        productText.setMaxLines(Integer.MAX_VALUE);
                        productText.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        productText.setEllipsize(TextUtils.TruncateAt.END);

                        productLayout.addView(productImage);
                        productLayout.addView(productText);
                        row.addView(productLayout);

                        // "Sold" Column
                        TextView soldColumn = new TextView(this);
                        soldColumn.setText(String.valueOf(totalSold));
                        soldColumn.setTextColor(ContextCompat.getColor(this, R.color.black));
                        soldColumn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        soldColumn.setLayoutParams(new TableRow.LayoutParams(
                                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(soldColumn);

                        // "Total Sale" Column
                        TextView totalSaleColumn = new TextView(this);
                        int totalSale = totalSold * itemDoc.getLong("item_price").intValue();
                        totalSalesValueInt += totalSale;
                        totalSaleColumn.setText(String.format("₱%d", totalSold * itemDoc.getLong("item_price")));
                        totalSaleColumn.setTextColor(ContextCompat.getColor(this, R.color.black));
                        totalSaleColumn.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        totalSaleColumn.setLayoutParams(new TableRow.LayoutParams(
                                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(totalSaleColumn);

                        // Add the row to the table layout
                        tableLayoutItems.addView(row);

                        processedItemCount[0]++;
                        if (processedItemCount[0] == totalItemCount) {
                            totalSalesValue.setText(String.valueOf(totalSalesValueInt));
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void calculateTotalSoldAndSales(String itemId, OnTotalSoldCalculatedListener listener) {
        // temporarily the collection is set to 'pendingOrders'
        // will set to 'deliveredOrders' later

        // as for now it will calculate for all items
        // i will add a filter later that will base on daily, monthly, and yearly

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pendingOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int totalSold = 0;

                for (DocumentSnapshot orderDoc : task.getResult().getDocuments()) {
                    List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderDoc.get("order_items");
                    if (orderItems != null) {
                        for (Map<String, Object> itemData : orderItems) {
                            // Check if the current map entry has the target itemId
                            if (itemData.containsKey("item_id") && itemData.get("item_id").equals(itemId)) {
                                totalSold += ((Number) itemData.get("item_order_quantity")).intValue();
                            }
                        }
                    }
                }

                listener.onTotalSoldCalculated(totalSold);
            } else {
                Toast.makeText(this, "Failed to retrieve order data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface OnTotalSoldCalculatedListener {
        void onTotalSoldCalculated(int totalSold);
    }
}