package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;

public class ToolActivity extends AppCompatActivity implements TDLibManager.Callback {

    private Client client;
    TextView tv_username, tv_lastName, tv_firstName, tv_status;
    ImageView img_logout, img_avt_profile;
    BottomNavigationView bottomNavigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool);

        client = TDLibManager.getClient(this);
        getUser();
        anhXa();
        bottomNavigationView.setSelectedItemId(R.id.navigation_tool);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_chat) {
                repplaceConversations();
                return true;
            }
            if (item.getItemId() == R.id.navigation_contact) {
                repplaceContact();
                return true;
            }
            if (item.getItemId() == R.id.navigation_tool) {
                return true;
            }
            return false;
        });

        img_logout.setOnClickListener(img -> {
            dangXuat();
        });

    }

    private void repplaceConversations() {
        startActivity(new Intent(getApplicationContext(), ListConversationsActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    private void repplaceContact() {
        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ToolActivity", "Onstart");
    }

    public void getUser() {
        client.send(new TdApi.GetMe(), this::onResult);
    }

    public void dangXuat() {
        client.send(new TdApi.LogOut(), this::onResult);
    }

    public void anhXa() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tv_firstName = findViewById(R.id.tv_profile_first_name);
        tv_status = findViewById(R.id.tv_profile_status);
        tv_lastName = findViewById(R.id.tv_profile_lastName);
        img_logout = findViewById(R.id.img_profile_logout);
        img_avt_profile = findViewById(R.id.img_profilt_avt);
        tv_username = findViewById(R.id.tv_profile_username);
        bottomNavigationView = findViewById(R.id.navigation);
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Ok.CONSTRUCTOR:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                TokenStorage tokenStorage = new TokenStorage(this);
                tokenStorage.clearAccessToken();
                finish();
                break;
            case TdApi.Error.CONSTRUCTOR:
                Log.d("Tool-->Onresult", "Object: Error" + object);
                break;
            case TdApi.User.CONSTRUCTOR:
                TdApi.User user = (TdApi.User) object;
                tv_firstName.setText(user.firstName);
                tv_lastName.setText(user.lastName);
                tv_username.setText("@" + user.username);
                if (user.profilePhoto.small.local.path != null) {
                    File imgFile = new File(user.profilePhoto.small.local.path);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        img_avt_profile.setImageBitmap(myBitmap);
                    }
                } else {
                    img_avt_profile.setImageResource(R.drawable.unknowavt);
                }
                Log.d("Tool-->Onresult", "UserFullInfo: " + user.username);
                break;
            default:
                Log.d("Tool-->Onresult", "Default: ");

        }
    }

    @Override
    public void onSetTdlibParametersSuccess() {

    }

    @Override
    public void onSetTdlibParametersError() {

    }


    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {

    }

    @Override
    public void onMessageReceived(TdApi.Message message) {

    }

    @Override
    public void onMessageDeleted(long chatId, long messageId) {

    }
}