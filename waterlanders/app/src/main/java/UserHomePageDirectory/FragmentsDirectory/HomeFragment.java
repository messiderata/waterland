package UserHomePageDirectory.FragmentsDirectory;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import AdminHomePageDirectory.Chats.ChatUsersConstructor;
import LoginDirectory.Login;
import UserHomePageDirectory.HomeFragmentUtils.AddedItems;
import UserHomePageDirectory.HomeFragmentUtils.GetItems;
import UserHomePageDirectory.HomeFragmentUtils.ItemAdapter;
import UserHomePageDirectory.HomeFragmentUtils.OrderConfirmation;
import UserHomePageDirectory.HomeFragmentUtils.OrderReceipt;

public class HomeFragment extends Fragment implements ItemAdapter.OnTotalAmountChangeListener {

    private RecyclerView recyclerView;
    private MaterialButton orderButton;

    private ItemAdapter itemAdapter;
    private List<GetItems> itemsList;
    private FirebaseFirestore db;
    private static final String TAG = "UserHomePage";
    private TextView textTotalAmount;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeObjects(view);
        getItemsFromFireStore();

        // if the order button is clicked
        // then pass all the items and total price
        // to the order confirmation to check if the items
        // and delivery address details as well as payment methods are correct
        // if the user's email address is not verified, they cant order
        orderButton.setOnClickListener(v -> {
            checkVerifiedUser(auth, isVerified -> {
                if (!isVerified) {
                    Toast.makeText(getContext(), "Email is not verified. Please verify your email.", Toast.LENGTH_SHORT).show();
                } else {
                    updateAccountStatus();
                    checkUserAccountStatus(isRejected -> {
                        if (!isRejected){
                            AddedItems addedItems = itemAdapter.getAddedItems();
                            addedItems.logCartItems();

                            if (!addedItems.getCartItems().isEmpty()) {
                                Intent intent = new Intent(getContext(), OrderConfirmation.class);
                                intent.putExtra("addedItems", addedItems);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Select an item first.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Account status is rejected. Placing order is restricted.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

        });

        return view;
    }

    private void initializeObjects(View view){
        recyclerView = view.findViewById(R.id.rv_userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // initialize recycler view and itemAdapter to display the items
        // from the database
        //
        // these adapter is responsible to update the current price
        // of all items selected of the customer
        itemsList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemsList, getContext());
        itemAdapter.setOnTotalAmountChangeListener(this);
        recyclerView.setAdapter(itemAdapter);

        textTotalAmount = view.findViewById(R.id.total_amount);
        orderButton = view.findViewById(R.id.placeOrderButton);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getItemsFromFireStore() {
        db.collection("items")
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e(TAG, "Error fetching data", error);
                    return;
                }
                if (value != null) {
                    List<GetItems> newItemsList = new ArrayList<>();
                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        if (!snapshot.getId().equals("test_id")){
                            GetItems items = snapshot.toObject(GetItems.class);
                            if (items != null) {
                                items.setItem_id(snapshot.getId());
                                Log.d(TAG, "--> items: " + items);
                                newItemsList.add(items);
                            }
                        }
                    }
                    itemsList.clear();
                    itemsList.addAll(newItemsList);
                    itemAdapter.notifyDataSetChanged();
                }
            });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTotalAmountChange(int totalAmount, AddedItems addedItems) {
        // Implement your logic to handle total amount change
        textTotalAmount.setText("₱" + totalAmount);
    }

    private void checkVerifiedUser(FirebaseAuth auth, OnEmailVerifiedCallback callback) {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            user.reload()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onEmailVerified(user.isEmailVerified());
                    } else {
                        Toast.makeText(getContext(), "Failed to check email verification status.", Toast.LENGTH_SHORT).show();
                        callback.onEmailVerified(false);
                    }
                });
        } else {
            callback.onEmailVerified(false);
        }
    }

    private void checkUserAccountStatus(OnEmailVerifiedCallback callback) {
        String currentUserID = auth.getCurrentUser().getUid();
        db.collection("users")
            .document(currentUserID)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()){
                    String accountStatus = documentSnapshot.getString("accountStatus");
                    callback.onEmailVerified(accountStatus.equals("REJECTED"));
                }
            }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                    callback.onEmailVerified(true);
            });
    }

    interface OnEmailVerifiedCallback {
        void onEmailVerified(boolean isVerified);
    }

    private void updateAccountStatus(){
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                .document(userId)
                .update("verificationStatus", "VERIFIED")
                .addOnSuccessListener(aVoid -> Log.d("Home Fragment", "Verification status updated to VERIFIED."))
                .addOnFailureListener(e -> Log.d("Home Fragment", "Failed to update account verification: " + e.getMessage()));
        } else {
            Log.e("Home Fragment", "No user is logged in.");
        }
    }
}