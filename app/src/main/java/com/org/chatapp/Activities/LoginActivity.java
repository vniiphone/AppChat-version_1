package com.org.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.org.chatapp.Local.TokenStorage;
import com.org.chatapp.R;
import com.org.chatapp.Utils.TDLibManager;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;

public class LoginActivity extends AppCompatActivity implements TDLibManager.Callback {

    private Client client;
    private Button btn_login;
    private TextInputLayout layout_textinput;
    private TextInputEditText edt_sdt;
    String strSDT = "";
    String TAG = "Login Activity";
    TokenStorage tokenStorage;
    String token;
    boolean hasToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*
         * xác thực thành công và nhận được Token từ máy chủ Telegram,
         * gọi phương thức saveAccessToken() của TokenStorage để lưu trữ Token
         */
        client = TDLibManager.getClient(this);
        TdApi.GetAuthorizationState getAuthorizationState = new TdApi.GetAuthorizationState();
        client.send(getAuthorizationState, this);
        // Khởi tạo TokenStorage và gán giá trị cho token và hasToken
        tokenStorage = new TokenStorage(this);
        token = tokenStorage.getAccessToken();
        hasToken = tokenStorage.hasAccessToken();
        AnhXa();
        btn_login.setOnClickListener(v -> {
            if (CheckSDT(edt_sdt.getText().toString().trim())) {
                DangNhap();
            }
        });
        Log.d("DebugLogin","Start: OnCreate");
    }

    public void AnhXa() {
        btn_login = findViewById(R.id.btn_login);
        edt_sdt = findViewById(R.id.edt_sdt);
    }

    public void DangNhap() {
        //Nhận OTP để đăng nhập
        strSDT = edt_sdt.getText().toString().trim();
        if (!strSDT.isEmpty()) {
            if (strSDT.startsWith("+84")) {
                // Lấy token từ TokenStorage
                String token = tokenStorage.getAccessToken();
                /*
                 * Gửi OTP cho TDLib để gửi cho máy chủ Telegram. Bạn có thể sử dụng phương thức
                 * client.send() để gửi yêu cầu xác thực sử dụng OTP đến máy chủ Telegram
                 */
                Log.d("CheckSDT_DangNhap()", "CheckSDT(strSDT): " + strSDT);
                TdApi.PhoneNumberAuthenticationSettings authenticationSettings = new TdApi.PhoneNumberAuthenticationSettings();
                authenticationSettings.allowFlashCall = true;
                authenticationSettings.isCurrentPhoneNumber = true;
                TdApi.SetAuthenticationPhoneNumber setPhoneNumber = new TdApi.SetAuthenticationPhoneNumber(strSDT, authenticationSettings);
                client.send(setPhoneNumber, this);
            } else {
                edt_sdt.setError("Nhập đúng định dạng là +84 cho vùng Việt Nam");
            }
        } else {
            edt_sdt.setError("Số điện thoại không được trống");
            Toast.makeText(this, "Vui lòng nhập số điện thoại để đăng nhập", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean CheckSDT(String strSDT) {
//        strSDT = edt_sdt.getText().toString().trim();
        if (!strSDT.isEmpty()) {
            if (strSDT.startsWith("+84")) {
                return true;
            } else {
                edt_sdt.setError("Nhập đúng định dạng là +84 cho vùng Việt Nam");
                return false;
            }
        } else {
            edt_sdt.setError("Số điện thoại không được trống");
            Toast.makeText(this, "Vui lòng nhập số điện thoại để đăng nhập", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onResult(TdApi.Object object) {
      /*  if (object instanceof TdApi.Ok) {
            // Xác thực thành công
            // Xử lý và chuyển hướng đến màn hình ConversationActivity

            Log.d("LoginActivity", "onResult: input OTP");
            Intent intent = new Intent(LoginActivity.this, AuthActivity.class);
            startActivity(intent);

            // Lấy Token từ kết quả xác thực
            String token = tokenStorage.getAccessToken(); // Lấy Token từ kết quả xác thực
            // Lưu trữ token
            tokenStorage.saveAccessToken(token);
            Log.d("saveAccess","Token: "+token);
            finish();
        } else if (object instanceof TdApi.Error) {
            // Xử lý lỗi xác thực
            edt_sdt.setError("Lỗi, kiểm tra lại số điện thoại");
            Toast.makeText(this, "Lỗi Đăng Nhập ", Toast.LENGTH_SHORT).show();
            Log.d("LoginActivity", "onResult: lỗi xác thực");
        } else {
            // Xử lý các kết quả khác từ TDLib
//            Intent intent = new Intent(LoginActivity.this, AuthActivity.class);
//            startActivity(intent);
            Log.d("LoginActivity", "onResult: xử lý kết quả từ TDLib");
            finish();
        }*/
      switch (object.getConstructor()) {
            case TdApi.Ok.CONSTRUCTOR:
                Log.d("onResult-->", "Ok.Contructor + " + object);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case TdApi.Error.CONSTRUCTOR:
                edt_sdt.setError("Lỗi Xác Thực, kiểm tra số điện thoại");
                Toast.makeText(this, "Lỗi Xác Thực", Toast.LENGTH_SHORT).show();
                Log.d("LoginActivity", "onResult: lỗi xác thực");
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR:
                Log.d("DebugLogin","AuthorizationStateWaitPhoneNumber");
                TdApi.PhoneNumberAuthenticationSettings authenticationSettings = new TdApi.PhoneNumberAuthenticationSettings();
                authenticationSettings.allowFlashCall = true;
                authenticationSettings.isCurrentPhoneNumber = true;
                client.send(new TdApi.SetAuthenticationPhoneNumber(strSDT, authenticationSettings), this::onResult);

                break;
            default:
                Log.d("onResult-->", "default");
        }
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


    @Override
    public void onSetTdlibParametersSuccess() {
        // Xử lý khi thiết lập TdlibParameters thành công
        Log.d("LoginActivity", "onSetTdlibParametersSuccess: thiết lập TdlibParameters thành công");
    }

    @Override
    public void onSetTdlibParametersError() {
        // Xử lý khi thiết lập TdlibParameters gặp lỗi
    }
}