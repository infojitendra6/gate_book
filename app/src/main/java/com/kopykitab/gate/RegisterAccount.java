package com.kopykitab.gate;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kopykitab.gate.components.RadioButton;
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
public class RegisterAccount extends AppCompatActivity {

    private String screenName = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.register_account);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.action_bar_background_dark));
        }

        ((EditText) findViewById(R.id.register_firstname)).setHintTextColor(Color.argb(179, 255, 255, 255));
        ((EditText) findViewById(R.id.register_lastname)).setHintTextColor(Color.argb(179, 255, 255, 255));
        ((EditText) findViewById(R.id.register_email)).setHintTextColor(Color.argb(179, 255, 255, 255));
        ((EditText) findViewById(R.id.register_password)).setHintTextColor(Color.argb(179, 255, 255, 255));
        ((EditText) findViewById(R.id.register_mobile_number)).setHintTextColor(Color.argb(179, 255, 255, 255));
        ((EditText) findViewById(R.id.register_referral_code)).setHintTextColor(Color.argb(179, 255, 255, 255));
        ((EditText) findViewById(R.id.register_password)).setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView tv, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    OnRegisterAccountButtonClick(tv);
                    return true;
                }
                return false;
            }
        });

        AccountManager accountManager = AccountManager.get(RegisterAccount.this);
        for (Account account : accountManager.getAccounts()) {
            if (account.type.equals("com.google")) {
                if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                    ((EditText) findViewById(R.id.register_email)).setText(account.name);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }

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
        Utils.triggerScreen(RegisterAccount.this, screenName);
    }

    public void OnRegisterAccountButtonClick(View v) {
        String firstName = ((EditText) findViewById(R.id.register_firstname)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.register_lastname)).getText().toString();
        String emailId = ((EditText) findViewById(R.id.register_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.register_password)).getText().toString();
        String referralCode = ((EditText) findViewById(R.id.register_referral_code)).getText().toString();
        String mobileNumber = ((EditText) findViewById(R.id.register_mobile_number)).getText().toString();

        RadioGroup customerTypeGroup = (RadioGroup) findViewById(R.id.customer_type_group);
        String customerType = ((RadioButton) findViewById(customerTypeGroup.getCheckedRadioButtonId())).getText().toString();

        if (firstName.equals("")) {
            Constants.showToast("Enter First Name", this);
        } else if (lastName.equals("")) {
            Constants.showToast("Enter Last Name", this);
        } else if (emailId.equals("")) {
            Constants.showToast("Enter Email Id", this);
        } else if (password.equals("")) {
            Constants.showToast("Enter Password", this);
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailId).matches()) {
            Constants.showToast("Enter Proper Email Id", this);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (Utils.isNetworkConnected(this)) {
                new CreateAccount().execute(firstName, lastName, emailId, password, referralCode, mobileNumber, customerType);
            } else {
                Utils.networkNotAvailableAlertBox(this);
            }
        }
    }

    public void OnAlreadyRegisteredButtonClick(View v) {
        Utils.showLoginActivity(this);
    }

    public void OnBackButtonClick(View v) {
        onBackPressed();
    }

    private class CreateAccount extends AsyncTask<String, Void, String> {

        private ProgressDialog pDialog;
        private String email;
        private String appId;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterAccount.this);
            pDialog.setMessage("Please wait while we register your account");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            appId = Utils.getAppId(RegisterAccount.this);
            email = params[2];

            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            postParams.add(new BasicNameValuePair("firstname", params[0]));
            postParams.add(new BasicNameValuePair("lastname", params[1]));
            postParams.add(new BasicNameValuePair("email", email));
            postParams.add(new BasicNameValuePair("password", params[3]));
            postParams.add(new BasicNameValuePair("appid", appId));
            postParams.add(new BasicNameValuePair("login_source", Constants.LOGIN_SOURCE));
            postParams.add(new BasicNameValuePair("referral_code", params[4]));
            postParams.add(new BasicNameValuePair("mobile_number", params[5]));
            postParams.add(new BasicNameValuePair("attributes[customer_type]", params[6]));

            String response = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpEntity httpEntity = null;
                HttpResponse httpResponse = null;

                HttpPost httpPost = new HttpPost(Constants.REGISTER_API_URL);
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

            try {
                JSONObject jsonObject = new JSONObject(result);
                String statusMessage = jsonObject.getString("message");

                boolean status = jsonObject.getBoolean("status");
                if (status) {
                    String customerId = jsonObject.getString("customer_id");
                    AppSettings.getInstance(RegisterAccount.this).setConfiguration(email, customerId, appId);
                    AppSettings.getInstance(RegisterAccount.this).set("customer_name", jsonObject.getString("firstname") + " " + jsonObject.getString("lastname"));
                    Constants.showToast(statusMessage, RegisterAccount.this);

                    Utils.showLibraryAndSyncData(RegisterAccount.this);
                } else {
                    final AlertDialog alertDialog = Utils.createAlertBox(RegisterAccount.this);
                    alertDialog.show();
                    ((ImageView) alertDialog.findViewById(R.id.dialog_icon)).setImageResource(R.drawable.error_icon);
                    ((TextView) alertDialog.findViewById(R.id.dialog_title)).setText("Error");
                    ((TextView) alertDialog.findViewById(R.id.dialog_message)).setText(statusMessage);
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}