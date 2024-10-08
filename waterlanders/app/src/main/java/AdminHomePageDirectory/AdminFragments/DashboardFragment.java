package AdminHomePageDirectory.AdminFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

import AdminHomePageDirectory.SalesReport.DashboardSalesReport;

public class DashboardFragment extends Fragment {

    private CardView annualSalesReportCard;
    private CardView monthlySalesReportCard;
    private CardView dailySalesReportCard;

    private TextView totalAnnualSale;
    private TextView currentAnnualYear;
    private TextView currentMonth;
    private TextView totalMonthlySale;
    private TextView currentDay;
    private TextView totalTodaySale;

    private TextView totalPendingOrders;
    private TextView totalWaitingOrders;
    private TextView totalOnDeliveryOrders;
    private TextView totalDeliveredOrders;

    private TextView totalAppUsers;

    private FirebaseFirestore db;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        // Initialize the TextView objects using the inflated view
        initializeTextValues(view);
        formatTextViewDates();

        // get and set the values from firebase
        setTotalSales();
        setTotalOrders();
        setTotalUsers();

        // set event listeners to the cards to show the details
        annualSalesReportCard.setOnClickListener(v -> {
            Intent showSalesIntent = new Intent(getActivity(), DashboardSalesReport.class);
            showSalesIntent.putExtra("activity_title", "ANNUAL SALES REPORT");
            showSalesIntent.putExtra("sales_report_icon", "annual_sales_report_icon");
            showSalesIntent.putExtra("total_orders_text", "Total Orders This Year");
            showSalesIntent.putExtra("current_date_text", currentAnnualYear.getText());
            showSalesIntent.putExtra("total_sales_text", "Total Sales This Year");
            showSalesIntent.putExtra("mode", "ANNUAL");
            startActivity(showSalesIntent);
        });

        monthlySalesReportCard.setOnClickListener(v -> {
            Intent showSalesIntent = new Intent(getActivity(), DashboardSalesReport.class);
            showSalesIntent.putExtra("activity_title", "MONTHLY SALES REPORT");
            showSalesIntent.putExtra("sales_report_icon", "monthly_sales_report_icon");
            showSalesIntent.putExtra("total_orders_text", "Total Orders This Month");
            showSalesIntent.putExtra("current_date_text", currentMonth.getText());
            showSalesIntent.putExtra("total_sales_text", "Total Sales This Month");
            showSalesIntent.putExtra("mode", "MONTHLY");
            startActivity(showSalesIntent);
        });

        dailySalesReportCard.setOnClickListener(v -> {
            Intent showSalesIntent = new Intent(getActivity(), DashboardSalesReport.class);
            showSalesIntent.putExtra("activity_title", "DAILY SALES REPORT");
            showSalesIntent.putExtra("sales_report_icon", "daily_sales_report_icon");
            showSalesIntent.putExtra("total_orders_text", "Total Orders Today");
            showSalesIntent.putExtra("current_date_text", currentDay.getText());
            showSalesIntent.putExtra("total_sales_text", "Total Sales Today");
            showSalesIntent.putExtra("mode", "DAY");
            startActivity(showSalesIntent);
        });

