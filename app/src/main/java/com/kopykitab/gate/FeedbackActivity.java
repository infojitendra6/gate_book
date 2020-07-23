package com.kopykitab.gate;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.kopykitab.gate.settings.AppSettings;
import com.kopykitab.gate.settings.Constants;
import com.kopykitab.gate.settings.Utils;
import com.kopykitab.gate.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FeedbackActivity extends AppCompatActivity {
	
	private EditText feedbackDescription;
	
	private String screenName = "Feedback";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeAsUpIndicator(R.drawable.back_button);
		actionBar.setDisplayHomeAsUpEnabled(true);
		SpannableString actionBarLabel = new SpannableString(getResources().getString(R.string.feedback_label));
		actionBarLabel.setSpan(new TypefaceSpan("" + Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf")), 0, actionBarLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		actionBar.setTitle(actionBarLabel);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)	{
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(getResources().getColor(R.color.action_bar_background_dark));
		}
		
		feedbackDescription = (EditText) findViewById(R.id.feedback_description);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.feedback_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	onBackPressed();
	            return true;
	        case R.id.submit_feedback:
	        	OnSubmitFeedbackButtonClick(feedbackDescription);
	        	return true;
	        default: 
	        	return super.onOptionsItemSelected(item);
	    }
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
		Utils.triggerScreen(FeedbackActivity.this, screenName);
	}
	
	public void OnSubmitFeedbackButtonClick(View v) {
		String description = feedbackDescription.getText().toString();
		
		if (description.trim().equals("")) {
			Constants.showToast("Enter Comments", this);
			feedbackDescription.requestFocus();
		} else	{
			new SubmitFeedback().execute(description);
		}
	}
	
	private class SubmitFeedback extends AsyncTask<String, Void, String>	{

		private ProgressDialog pDialog;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pDialog = new ProgressDialog(FeedbackActivity.this);
			pDialog.setMessage("Feedback submit in progress...");
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
			postParams.add(new BasicNameValuePair("customer_id", AppSettings.getInstance(FeedbackActivity.this).get("CUSTOMER_ID")));
			postParams.add(new BasicNameValuePair("feedback_description", params[0]));
			postParams.add(new BasicNameValuePair("login_source", Constants.LOGIN_SOURCE));

			String response = null;

			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpEntity httpEntity = null;
				HttpResponse httpResponse = null;

				HttpPost httpPost = new HttpPost(Constants.FEEDBACK_API_URL);
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
				Log.i("Feedback Submit Details", result);
				
				try {
					JSONObject jsonObject = new JSONObject(result);
					boolean status = jsonObject.getBoolean("status");
					final AlertDialog alertDialog = Utils.createAlertBox(FeedbackActivity.this);
					alertDialog.show();
					if(status)	{
						((ImageView) alertDialog.findViewById(R.id.dialog_icon)).setImageResource(R.drawable.thumbs_up);
						((TextView) alertDialog.findViewById(R.id.dialog_title)).setText("Success");
					} else	{
						((ImageView) alertDialog.findViewById(R.id.dialog_icon)).setImageResource(R.drawable.error_icon);
						((TextView) alertDialog.findViewById(R.id.dialog_title)).setText("Error");
					}
					((TextView) alertDialog.findViewById(R.id.dialog_message)).setText(jsonObject.getString("message"));
					((LinearLayout) alertDialog.findViewById(R.id.dialog_one_button)).setVisibility(View.VISIBLE);
					Button alertDialogButton = (Button) alertDialog.findViewById(R.id.dialog_one_button_button);
					alertDialogButton.setText("Ok");
					alertDialogButton.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View paramView) {
							// TODO Auto-generated method stub
							onBackPressed();
							alertDialog.dismiss();								
						}
					});					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}