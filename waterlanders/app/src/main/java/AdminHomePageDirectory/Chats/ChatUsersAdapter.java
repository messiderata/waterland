package AdminHomePageDirectory.Chats;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.waterlanders.R;

import java.util.List;

import UserHomePageDirectory.FragmentsDirectory.ChatActivity;


public class ChatUsersAdapter extends RecyclerView.Adapter<ChatUsersAdapter.ChatUsersAdapterViewHolder> {
    Context context;
    List<ChatUsersConstructor> chatUsersConstructors;

    public ChatUsersAdapter(Context context, List<ChatUsersConstructor> chatUsersConstructors) {
        this.context = context;
        this.chatUsersConstructors = chatUsersConstructors;
    }

    public static class ChatUsersAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView userIMG;
        TextView userFullName;
        TextView username;
        TextView userID;
        TextView chatCount;

        public ChatUsersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            userIMG = itemView.findViewById(R.id.user_img);
            userFullName = itemView.findViewById(R.id.user_fullname);
            username = itemView.findViewById(R.id.username);
            userID = itemView.findViewById(R.id.user_id);
            chatCount = itemView.findViewById(R.id.chat_count);
        }
    }

    @NonNull
    @Override
    public ChatUsersAdapter.ChatUsersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_users, parent, false);
        return new ChatUsersAdapter.ChatUsersAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return chatUsersConstructors.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUsersAdapter.ChatUsersAdapterViewHolder holder, int position) {
        ChatUsersConstructor usersConstructor = chatUsersConstructors.get(position);
        Log.d("ChatUsersAdapter", "Binding user: " + usersConstructor.getFullName());

        // set text values
        holder.userFullName.setText(usersConstructor.getFullName());
        holder.username.setText(usersConstructor.getUsername());
        holder.userID.setText(usersConstructor.getUserID());

        // check if has unread message
        Long unreadMessagesFromUserToAdmin = usersConstructor.getUnreadMessagesFromUserToAdmin();
        if (unreadMessagesFromUserToAdmin > 0){
            holder.chatCount.setText(String.valueOf(unreadMessagesFromUserToAdmin));
            holder.chatCount.setVisibility(View.VISIBLE);
        } else {
            holder.chatCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("receiver_id", usersConstructor.getUserID());
            context.startActivity(chatIntent);
        });
    }

}
