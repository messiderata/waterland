package UserHomePageDirectory.HomeFragmentUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import UserHomePageDirectory.FragmentsDirectory.HistoryFragment;
import UserHomePageDirectory.MainDashboardUser;
import UserHomePageDirectory.OrderTrackingFragments.UserPendingOrdersFragment;

public class OrderReceipt extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseDatabase rdb;
    private AddedItems addedItems;
    private Map<String, Object> currentDefaultAddress;
    private Map<String, Object> GCashPaymentDetails;
    private String modeOfPayment, additionalMessage, selectedDateValue;
    private CardView button_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_receipt);
        initializeObject();
        getIntentData();

        // Save order to Firebase
        generateUniqueDocumentId();

        // Set up button and its click listener
        button_btn.setOnClickListener(view -> {
            Intent backToHome = new Intent(OrderReceipt.this, MainDashboardUser.class);
            startActivity(backToHome);
            finish();
        });


    }

    private void initializeObject(){
        button_btn = findViewById(R.id.button_ok);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rdb = FirebaseDatabase.getInstance();
    }

    private void getIntentData(){
        // Retrieve intent data
        Intent intent = getIntent();
        addedItems = (AddedItems) intent.getSerializableExtra("addedItems");
        currentDefaultAddress = (Map<String, Object>) intent.getSerializableExtra("deliveryAddress");
        modeOfPayment = (String) intent.getSerializableExtra("modeOfPayment");
        additionalMessage = (String) intent.getSerializableExtra("additionalMessage");
        selectedDateValue = (String) intent.getSerializableExtra("selectedDateValue");

        if (modeOfPayment.equals("GCash")){
            GCashPaymentDetails = (Map<String, Object>) intent.getSerializableExtra("GCashPaymentDetails");
        }
    }

    // flow
    // this first generate a random id from 'pendingOrders' collection
    // then it will check if that id is already existed in
    // 'waitingForCourier', 'onDelivery', 'deliveredOrders' collections
    // this way we can pass the the id from a collection to another without conflict
    // this way as well we can track the order properly without being confused because of changing the id
    // then if the ids are existed then generate again
    // else save the order details to the firebase firestore

    // flow
    // first it generate id according to the date with the format 'YYYYXXXX' example '20240001'
    // the number will assign on the 'XXXX' is base on the number of items under
    // 'waitingForCourier', 'onDelivery', 'deliveredOrders' collections
    // base on the 'date_ordered' field in each document in each collections
    // the 'date_ordered' field look like this 'November 24, 2024 at 7:24:29PM UTC+8'
    // base on the number of items that is ordered within the current year will be counted
    // then the assigned id will +1 of the total counted items
    // example the total items under 2024 is 13 then the new id is 14 resulting in this format '20240014'
    private void generateUniqueDocumentId() {
        String documentId = db.collection("pendingOrders").document().getId();

        // Get the Current Year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String yearPrefix = String.valueOf(currentYear);

        // Count Documents for the Current Year
        String yearStart = "01/01/" + yearPrefix;
        String yearEnd = "12/31/" + yearPrefix;


        db.collection("pendingOrders")
                .whereGreaterThanOrEqualTo("date_delivery", yearStart)
                .whereLessThanOrEqualTo("date_delivery", yearEnd)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int pendingOrdersCount = task.getResult().size();
                        checkDocumentInWaitingOrdersCollection(pendingOrdersCount, yearStart, yearEnd, yearPrefix);
                    }
                });
    }

    private void checkDocumentInWaitingOrdersCollection(int totalCount, String yearStart, String yearEnd, String yearPrefix) {
        db.collection("waitingForCourier")
                .whereGreaterThanOrEqualTo("date_delivery", yearStart)
                .whereLessThanOrEqualTo("date_delivery", yearEnd)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int waitingForCourierCount = task.getResult().size();
                        int updatedCount = totalCount + waitingForCourierCount;
                        checkDocumentInOnDelivery(updatedCount, yearStart, yearEnd, yearPrefix);
                    }
                });
    }

    private void checkDocumentInOnDelivery(int totalCount, String yearStart, String yearEnd, String yearPrefix) {
          db.collection("onDelivery")
                .whereGreaterThanOrEqualTo("date_delivery", yearStart)
                .whereLessThanOrEqualTo("date_delivery", yearEnd)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int onDeliveryCount = task.getResult().size();
                        int updatedCount = totalCount + onDeliveryCount;
                        checkDocumentInDeliveredOrders(updatedCount, yearStart, yearEnd, yearPrefix);
                    }
                });
    }

    private void checkDocumentInDeliveredOrders(int totalCount, String yearStart, String yearEnd, String yearPrefix) {
        db.collection("deliveredOrders")
            .whereGreaterThanOrEqualTo("date_delivery", yearStart)
            .whereLessThanOrEqualTo("date_delivery", yearEnd)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    int deliveredOrdersCount = task.getResult().size();
                    int finalCount = totalCount + deliveredOrdersCount;
                    checkDocumentInCancelledOrders(finalCount, yearStart, yearEnd, yearPrefix);
                }
            });
    }

    private void checkDocumentInCancelledOrders(int totalCount, String yearStart, String yearEnd, String yearPrefix) {
        db.collection("cancelledOrders")
                .whereGreaterThanOrEqualTo("date_delivery", yearStart)
                .whereLessThanOrEqualTo("date_delivery", yearEnd)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        int deliveredOrdersCount = task.getResult().size();
                        int finalCount = totalCount + deliveredOrdersCount;

                        // Generate the unique ID
                        String uniqueDocumentId = yearPrefix + String.format("0" + finalCount);

                        // Save the document
                        saveOrderWithUniqueDocumentId(uniqueDocumentId);
                    }
                });
    }

    private void saveOrderWithUniqueDocumentId(String documentId) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("date_ordered", Timestamp.now());
        orderData.put("order_icon", addedItems.getOrderIcon());
        orderData.put("order_id", documentId);
        orderData.put("order_items", addedItems.getCartItems());
        orderData.put("order_status", "ORDERED");
        orderData.put("total_amount", addedItems.getTotalAmount());
        orderData.put("delivery_address", currentDefaultAddress);
        orderData.put("user_id", mAuth.getCurrentUser().getUid());
        orderData.put("mode_of_payment", modeOfPayment);
        orderData.put("additional_message", additionalMessage);

        if (modeOfPayment.equals("GCash")){
            orderData.put("gcash_payment_details", GCashPaymentDetails);
            orderData.put("isPaid", "YES");
        } else {
            orderData.put("isPaid", "NO");
        }

        // Retrieve the current user's 'fullName' and save it to search term
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullName = documentSnapshot.getString("fullName");
                String email = documentSnapshot.getString("email");
                String[] nameParts = fullName.split(" ");
                if (nameParts.length >= 2) {
                    orderData.put("search_term", nameParts[1]);
                } else if (nameParts.length == 1) {
                    orderData.put("search_term", nameParts[0]);
                } else {
                    orderData.put("search_term", "User no surname");
                }

                String accountStatus = documentSnapshot.getString("accountStatus");
                orderData.put("accountStatus", accountStatus);

                // Delivery date
                selectedDateValue = selectedDateValue.replace("Selected Date: ", "");
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
                LocalDate date = LocalDate.parse(selectedDateValue, inputFormatter);
                String formattedDate = date.format(outputFormatter);
                orderData.put("date_delivery", formattedDate);

                db.collection("pendingOrders")
                        .document(documentId)
                        .set(orderData)
                        .addOnSuccessListener(aVoid -> {
                            sendInvoiceEmail(orderData, fullName, email);
                            saveOrderInRealtimeDatabase(documentId);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrderReceipt.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(OrderReceipt.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
        });


    }

    // if user order something then
    // save the orderID and order status to 'userUID'
    // if userUID is already exist just append the new data
    public void saveOrderInRealtimeDatabase(String documentId){
        String userUID = mAuth.getCurrentUser().getUid();
        DatabaseReference myRef = rdb.getReference(userUID);

        // Create an order object
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", documentId);
        orderData.put("orderStatus", "ORDERED");

        // Save the order data under 'orders' node
        if (documentId != null) {
            myRef.child("orders").child(documentId).setValue(orderData)
                .addOnSuccessListener(aVoid -> {
                    // Order saved successfully
                    Log.d("Order", "Order saved successfully");
                })
                .addOnFailureListener(e -> {
                    // Failed to save order
                    Log.e("Order", "Failed to save order", e);
                });
        }
    }

    private void sendInvoiceEmail(Map<String, Object> orderData, String fullName, String recipientEmail){
        Log.d("invoiceEmail", "send invoice email method is initiated");
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        Timestamp timestamp = (Timestamp) orderData.get("date_ordered");
        Date orderDate = timestamp.toDate();
        String mSubject = "Aly in Waterland Order Number:" + orderData.get("order_id");
        Map<String, Object> deliveryAddress = (Map<String, Object>) orderData.get("delivery_address");

        // Add logo at the top
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 55, false);
        canvas.drawBitmap(scaledBitmap, 50, 60, paint);

        // Title: Invoice
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setFakeBoldText(true);
        canvas.drawText("Aly in Waterland Order Invoice", 120, 110, paint);

        // Order Information Section (Styled)
        paint.setColor(Color.rgb(106, 168, 79)); // Green color for section titles
        paint.setTextSize(18);
        paint.setFakeBoldText(false);
        canvas.drawText("Order Information", 50, 180, paint);

        // Order details
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        canvas.drawText("Date Ordered: " + orderDate, 50, 210, paint);
        canvas.drawText("Order ID: " + orderData.get("order_id"), 50, 230, paint);
        canvas.drawText("Total Amount: ₱" + orderData.get("total_amount"), 50, 250, paint);
        canvas.drawText("Mode of Payment: " + orderData.get("mode_of_payment"), 50, 270, paint);
        canvas.drawText("Is Paid: " + orderData.get("isPaid"), 50, 290, paint);

        // Delivery Address Section (Styled)
        paint.setColor(Color.rgb(106, 168, 79));
        paint.setTextSize(18);
        canvas.drawText("Delivery Address", 50, 330, paint);

        // Delivery address details
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        canvas.drawText("Address: " + deliveryAddress.get("deliveryAddress"), 50, 350, paint);

        // Customer Name and ID
        canvas.drawText("Customer Name: " + fullName, 50, 370, paint);
        canvas.drawText("Your account ID: " + orderData.get("user_id"), 50, 390, paint);

        // Additional Message Section (Styled)
        paint.setColor(Color.rgb(106, 168, 79));
        paint.setTextSize(18);
        canvas.drawText("Additional Message", 50, 430, paint);

        // Additional message from order data
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        canvas.drawText("Message: " + orderData.get("additional_message"), 50, 450, paint);

        // Thank You Message at the bottom
        paint.setColor(Color.BLACK);
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Thank you for your purchase!", 50, 520, paint);

        // Finish the page
        document.finishPage(page);

        // Save the document to the Downloads folder
        File pdfFile;
        // For Android 10+ (Scoped Storage)
        pdfFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "AlyInvoice.pdf");


        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();
            Log.d("invoiceEmail", "PDF saved to Downloads folder");

            JavaMailAPI javaMailAPI = new JavaMailAPI(this, recipientEmail, mSubject, pdfFile);
            javaMailAPI.execute();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("invoiceEmail", "Error saving PDF: "+e);
        }
    }
}
