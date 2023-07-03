package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;
import com.org.chatapp.Utils.Utils;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MainActivity extends AppCompatActivity implements TDLibManager.Callback {
    private static Client client;
    boolean haveAuthorization = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TdApi.GetAuthorizationState AuthState = new TdApi.GetAuthorizationState();
        client = TDLibManager.getClient(this);
        client.send(AuthState, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "MainActivity");
//        client = TDLibManager.getClient(this);
//        client.send(AuthState, this::onResult);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        client = TDLibManager.getClient(this);
//        client.send(AuthState, this::onResult);
    }

    @Override
    public void onSetTdlibParametersSuccess() {
        // Xử lý khi SetTdlibParameters thành công
        Log.d("MainActivity", "onSetTdlibParametersSuccess");
    }

    @Override
    public void onSetTdlibParametersError() {
        // Xử lý khi SetTdlibParameters gặp lỗi
        Log.d("MainActivity", "onSetTdlibParametersError");
    }

    @Override
    public void onResult(TdApi.Object object) {
        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
            onAuthStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
        } else {
            Log.d("MainActivity", "else Onresult: " + object);
            switch (object.getConstructor()) {
                case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                    Log.d("DebugLogin", "Main: AuthorizationStateWaitPhoneNumber");
//                client.send(new TdApi.SetAuthenticationPhoneNumber("+917418189531", authenticationSettings), this);
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                    break;
                case TdApi.Ok.CONSTRUCTOR:
                    Log.d("onResultMainActivity", "onResult: Ok.CONSTRUCTOR");
                    client.send(new TdApi.GetAuthorizationState(), this);
                    break;
                case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                    Log.d("onResultMainActivity", "onResult: AuthorizationStateWaitTdlibParameters");
                    TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
                    // Gửi yêu cầu khởi tạo TDLib parameters
                    TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                    parameters.apiId = 25729812;
                    parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                    parameters.databaseDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
                    parameters.useMessageDatabase = true;
                    parameters.useSecretChats = true;
                    parameters.systemLanguageCode = "en";
                    parameters.deviceModel = "Pixel 6";
                    authStateRequest.systemVersion = "12.0";
                    parameters.applicationVersion = "0.0.2";
                    authStateRequest.enableStorageOptimizer = true;
                    client.send(new TdApi.SetTdlibParameters(parameters), this);
                    break;
                case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                    Log.d("onResultMainActivity", "onResult: AuthorizationStateWaitEncryptionKey");
                    client.send(new TdApi.CheckDatabaseEncryptionKey(), this);
                    TdApi.GetAuthorizationState AuthState = new TdApi.GetAuthorizationState();
                    client.send(AuthState, this);
                    break;
                case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                    // Xử lý trạng thái chờ mã xác thực
                    Log.d("Main", "trạng thái chờ mã xác thực");
                    Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(authIntent);
                    finish();
                    break;
                case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                    Intent conversationIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                    conversationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Log.d("onResultMainActivity", "onResult: Registered...");
                    startActivity(conversationIntent);
                    finish();
                case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                    haveAuthorization = false;
                    TokenStorage tokenStoragelogout = new TokenStorage(MainActivity.this);
                    tokenStoragelogout.clearAccessToken();
                    System.out.println("Logging out");
                    TdApi.GetAuthorizationState AuthState2 = new TdApi.GetAuthorizationState();
                    client.send(AuthState2, this);
                    break;
                case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                    haveAuthorization = false;
                    TokenStorage tokenStorageGet = new TokenStorage(MainActivity.this);
                    if (tokenStorageGet.hasAccessToken()) {
                        String accessTokenGet = tokenStorageGet.getAccessToken();
                        // Sử dụng accessToken để thực hiện các yêu cầu xác thực hoặc thao tác khác
                        Intent reLoginIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                        startActivity(reLoginIntent);
                        finish();
                    } else {
                        Intent reLoginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(reLoginIntent);
                        finish();
                        break;
                        // Người dùng chưa đăng nhập, chuyển hướng đến màn hình đăng nhập hoặc thực hiện các xử lý tương ứng
                    }
               /* Intent openMain = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(openMain);*/
                    break;
            }

        }
      /*  switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                Log.d("onResultMainActivity", "onResult: UpdateAuthorizationState.CONSTRUCTOR");
                onAuthStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                break;
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                Log.d("onResultMainActivity", "onResult: AuthorizationStateWaitTdlibParameters");
                TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
                // Gửi yêu cầu khởi tạo TDLib parameters
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.apiId = 25729812;
                parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                parameters.databaseDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Pixel 6";
                authStateRequest.systemVersion = "12.0";
                parameters.applicationVersion = "0.0.2";
                authStateRequest.enableStorageOptimizer = true;
                client.send(new TdApi.SetTdlibParameters(parameters), this);
                break;


            case TdApi.Ok.CONSTRUCTOR:
                Log.d("onResultMainActivity", "onResult: UpdateAuthorizationState.CONSTRUCTOR");
                client.send(new TdApi.GetAuthorizationState(), this::onResult);
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                Intent conversationIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                //conversationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d("onResultMainActivity", "onResult: Registered...");
                startActivity(conversationIntent);
                finish();
            default:
                Log.d("onResultMainActivity", "Default: " + object);
                // Todo: Implement methods to save or get Token
//                authenticationSettings.authenticationTokens = token;
//            client.send(new TdApi.SetAuthenticationPhoneNumber("+917418189531", ), this);
//            break;
        }*/
    }

    private void onAuthStateUpdated(TdApi.AuthorizationState authorizationState) {

        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                Log.d("onResultMainActivity", "onResult: AuthorizationStateWaitTdlibParameters");
                TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
                // Gửi yêu cầu khởi tạo TDLib parameters
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                parameters.apiId = 25729812;
                parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                parameters.databaseDirectory = getApplicationContext().getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Pixel 6";
                authStateRequest.systemVersion = "12.0";
                parameters.applicationVersion = "0.0.2";
                authStateRequest.enableStorageOptimizer = true;
                client.send(new TdApi.SetTdlibParameters(parameters), this);
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                // Xử lý trạng thái chờ khóa mã hóa
                Log.d("Main", "AuthorizationStateWaitEncryptionKey");
                client.send(new TdApi.CheckDatabaseEncryptionKey(), this);
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                // Đã xác thực, chuyển hướng đến LoginActivity
                Log.d("Main", "chuyển hướng đến LoginActivity");
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR:
                // Xử lý trạng thái chờ mã xác thực
                Log.d("Main", "trạng thái chờ mã xác thực");
                Intent authIntent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(authIntent);
                finish();
                break;
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                Log.d("Main", "Gọi ListConversationsActivity: ");
                Intent conversationIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                startActivity(conversationIntent);
                finish();
                break;
            case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
                Intent RegistrationIntent = new Intent(MainActivity.this, RegistrationActivity.class);
                startActivity(RegistrationIntent);
                finish();
                break;
            }
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                haveAuthorization = false;
                TokenStorage tokenStoragelogout = new TokenStorage(MainActivity.this);
                tokenStoragelogout.clearAccessToken();
                System.out.println("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                haveAuthorization = false;
                TokenStorage tokenStorageGet = new TokenStorage(MainActivity.this);
                if (tokenStorageGet.hasAccessToken()) {
                    String accessTokenGet = tokenStorageGet.getAccessToken();
                    // Sử dụng accessToken để thực hiện các yêu cầu xác thực hoặc thao tác khác
                    Intent reLoginIntent = new Intent(MainActivity.this, ListConversationsActivity.class);
                    startActivity(reLoginIntent);
                    finish();
                } else {
                    Intent reLoginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(reLoginIntent);
                    finish();
                    break;
                    // Người dùng chưa đăng nhập, chuyển hướng đến màn hình đăng nhập hoặc thực hiện các xử lý tương ứng
                }
               /* Intent openMain = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(openMain);*/
                break;
            default:
                System.err.println("Unsupported authorization state:" + authorizationState);
        }

    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {
        Log.d("onUpdatesReceived", "Update: " + update);
    }

    @Override
    public void onMessageReceived(TdApi.Message message) {

    }

    @Override
    public void onMessageDeleted(long chatId, long messageId) {

    }

}

