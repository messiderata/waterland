package AdminHomePageDirectory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

import AdminHomePageDirectory.SalesReport.DetailsSalesReport;
import LoginDirectory.Login;

public class AdminHomePage extends AppCompatActivity {
    private CardView dailySalesReportCardView;
    private CardView monthlySalesReportCardView;
    private CardView annualSalesReportCardView;
    private TextView dailyTotalSalesTextView;
    private TextView monthlyTotalSalesTextView;
    private TextView annualTotalSalesTextView;
    private TextView currentDayTextView;
    private TextView currentMonthTextView;
    private TextView currentYearTextView;
    private Button logoutButtonButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_home_page);

        dailySalesReportCardView = findViewById(R.id.dailySalesReport);
        monthlySalesReportCardView = findViewById(R.id.monthlySalesReport);
        annualSalesReportCardView = findViewById(R.id.annualSalesReport);
        dailyTotalSalesTextView = findViewById(R.id.dailyTotalSales);
        monthlyTotalSalesTextView = findViewById(R.id.monthlyTotalSales);
        annualTotalSalesTextView = findViewById(R.id.annualTotalSales);
        currentDayTextView = findViewById(R.id.currentDay);
        currentMonthTextView = findViewById(R.id.currentMonth);
        currentYearTextView = findViewById(R.id.currentYear);
        logoutButtonButton = findViewById(R.id.logoutButton);
        db = FirebaseFirestore.getInstance();

        setDates();
        setSales();

        dailySalesReportCardView.setOnClickListener(view -> {
            Intent intent = new Intent(this, DetailsSalesReport.class);
            intent.putExtra("mode", "DAILY");
            startActivity(intent);
        });

        monthlySalesReportCardView.setOnClickListener(view -> {
            Intent intent = new Intent(this, DetailsSalesReport.class);
            intent.putExtra("mode", "MONTHLY");
            startActivity(intent);
        });

        annualSalesReportCardView.setOnClickListener(view -> {
            Intent intent = new Intent(this, DetailsSalesReport.class);
            intent.putExtra("mode", "ANNUAL");
            startActivity(intent);
        });

        logoutButtonButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(AdminHomePage.this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminHomePage.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void setDates(){
        Calendar calendar = Calendar.getInstance();

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        // format day
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
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

        currentDayTextView.setText(String.format(currentDay+ " "+ dayName));
        currentMonthTextView.setText(String.format(currentDay+ " "+ monthName));
        currentYearTextView.setText(String.valueOf(currentYear));
    }

    private void setSales(){
        Calendar calendar = Calendar.getInstance();

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);

        db.collection("deliveredOrders").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                int dailyTotal = 0;
                int monthlyTotal = 0;
                int annualTotal = 0;

                for (DocumentSnapshot document : documents) {
                    Timestamp timestamp = document.getTimestamp("date_delivered");
                    Calendar docCalendar = Calendar.getInstance();
                    docCalendar.setTime(timestamp.toDate());

                    int docDay = docCalendar.get(Calendar.DAY_OF_MONTH);
                    int docMonth = docCalendar.get(Calendar.MONTH) + 1;
                    int docYear = docCalendar.get(Calendar.YEAR);

                    int totalAmount = document.getLong("total_amount").intValue();

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

                dailyTotalSalesTextView.setText(String.format("₱"+ dailyTotal));
                monthlyTotalSalesTextView.setText(String.format("₱"+ monthlyTotal));
                annualTotalSalesTextView.setText(String.format("₱"+ annualTotal));
            } else {
                Toast.makeText(AdminHomePage.this, "Failed to retrieve sales data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}