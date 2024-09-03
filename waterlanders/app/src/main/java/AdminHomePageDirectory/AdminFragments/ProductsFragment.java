package AdminHomePageDirectory.AdminFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import AdminHomePageDirectory.Products.AddNewItem;
import AdminHomePageDirectory.Products.ItemsAdapter;
import AdminHomePageDirectory.Products.ItemsConstructor;
import DeliveryHomePageDirectory.DeliveryHomePage;
import DeliveryHomePageDirectory.onDelivery.AdapterOnDeliveryOrders;
import DeliveryHomePageDirectory.onDelivery.GetOnDeliveryOrders;

public class ProductsFragment extends Fragment {

    private ImageView productAddButton;
    private RecyclerView recyclerViewHolder;

    private List<ItemsConstructor> itemsConstructorList;
    private ItemsAdapter itemsAdapter;

    private FirebaseFirestore db;

    public ProductsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);
        initializeObjects(view);
        populateProductList();

        productAddButton.setOnClickListener(v -> {
            Intent addNewItemIntent = new Intent(getActivity(), AddNewItem.class);
            startActivity(addNewItemIntent);
        });
        return view;
    }

    private void initializeObjects(View view){
        productAddButton = view.findViewById(R.id.product_add_button);

        recyclerViewHolder = view.findViewById(R.id.recycle_view_holder);
        recyclerViewHolder.setLayoutManager(new LinearLayoutManager(getActivity()));

        itemsConstructorList = new ArrayList<>();
        itemsAdapter = new ItemsAdapter(getActivity(), itemsConstructorList);
        recyclerViewHolder.setAdapter(itemsAdapter);

        db = FirebaseFirestore.getInstance();
    }

    private void populateProductList(){
        db.collection("items").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> itemsList = task.getResult().getDocuments();

                for (DocumentSnapshot document : itemsList) {
                    ItemsConstructor items = document.toObject(ItemsConstructor.class);
                    if (items != null) {
                        items.setItem_id(document.getId());
                        itemsConstructorList.add(items);
                    }
                }

                if (itemsAdapter != null) {
                    itemsAdapter.notifyDataSetChanged();
                } else {
                    itemsAdapter = new ItemsAdapter(getActivity(), itemsConstructorList);
                    recyclerViewHolder.setAdapter(itemsAdapter);
                }
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}