        return view;
    }

    private void initializeTextValues(View view){
        annualSalesReportCard = view.findViewById(R.id.annual_sales_report_card);
        monthlySalesReportCard = view.findViewById(R.id.monthly_sales_report_card);
        dailySalesReportCard = view.findViewById(R.id.daily_sales_report_card);

        totalAnnualSale = view.findViewById(R.id.total_annual_sale);
        currentAnnualYear = view.findViewById(R.id.current_annual_year);
        currentMonth = view.findViewById(R.id.current_month);
        totalMonthlySale = view.findViewById(R.id.total_monthly_sale);
        currentDay = view.findViewById(R.id.current_day);
        totalTodaySale = view.findViewById(R.id.total_today_sale);

        totalPendingOrders = view.findViewById(R.id.total_pending_orders);
        totalWaitingOrders = view.findViewById(R.id.total_waiting_orders);
        totalOnDeliveryOrders = view.findViewById(R.id.total_onDelivery_orders);
        totalDeliveredOrders = view.findViewById(R.id.total_delivered_orders);

        totalAppUsers = view.findViewById(R.id.total_app_users);

        db = FirebaseFirestore.getInstance();
    }

    private void formatTextViewDates(){
        Calendar calendar = Calendar.getInstance();
        int currentDayInt = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonthInt = calendar.get(Calendar.MONTH);
        int currentYearInt = calendar.get(Calendar.YEAR);
        int dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK);

        String[] months = new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        String[] days = new String[]{
                "buffer", "Sunday", "Monday", "Tuesday",
                "Wednesday", "Thursday", "Friday", "Saturday"
        };

        currentAnnualYear.setText(String.format(String.valueOf(currentYearInt)));
        currentMonth.setText(String.format(currentDayInt + " " + months[currentMonthInt]));
        currentDay.setText(String.format(currentDayInt + " " + days[dayOfWeekInt]));
    }

    private void setTotalSales() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        db.collection("deliveredOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                int dailyTotal = 0;
                int monthlyTotal = 0;
                int annualTotal = 0;

                for (DocumentSnapshot document : documents) {
                    if (!document.getId().equals("test_id")) {
                        // Get the date and check if it matches the current day
                        Timestamp timestamp = document.getTimestamp("date_delivered");
                        if (timestamp != null) {
                            Calendar docCalendar = Calendar.getInstance();
                            docCalendar.setTime(timestamp.toDate());

                            int docDay = docCalendar.get(Calendar.DAY_OF_MONTH);
                            int docMonth = docCalendar.get(Calendar.MONTH) + 1;
                            int docYear = docCalendar.get(Calendar.YEAR);

                            Long totalAmount = document.getLong("total_amount");

                            if (totalAmount != null) {
                                if (docYear == currentYear) {
                                    annualTotal += totalAmount;

                                    if (docMonth == currentMonth) {
                                        monthlyTotal += totalAmount;

                                        if (docDay == currentDay) {
                                            dailyTotal += totalAmount;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // update the textView Value
                totalTodaySale.setText(String.format("₱" + dailyTotal));
                totalMonthlySale.setText(String.format("₱" + monthlyTotal));
                totalAnnualSale.setText(String.format("₱" + annualTotal));

            } else {
                Toast.makeText(getContext(), "Failed to retrieve sales data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTotalOrders(){
        // pending orders
        db.collection("pendingOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalOrdersCount = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals("test_id")) {
                        totalOrdersCount++;
                    }
                }
                totalPendingOrders.setText(String.valueOf(totalOrdersCount));
            } else {
                Toast.makeText(getContext(), "Failed to retrieve pending orders count", Toast.LENGTH_SHORT).show();
            }
        });


        // waiting for courier orders
        db.collection("waitingForCourier").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalOrdersCount = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals("test_id")) {
                        totalOrdersCount++;
                    }
                }
                totalWaitingOrders.setText(String.valueOf(totalOrdersCount));
            } else {
                Toast.makeText(getContext(), "Failed to retrieve waiting for courier orders count", Toast.LENGTH_SHORT).show();
            }
        });

        // on delivery orders
        db.collection("onDelivery").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalOrdersCount = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals("test_id")) {
                        totalOrdersCount++;
                    }
                }
                totalOnDeliveryOrders.setText(String.valueOf(totalOrdersCount));
            } else {
                Toast.makeText(getContext(), "Failed to retrieve on delivery orders count", Toast.LENGTH_SHORT).show();
            }
        });

        // delivered orders
        db.collection("deliveredOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalOrdersCount = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals("test_id")) {
                        totalOrdersCount++;
                    }
                }
                totalDeliveredOrders.setText(String.valueOf(totalOrdersCount));
            } else {
                Toast.makeText(getContext(), "Failed to retrieve delivered orders count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setTotalUsers(){
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalUsersCount = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    // prevent from counting the the admin account as user
                    if (!document.getId().equals("NVWcwGTdD1VdMcnUg86IBAUuE3i2")) {
                        totalUsersCount++;
                    }
                }
                totalAppUsers.setText(String.valueOf(totalUsersCount));
            } else {
                Toast.makeText(getContext(), "Failed to retrieve users count", Toast.LENGTH_SHORT).show();
            }
        });
    }
}