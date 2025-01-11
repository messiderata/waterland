package AdminHomePageDirectory.AdminFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.waterlanders.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import AdminHomePageDirectory.Chats.ChatUsersAdapter;
import AdminHomePageDirectory.Chats.ChatUsersConstructor;
import AdminHomePageDirectory.Orders.Utils.PendingOrders.PendingOrdersConstructor;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<ChatUsersConstructor> chatUsersConstructors;
    private ChatUsersAdapter chatUsersAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat, container, false);
        initializeObjects(view);
        populateChatUserContainer();
        return view;
    }

    private void initializeObjects(View view){
        recyclerView = view.findViewById(R.id.chat_users_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        chatUsersConstructors = new ArrayList<>();
        chatUsersAdapter = new ChatUsersAdapter(getActivity(), chatUsersConstructors);
        recyclerView.setAdapter(chatUsersAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void populateChatUserContainer(){
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                List<DocumentSnapshot> userList = task.getResult().getDocuments();
                List<ChatUsersConstructor> chatUsersList = new ArrayList<>();

                for (DocumentSnapshot document : userList){
                    String currentUserID = auth.getCurrentUser().getUid();

                    Log.d("ChatFragment", "currentUserID: " + currentUserID);
                    Log.d("ChatFragment", "document.getId(): " + document.getId());

                    if (!document.getId().equals(currentUserID) && !document.getId().equals("354PEdF63GdC4mPXRIzCgriDZIU2")) {
                        ChatUsersConstructor chatUsers = document.toObject(ChatUsersConstructor.class);
                        if (chatUsers != null){
                            chatUsers.setUserID(document.getId());

                            // Handle 'unreadMessagesDate' field
                            String unreadMessagesDateStr = document.getString("unreadMessagesDate");
                            if (unreadMessagesDateStr != null && !unreadMessagesDateStr.isEmpty()) {
                                chatUsers.setUnreadMessagesDate(unreadMessagesDateStr);
                            } else {
                                chatUsers.setUnreadMessagesDate(null);
                            }

                            chatUsersList.add(chatUsers);
                            Log.d("ChatFragment", "Document Data: " + document.getData());
                        }
                    }
                }

                chatUsersList.sort((user1, user2) -> {
                    String date1 = user1.getUnreadMessagesDate();
                    String date2 = user2.getUnreadMessagesDate();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date1format = null;
                    Date date2format = null;

                    try {
                        if (date1 != null && !date1.isEmpty()) {
                            date1format = sdf.parse(date1);
                        }
                        if (date2 != null && !date2.isEmpty()) {
                            date2format = sdf.parse(date2);
                        }
                    } catch (ParseException e) {
                        Log.e("chat fragment", "error parsing dates for sorting: " + e);
                    }

                    if (date1format == null && date2format == null) return 0;
                    if (date1format == null) return 1;
                    if (date2format == null) return -1;
                    return date2format.compareTo(date1format);
                });

                chatUsersConstructors.clear();
                chatUsersConstructors.addAll(chatUsersList);
                chatUsersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Failed to retrieve items data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}