package com.kopykitab.gate;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kopykitab.gate.settings.AppSettings;
import com.kopykitab.gate.settings.Constants;
import com.kopykitab.gate.settings.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;

    private String screenName = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppSettings.getInstance(this).isConfigurationDone()) {
            Intent libraryIntent = new Intent(this, LibraryActivity.class);
            startActivity(libraryIntent);
            finish();
        } else {
            setContentView(R.layout.activity_login);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(R.color.action_bar_background_dark));
            }

            loginEmail = (EditText) findViewById(R.id.login_email);
            loginPassword = (EditText) findViewById(R.id.login_password);
            loginButton = (Button) findViewById(R.id.login_button);

            loginEmail.setHintTextColor(Color.argb(179, 255, 255, 255));
            loginPassword.setHintTextColor(Color.argb(179, 255, 255, 255));

            loginEmail.addTextChangedListener(mTextWatcher);
            loginPassword.addTextChangedListener(mTextWatcher);
            loginPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        validateLogin(tv);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            if (!loginEmail.getText().toString().equals("") && !loginPassword.getText().toString().equals("")) {
                loginButton.setEnabled(true);
            } else {
                loginButton.setEnabled(false);
            }
        }
    };

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Utils.triggerScreen(LoginActivity.this, screenName);
    }

    public void validateLogin(View v) {
        String login_email = loginEmail.getText().toString();
        String login_password = loginPassword.getText().toString();

        if (login_email.equals("")) {
            Constants.showToast("Enter Email Id", this);
        } else if (login_password.equals("")) {
            Constants.showToast("Enter Password", this);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(login_email).matches()) {
            Constants.showToast("Enter Proper Email Id", this);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (Utils.isNetworkConnected(LoginActivity.this)) {
                new ValidateLogin().execute(login_email, login_password);
            } else {
                Utils.networkNotAvailableAlertBox(LoginActivity.this);
            }
        }
    }

    public void OnBackButtonClick(View v) {
        onBackPressed();
    }

    public void OnForgotPasswordButtonClick(View v) {
        Utils.showForgotPasswordActivity(this);
    }

    public void OnRegisterButtonClick(View v) {
        Utils.showRegisterActivity(this);
    }

    private class ValidateLogin extends AsyncTask<String, Void, String> {

        private String appId, email;
        private ProgressDialog pDialog;

        protected ValidateLogin() {
            appId = Utils.getAppId(LoginActivity.this);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Please wait, Logging in progress...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            email = params[0];

            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("email", email));
            postParams.add(new BasicNameValuePair("password", params[1]));
            postParams.add(new BasicNameValuePair("appid", appId));
            postParams.add(new BasicNameValuePair("login_source", Constants.LOGIN_SOURCE));

            String response = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpEntity httpEntity = null;
                HttpResponse httpResponse = null;

                HttpPost httpPost = new HttpPost(Constants.LOGIN_API_URL);
                httpPost.setEntity(new UrlEncodedFormEntity(postParams));

                httpResponse = httpClient.execute(httpPost);

                httpEntity = httpResponse.getEntity();
                response = EntityUtils.toString(httpEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            if (result != null) {
                Log.i("Login Details", result);

                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String status = jsonObject.getString("status");
                    String customerId = "";
                    Utils.triggerGAEvent(LoginActivity.this, "Login", jsonObject.getString("messageStatus"), email);
                    if (status.equals("True")) {
                        customerId = jsonObject.getString("id");
                        AppSettings.getInstance(LoginActivity.this).setConfiguration(email, customerId, appId);
                        AppSettings.getInstance(LoginActivity.this).set("customer_name", jsonObject.getString("Firstname") + " " + jsonObject.getString("Lastname"));
                        if (jsonObject.has("gcm_reg_id")) {
                            AppSettings.getInstance(LoginActivity.this).set("GCM_REG_ID", jsonObject.getString("gcm_reg_id"));
                        }

                        Utils.showLibraryAndSyncData(LoginActivity.this);
                    } else if (status.equals("False")) {
                        final AlertDialog alertDialog = Utils.createAlertBox(LoginActivity.this);
                        alertDialog.show();
                        ((ImageView) alertDialog.findViewById(R.id.dialog_icon)).setImageResource(R.drawable.error_icon);
                        ((TextView) alertDialog.findViewById(R.id.dialog_title)).setText("Error");
                        ((TextView) alertDialog.findViewById(R.id.dialog_message)).setText(jsonObject.getString("messageStatus"));
                        ((LinearLayout) alertDialog.findViewById(R.id.dialog_one_button)).setVisibility(View.VISIBLE);
                        Button alertDialogButton = (Button) alertDialog.findViewById(R.id.dialog_one_button_button);
                        alertDialogButton.setText("Ok");
                        alertDialogButton.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View paramView) {
                                // TODO Auto-generated method stub
                                alertDialog.dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("Error Login", "Problem in login");
                Constants.showToast("Error in Login. Check Internet Connection.", LoginActivity.this);
            }
        }
    }
}