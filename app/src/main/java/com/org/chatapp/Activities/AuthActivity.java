package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

public class AuthActivity extends AppCompatActivity implements TDLibManager.Callback {
    private Client client;
    private Button btn_checkauth;
    private EditText edt_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        AnhXa();
// Khởi tạo TDLib client
        // client = Client.create(this, this, this);
        client = TDLibManager.getClient(this);
        btn_checkauth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = edt_code.getText().toString();
                if (!code.equals("")) {
                    TdApi.CheckAuthenticationCode authCode = new TdApi.CheckAuthenticationCode(code);
                    //client = Client.create(AuthActivity.this,null,null);
                    client.send(authCode, AuthActivity.this::onResult);
                }
            }
        });

        Client.ResultHandler updateHandler = object -> System.out.println("Receive Update: " + object.toString());
        Client.ExceptionHandler updateExceptionHandler = new Client.ExceptionHandler() {
            @Override
            public void onException(Throwable e) {
                System.out.println("Receive ExceptionHandler: " + e.getMessage());
            }
        };

        Client.ExceptionHandler defaultExceptionHandler = new Client.ExceptionHandler() {
            @Override
            public void onException(Throwable e) {
                System.out.println("Receive defaultExceptionHandler: " + e.getMessage());
            }
        };

        Client client = Client.create(updateHandler, updateExceptionHandler, defaultExceptionHandler);
        // Kiểm tra xem đối tượng Client đã được tạo thành công hay không
        if (client != null) {
            System.out.println("Client được tạo thành công.");
            // Thực hiện các hoạt động khác với đối tượng Client
        } else {
            System.out.println("Không thể tạo Client.");
            // Xử lý trường hợp không thể tạo Client
        }
    }

    public void AnhXa() {
        edt_code = findViewById(R.id.authCode);
        btn_checkauth = findViewById(R.id.checkBtn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                switch (((TdApi.UpdateAuthorizationState) object).authorizationState.getConstructor()) {
                    case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                        Log.d("AuthActivity", "onResult: TDlibParams");
                        TdApi.TdlibParameters authStateRequest = new TdApi.TdlibParameters();
                        // Gửi yêu cầu khởi tạo TDLib parameters
                        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                        parameters.apiId = 25729812;
                        parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                        parameters.databaseDirectory = getFilesDir().getAbsolutePath();
                        parameters.useMessageDatabase = true;
                        parameters.useSecretChats = true;
                        parameters.systemLanguageCode = "en";
                        parameters.deviceModel = "Pixel";
                        authStateRequest.systemVersion = "12.0";
                        parameters.applicationVersion = "0.0.1";
                        authStateRequest.enableStorageOptimizer = true;
                        client.send(new TdApi.SetTdlibParameters(parameters), this::onResult);
                        break;
                    case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                        client.send(new TdApi.CheckDatabaseEncryptionKey(), this::onResult);
                        break;
                    case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                        TokenStorage tokenStorage = new TokenStorage(this);
                        Intent conversationIntent = new Intent(AuthActivity.this, MainActivity.class);
                        conversationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(conversationIntent);
                        break;
                }
            case TdApi.UpdateConnectionState.CONSTRUCTOR:
                switch (((TdApi.UpdateConnectionState) object).state.getConstructor()) {
                    case TdApi.ConnectionStateReady.CONSTRUCTOR:
                        Log.d("AuthActivity", "onResult: Successfully loginned! ");
                        Toast.makeText(AuthActivity.this, "Successfully loginned!", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
       /* if (object instanceof TdApi.UpdateAuthorizationState) {
            TdApi.AuthorizationState authorizationState = ((TdApi.UpdateAuthorizationState) object).authorizationState;
            if (authorizationState instanceof TdApi.AuthorizationStateWaitTdlibParameters) {
                TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
                // Set TDLib parameters
                parameters.apiId = 25729812;
                parameters.apiHash = "8285aa99a082199b9884819998f28c94";
                parameters.databaseDirectory = getFilesDir().getAbsolutePath();
                parameters.useMessageDatabase = true;
                parameters.useSecretChats = true;
                parameters.systemLanguageCode = "en";
                parameters.deviceModel = "Pixel";
                parameters.systemVersion = "12.0";
                parameters.applicationVersion = "0.0.1";
                parameters.enableStorageOptimizer = true;

                client.send(new TdApi.SetTdlibParameters(parameters), this::onResult);
            } else if (authorizationState instanceof TdApi.AuthorizationStateReady) {
                // User is authorized successfully
                Intent conversationIntent = new Intent(AuthActivity.this, MainActivity.class);
                conversationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(conversationIntent);
            }
        } else if (object instanceof TdApi.UpdateConnectionState) {
            TdApi.ConnectionState connectionState = ((TdApi.UpdateConnectionState) object).state;
            if (connectionState instanceof TdApi.ConnectionStateReady) {
                Log.d("AuthActivity", "onResult: Successfully logged in!");
                Toast.makeText(AuthActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();
            }
        }*/
    }

    @Override
    public void onUonUpdatesReceived(TdApi.Object update) {
        // Handle received updates here
    }

    @Override
    public void onMessageReceived(TdApi.Message message) {

    }

    @Override
    public void onMessageDeleted(long chatId, long messageId) {

    }

    @Override
    public void onSetTdlibParametersSuccess() {
        // Called when TDLib parameters are set successfully
    }

    @Override
    public void onSetTdlibParametersError() {
        // Called when an error occurs while setting TDLib parameters
    }
}
