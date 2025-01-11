package AdminHomePageDirectory.Accounts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;

import java.util.List;

import AdminHomePageDirectory.Chats.ChatUsersAdapter;
import AdminHomePageDirectory.Chats.ChatUsersConstructor;

public class AccountStatusListAdapter extends RecyclerView.Adapter<AccountStatusListAdapter.AccountStatusListAdapterViewHolder> {
    Context context;
    List<ChatUsersConstructor> chatUsersConstructors;

    public AccountStatusListAdapter(Context context, List<ChatUsersConstructor> chatUsersConstructors) {
        this.context = context;
        this.chatUsersConstructors = chatUsersConstructors;
    }

    public static class AccountStatusListAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView userIMG;
        TextView userFullName;
        TextView username;
        TextView userID;
        TextView accountStatus;

        public AccountStatusListAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            userIMG = itemView.findViewById(R.id.user_img);
            userFullName = itemView.findViewById(R.id.user_fullname);
            username = itemView.findViewById(R.id.username);
            userID = itemView.findViewById(R.id.user_id);
            accountStatus = itemView.findViewById(R.id.account_status);
        }
    }

    @NonNull
    @Override
    public AccountStatusListAdapter.AccountStatusListAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_account_status_list, parent, false);
        return new AccountStatusListAdapter.AccountStatusListAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return chatUsersConstructors.size();
    }

    @Override
    public void onBindViewHolder(@NonNull AccountStatusListAdapter.AccountStatusListAdapterViewHolder holder, int position) {
        ChatUsersConstructor usersConstructor = chatUsersConstructors.get(position);

        // set text values
        holder.userFullName.setText(usersConstructor.getFullName());
        holder.username.setText(usersConstructor.getUsername());
        holder.userID.setText(usersConstructor.getUserID());

        Log.d("AccountStatusList", "Binding user: " + usersConstructor.getFullName());

        holder.accountStatus.setText(usersConstructor.getAccountStatus());
        if ("PENDING".equals(usersConstructor.getAccountStatus())) {
            holder.accountStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary_color));
        } else if ("REJECTED".equals(usersConstructor.getAccountStatus())){
            holder.accountStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.accountStatus.setTextColor(ContextCompat.getColor(context, R.color.button_bg));
        }
    }
}
