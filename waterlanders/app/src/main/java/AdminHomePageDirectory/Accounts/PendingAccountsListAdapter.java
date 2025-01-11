package AdminHomePageDirectory.Accounts;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import AdminHomePageDirectory.Chats.ChatUsersConstructor;
import UserHomePageDirectory.MainDashboardUser;

public class PendingAccountsListAdapter extends RecyclerView.Adapter<PendingAccountsListAdapter.PendingAccountsListAdapterViewHolder> {
    Context context;
    List<ChatUsersConstructor> chatUsersConstructors;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PendingAccountsListAdapter(Context context, List<ChatUsersConstructor> chatUsersConstructors) {
        this.context = context;
        this.chatUsersConstructors = chatUsersConstructors;
    }

    public static class PendingAccountsListAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView userIMG;
        TextView userFullName;
        TextView username;
        TextView userID;
        Button reviewButton;

        public PendingAccountsListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            userIMG = itemView.findViewById(R.id.user_img);
            userFullName = itemView.findViewById(R.id.user_fullname);
            username = itemView.findViewById(R.id.username);
            userID = itemView.findViewById(R.id.user_id);
            reviewButton = itemView.findViewById(R.id.review_button);
        }
    }

    @NonNull
    @Override
    public PendingAccountsListAdapter.PendingAccountsListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_pending_accounts_list, parent, false);
        return new PendingAccountsListAdapter.PendingAccountsListAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return chatUsersConstructors.size();
    }

    @Override
    public void onBindViewHolder(@NonNull PendingAccountsListAdapter.PendingAccountsListAdapterViewHolder holder, int position) {
        ChatUsersConstructor usersConstructor = chatUsersConstructors.get(position);

        // set text values
        holder.userFullName.setText(usersConstructor.getFullName());
        holder.username.setText(usersConstructor.getUsername());

        String userIDString = usersConstructor.getUserID();
        holder.userID.setText(userIDString);

        holder.reviewButton.setOnClickListener(view -> {
            Intent pendingAccountDetailsIntent = new Intent(context, PendingAccountsListDetails.class);
            pendingAccountDetailsIntent.putExtra("current_user", usersConstructor);
            context.startActivity(pendingAccountDetailsIntent);
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });
    }
}
