package AdminHomePageDirectory.SalesReport;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import UserHomePageDirectory.GetItems;

public class DetailsSalesReport extends AppCompatActivity {
    private TextView currentDayTextView;
    private TextView totalOrdersTitleTextView;
    private TextView totalSalesTodayTextView;
    private RecyclerView orderListRecyclerView;
    private Button backButton;
    private FirebaseFirestore db;
    private List<GetItems> items;
    private List<String> itemIds;
    private AdapterSalesReport adapterSalesReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_sales_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentDayTextView = findViewById(R.id.currentDay);
        totalOrdersTitleTextView = findViewById(R.id.totalOrdersTitle);
        orderListRecyclerView = findViewById(R.id.rv_orderList);
        totalSalesTodayTextView = findViewById(R.id.totalSalesToday);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();
        items = new ArrayList<>();
        itemIds = new ArrayList<>();

        // Retrieve data from Intent
        Intent params = getIntent();
        String mode = params.getStringExtra("mode");

        adapterSalesReport = new AdapterSalesReport(items, this, mode, itemIds);
        orderListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderListRecyclerView.setAdapter(adapterSalesReport);
        getItemsFromFireStore(mode);
        setDates(mode);
        setSales(mode);

        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    private void setDates(String mode) {
        Calendar calendar = Calendar.getInstance();

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // Get the day of the week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Convert the day of the week to a string
        String dayName = "";
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                dayName = "Sunday";
                break;
            case Calendar.MONDAY:
                dayName = "Monday";
                break;
            case Calendar.TUESDAY:
                dayName = "Tuesday";
                break;
            case Calendar.WEDNESDAY:
                dayName = "Wednesday";
                break;
            case Calendar.THURSDAY:
                dayName = "Thursday";
                break;
            case Calendar.FRIDAY:
                dayName = "Friday";
                break;
            case Calendar.SATURDAY:
                dayName = "Saturday";
                break;
        }

        // format month
        String monthName = "";
        switch (currentMonth){
            case Calendar.JANUARY:
                monthName = "January";
                break;
            case Calendar.FEBRUARY:
                monthName = "February";
                break;
            case Calendar.MARCH:
                monthName = "March";
                break;
            case Calendar.APRIL:
                monthName = "April";
                break;
            case Calendar.MAY:
                monthName = "May";
                break;
            case Calendar.JUNE:
                monthName = "June";
                break;
            case Calendar.JULY:
                monthName = "July";
                break;
            case Calendar.AUGUST:
                monthName = "August";
                break;
            case Calendar.SEPTEMBER:
                monthName = "September";
                break;
            case Calendar.OCTOBER:
                monthName = "October";
                break;
            case Calendar.NOVEMBER:
                monthName = "November";
                break;
            case Calendar.DECEMBER:
                monthName = "December";
                break;
        }

        switch (mode){
            case "DAILY":
                currentDayTextView.setText(String.format(currentDay+ " "+ dayName));
                totalOrdersTitleTextView.setText("Total Orders Today");
                break;
            case "MONTHLY":
                currentDayTextView.setText(String.format(currentDay+ " "+ monthName));
                totalOrdersTitleTextView.setText("Total Orders This Month");
                break;
            case "ANNUAL":
                currentDayTextView.setText(String.valueOf(currentYear));
                totalOrdersTitleTextView.setText("Total Orders This Year");
                break;
        }
    }


    private void getItemsFromFireStore(String mode){
        db.collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null){
                            Log.w("DetailsDailySalesReport", "Listen failed.", error);
                            return;
                        }
                        items.clear();
                        itemIds.clear();
                        for (DocumentSnapshot snapshot : value.getDocuments()){
                            GetItems get_items = snapshot.toObject(GetItems.class);
                            if (get_items != null) {
                                get_items.setItem_id(snapshot.getId());
                                items.add(get_items);
                                itemIds.add(snapshot.getId());
                            }
                        }

                        if (adapterSalesReport != null) {
                            adapterSalesReport.notifyDataSetChanged();
                        } else {
                            adapterSalesReport = new AdapterSalesReport(items, DetailsSalesReport.this, mode, itemIds);
                            orderListRecyclerView.setAdapter(adapterSalesReport);
                        }
                    }
                });
    }

    private void setSales(String mode) {
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
                        Long totalAmount = document.getLong("total_amount");

                        // Check if totalAmount is not null
                        if (totalAmount != null) {
                            // Check if orderItems is not null
                            if (orderItems != null) {
                                for (Map<String, Object> itemMap : orderItems) {
                                    // Assuming each map has a key "item_id" that stores the item ID
                                    String itemId = (String) itemMap.get("item_id");
                                    if (itemIds.contains(itemId)) {
                                        // Only consider documents from the current day
                                        if (docYear == currentYear) {
                                            annualTotal += totalAmount;

                                            if (docMonth == currentMonth) {
                                                monthlyTotal += totalAmount;

                                                if (docDay == currentDay) {
                                                    dailyTotal += totalAmount;
                                                }
                                            }

                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Update the total sales TextView
                switch (mode){
                    case "DAILY":
                        totalSalesTodayTextView.setText(String.format("Total Sales Today: ₱"+ dailyTotal));
                        break;
                    case "MONTHLY":
                        totalSalesTodayTextView.setText(String.format("Total Sales This Month: ₱"+ monthlyTotal));
                        break;
                    case "ANNUAL":
                        totalSalesTodayTextView.setText(String.format("Total Sales This Year: ₱"+ annualTotal));
                        break;
                }

            } else {
                Toast.makeText(DetailsSalesReport.this, "Failed to retrieve sales data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}