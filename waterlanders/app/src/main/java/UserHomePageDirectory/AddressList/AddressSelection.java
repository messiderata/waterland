package UserHomePageDirectory.AddressList;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.api.Distribution;

import java.util.ArrayList;
import java.util.List;

import Handler.StatusBarUtil;
import UserHomePageDirectory.MainDashboardUser;
import UserHomePageDirectory.OrderConfirmation;

public class AddressSelection extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddressAdapter contactAdapter;
    private List<Contact> contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_selection);
        StatusBarUtil.setStatusBarColor(this, R.color.button_bg);

        ImageView backImage = findViewById(R.id.btn_back);
        CardView continueButton = findViewById(R.id.continue_button);
        LinearLayout newAddressButton = findViewById(R.id.add_new_address_button);

        backImage.setOnClickListener(view -> {
            finish();
        });
        continueButton.setOnClickListener(view -> {
            finish();
        });

        newAddressButton.setOnClickListener(view -> {
            Intent backIntent = new Intent(AddressSelection.this, AddressInput.class);
            startActivity(backIntent);
//            finish();
        });


        recyclerView = findViewById(R.id.address_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        contactList = new ArrayList<>();
        contactList.add(new Contact("John Doe", "+123456789", "123 Main St"));


        contactAdapter = new AddressAdapter(contactList);
        recyclerView.setAdapter(contactAdapter);
    }
}