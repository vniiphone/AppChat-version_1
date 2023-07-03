package com.org.chatapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;


public class ListConversationsActivity extends AppCompatActivity implements TDLibManager.Callback {
    private static final int REQUEST_CODE_CONVERSATION = 1;
    public static Client client;
    public final String TAG = "ListConversationsActivity";
    RecyclerView recyclerView_conversation_listChat;
    ImageView imgNewGroup, imgLogout;
    androidx.appcompat.widget.Toolbar toolbar;
    public static ArrayList<TdApi.Chat> chatListArray;

    private static TdApi.AuthorizationState authorizationState = null;
    private static volatile boolean haveAuthorization = false;
    private static volatile boolean needQuit = false;
    private static volatile boolean canQuit = false;

    private static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    private static boolean haveFullMainChatList = false;
    private static final String newLine = System.getProperty("line.separator");
    ListChatsAdapter listChatsAdapter;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_conversations);

        client = TDLibManager.getClient(this);
        haveFullMainChatList = false;
        chatListArray = new ArrayList<>();

        AnhXa();


        bottomNavigationView.setSelectedItemId(R.id.navigation_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_contact) {
                repplaceContacts();
                return true;
            }
            if (item.getItemId() == R.id.navigation_chat) {
                return true;
            }
            if (item.getItemId() == R.id.navigation_tool) {
                repplaceTool();
                return true;
            }
            return false;
        });

       /* imgNewGroup.setOnClickListener(v -> {
            Intent intent = new Intent(ListConversationsActivity.this, NewGroupActivity.class);
            startActivityForResult(intent, REQUEST_CODE_CONVERSATION);
        });
*/
        recyclerView_conversation_listChat.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View childView = rv.findChildViewUnder(e.getX(), e.getY());
                int position = rv.getChildAdapterPosition(childView);
                if (childView != null && e.getAction() == MotionEvent.ACTION_UP) {
                    long putId = chatListArray.get(position).id;
                    String strTitle = chatListArray.get(position).title;
                    long longIDLastMsg = chatListArray.get(position).lastMessage.id;
                    String strID = String.valueOf(putId);
                    Log.d("onCreate", "Sizee Chat arrray" + strTitle);
                    Intent intent = new Intent(ListConversationsActivity.this, ConversationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("chatIdFromPrevious", strID);
                    bundle.putString("chatTittle", strTitle);
                    bundle.putLong("lasgMsgId", longIDLastMsg);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    Log.d("getLichSuChatDebug", "LastMessage of List: " + chatListArray.get(position).lastMessage.id);
                    Log.d("ChatID_onClick", "Transition Intent " + strID);
                    Toast.makeText(ListConversationsActivity.this, "Chat ID: " + strID, Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
    }

    private void repplaceContacts() {
        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    private void repplaceTool() {
        startActivity(new Intent(getApplicationContext(), ToolActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    private void AnhXa() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        bottomNavigationView = findViewById(R.id.navigation);
//        imgLogout = findViewById(R.id.img_logout);
        recyclerView_conversation_listChat = findViewById(R.id.recyclerview_conversation);
        recyclerView_conversation_listChat.setLayoutManager(new LinearLayoutManager(this));
        listChatsAdapter = new ListChatsAdapter(chatListArray);
        recyclerView_conversation_listChat.setAdapter(listChatsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_on_conversation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.mnu_newGroup) {
            startActivity(new Intent(ListConversationsActivity.this, NewGroupActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("DebugActitivty", "onStart");
        chatListArray.clear();
        getChatDebug();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("DebugActitivty", "onRestart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("DebugActitivty", "onResume");


    }


    public ArrayList<TdApi.Chat> getChatDebug() {
        Log.d(TAG, "getChats: " + this.chatListArray.size());
        client.send(new TdApi.GetChats(new TdApi.ChatList() {
            @Override
            public int getConstructor() {
                return TdApi.ChatListMain.CONSTRUCTOR;
            }
        }, 30), this::onResult);
        return chatListArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CONVERSATION) {
            if (resultCode == Activity.RESULT_OK) {
                // Tải lại danh sách trò chuyện
                chatListArray.clear();
                getChatDebug();
            }
        }
    }


    @Override
    public void onResult(TdApi.Object object) {
        Log.d("onResult->", "OnResult: " + object.toString());
        Log.d("debugPhoto", "Object nhận được: " + object);
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                Log.d("getChatDebug", "Error.CONSTRUCTOR: " + object);
                break;
            case TdApi.Chats.CONSTRUCTOR:
                long chatIDs[] = ((TdApi.Chats) object).chatIds;
                Log.d("getChatDebug", "Chats.CONSTRUCTOR: " + object + " Chat size: " + Arrays.stream(chatIDs).count());
                for (long chatID : chatIDs) {
                    Log.d("getChatDebug", "onResult: " + chatID);
                    client.send(new TdApi.GetChat(chatID), this::onResult);
                }
                Log.d("getChatDebug", "onResult: " + chatIDs.toString());
                break;
            case TdApi.Chat.CONSTRUCTOR:
                Log.d("getChatDebug", "Chat.CONSTRUCTOR: " + object);
                TdApi.Chat myChat = ((TdApi.Chat) object);
                chatListArray.add(myChat);
                client.send(new TdApi.DownloadFile(myChat.photo.small.id, 1, 0, 0, false), this::onResult);
                runOnUiThread(() -> listChatsAdapter.refresh());
                Log.d("getChatDebug", "Chat.CONSTRUCTOR: " + chatListArray.size());
                break;
            case TdApi.UpdateUser.CONSTRUCTOR:
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                TdApi.User user = updateUser.user;
            case TdApi.Ok.CONSTRUCTOR:
                Log.d("getChatDebug", "Ok.CONSTRUCTOR:: " + object);
                break;
            case TdApi.LogOut.CONSTRUCTOR:
                Log.d("getChatDebug", "LogOut.CONSTRUCTOR " + object);
                haveAuthorization = false;
                client.close();
                finish();
                Intent intent = new Intent(ListConversationsActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                Log.d("getChatDebug", "Logging out");
                finish();
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                Log.d("getChatDebug", "Closing");
                finish();
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                Log.d("getChatDebug", "Closed");
                if (!needQuit) {
                    client = Client.create(this, null, null); // recreate client after previous has closed
                } else {
                    canQuit = true;
                }
                break;
            default:
                Log.d("DeBugNull", "Object Default: " + object);
        }
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {
        switch (update.getConstructor()) {
            case TdApi.UpdateNewMessage.CONSTRUCTOR:
                // Xử lý cập nhật tin nhắn mới
                TdApi.UpdateNewMessage updateNewMessage = (TdApi.UpdateNewMessage) update;
                TdApi.Message message = updateNewMessage.message;
                Log.d("NhanTinUpdate", message.content.toString());
                runOnUiThread(() -> listChatsAdapter.refresh());
                Toast.makeText(this, "Tin nhắn: " + message.content, Toast.LENGTH_SHORT).show();
                // ...
                break;
            case TdApi.UpdateChatLastMessage.CONSTRUCTOR:
                // Xử lý cập nhật tin nhắn cuối cùng trong cuộc trò chuyện
                runOnUiThread(() -> listChatsAdapter.refresh());
                TdApi.UpdateChatLastMessage updateChatLastMessage = (TdApi.UpdateChatLastMessage) update;
                // ...
                break;
            case TdApi.UpdateChatReadInbox.CONSTRUCTOR:
                // Xử lý cập nhật trạng thái tin nhắn đã đọc trong cuộc trò chuyện
                TdApi.UpdateChatReadInbox updateChatReadInbox = (TdApi.UpdateChatReadInbox) update;
                // ...
                break;
            default:
                Log.d("TDLibManager", "Default " + update);
        }
    }

    @Override
    public void onMessageReceived(TdApi.Message message) {

    }

    @Override
    public void onMessageDeleted(long chatId, long messageId) {

    }


    public class ExceptionHandler implements Client.ExceptionHandler {
        @Override
        public void onException(Throwable e) {
            e.printStackTrace();
        }
    }


    private static class OrderedChat implements Comparable<OrderedChat> {
        final long chatId;
        final TdApi.ChatPosition position;

        OrderedChat(long chatId, TdApi.ChatPosition position) {
            this.chatId = chatId;
            this.position = position;
        }

        @Override
        public int compareTo(OrderedChat o) {
            if (this.position.order != o.position.order) {
                return o.position.order < this.position.order ? -1 : 1;
            }
            if (this.chatId != o.chatId) {
                return o.chatId < this.chatId ? -1 : 1;
            }
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            OrderedChat o = (OrderedChat) obj;
            return this.chatId == o.chatId && this.position.order == o.position.order;
        }

    }

    @Override
    public void onSetTdlibParametersSuccess() {
        Log.d("onSetTdlibParametersSuccess", "Success");
    }

    @Override
    public void onSetTdlibParametersError() {
        Log.d("onSetTdlibParametersError", "Error");
    }

}


class ListChatsAdapter extends RecyclerView.Adapter<ListChatsAdapter.ViewHolder> {

    private final List<TdApi.Chat> chatList;
    private OnItemClickListener listener;

    ListChatsAdapter(List<TdApi.Chat> mChatsList) {
        this.chatList = mChatsList;
    }


    @NonNull
    @Override
    public ListChatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_chats, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ListChatsAdapter.ViewHolder holder, int position) {

        TdApi.Chat chat = chatList.get(position);
        long timestamp = chatList.get(position).lastMessage.date;

// Convert the timestamp to an Instant
        Instant instant = Instant.ofEpochSecond(timestamp);

// Convert the Instant to a LocalDateTime in the Vietnam time zone
        ZoneId vietnamTimeZone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, vietnamTimeZone);

// Format the LocalDateTime as a String
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedDateTime = localDateTime.format(formatter);

        holder.txt_title.setText(chat.title);
//        holder.txt_last_message_content.setText(((TdApi.MessageContent) chat.lastMessage.content));

        if (chat.lastMessage.content instanceof TdApi.MessageText) {
            //Kiểm tra có phải kiểu content không trong hàm onBindViewHolder
            TdApi.MessageText messageText = (TdApi.MessageText) chat.lastMessage.content;
            holder.txt_last_message_content.setText(messageText.text.text);

            String lastMessageContent = messageText.text.text.toString();
            int maxLength = 29; // Giới hạn độ dài của nội dung

            if (lastMessageContent.length() > maxLength) {
                lastMessageContent = lastMessageContent.substring(0, maxLength) + "...";
            }

            holder.txt_last_message_content.setText(lastMessageContent);

        } else {
            // Xử lý cho các kiểu nội dung tin nhắn khác
            holder.txt_last_message_content.setText("Not instanceof");
        }
        holder.txt_time.setText(formattedDateTime);
        if (chatList.get(position).photo != null) {
            File imgFile = new File(chatList.get(position).photo.small.local.path);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.img_avatar.setImageBitmap(myBitmap);
            }
        } else {
            holder.img_avatar.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    private boolean isChatListEmpty() {
        return chatList == null || chatList.isEmpty();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txt_title, txt_time, txt_last_message_content;
        private ImageView img_avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_title = itemView.findViewById(R.id.txt_chats_title_item);
            txt_time = itemView.findViewById(R.id.txt_chats_time_item);
            txt_last_message_content = itemView.findViewById(R.id.txt_chats_last_message_content_item);
            img_avatar = itemView.findViewById(R.id.img_chats_avt_item);
        }
    }
}

