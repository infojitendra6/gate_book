package com.kopykitab.gate.components;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kopykitab.gate.BuildConfig;
import com.kopykitab.gate.LoginMainActivity;
import com.kopykitab.gate.NotificationActivity;
import com.kopykitab.gate.WebViewActivity;
import com.kopykitab.gate.settings.AppSettings;
import com.kopykitab.gate.settings.Constants;
import com.kopykitab.gate.R;
import com.kopykitab.gate.LibraryActivity;

public class NotificationIntentService extends IntentService {
    // Sets an ID for the notification, so it can be updated
    NotificationCompat.Builder builder;

    public NotificationIntentService() {
        super("NotificationIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("message")) {
                    sendNotification(extras.get("message").toString());
                }
            }
        }
        NotificationBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String notificationJson) {
        new GenerateNotification(this).execute(notificationJson);
    }

    private class GenerateNotification extends AsyncTask<String, Void, Bitmap> {

        private Context mContext;
        private JSONObject notificationJsonObject;

        public GenerateNotification(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            try {
                notificationJsonObject = new JSONObject(params[0]);

                String imageUrl = notificationJsonObject.getString("image_url");
                if (imageUrl != null && !imageUrl.equals("") && !imageUrl.isEmpty()) {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream in = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(in);
                    return myBitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            try {
                Intent resultIntent = new Intent(mContext, NotificationActivity.class);
                if (!AppSettings.getInstance(getApplicationContext()).isConfigurationDone()) {
                    resultIntent = new Intent(mContext, LoginMainActivity.class);
                }

                Class customClass = null;
                try {
                    if (notificationJsonObject.has("activity_name") && notificationJsonObject.getString("activity_name") != null && !notificationJsonObject.getString("activity_name").isEmpty()) {
                        String activityName = BuildConfig.APPLICATION_ID + "." + notificationJsonObject.getString("activity_name");
                        customClass = Class.forName(activityName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (customClass != null) {
                    resultIntent = new Intent(mContext, customClass);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } else {
                    String notificationUrl = notificationJsonObject.getString("notification_url");
                    if (notificationUrl != null && !notificationUrl.isEmpty() && !notificationUrl.equals("")) {
                        if (notificationJsonObject.getBoolean("open_directly")) {
                            if (notificationJsonObject.getString("notification_url").startsWith(Constants.BASE_URL + "app/")) {
                                resultIntent = new Intent(mContext, WebViewActivity.class);
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                resultIntent.putExtra("web_url", notificationJsonObject.getString("notification_url"));
                            } else {
                                resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(notificationJsonObject.getString("notification_url")));
                            }
                        }
                    }
                }
                resultIntent.putExtra("notification_type", notificationJsonObject.getString("notification_type"));
                Intent libraryIntent = new Intent(mContext, LibraryActivity.class);
                Intent[] listOfIntents = {libraryIntent, resultIntent};
                PendingIntent resultPendingIntent = PendingIntent.getActivities(mContext, 0, listOfIntents, PendingIntent.FLAG_ONE_SHOT);

                NotificationCompat.Builder mNotifyBuilder;
                NotificationManager mNotificationManager;

                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotifyBuilder = new NotificationCompat.Builder(mContext)
                        .setContentTitle(notificationJsonObject.getString("title"))
                        .setContentText(notificationJsonObject.getString("description"))
                        .setSmallIcon(R.mipmap.ic_launcher);

                if (result != null) {
                    mNotifyBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(result));
                }

                // Set pending intent
                mNotifyBuilder.setContentIntent(resultPendingIntent);

                // Set Vibrate, Sound and Light
                int defaults = 0;
                defaults = defaults | Notification.DEFAULT_LIGHTS;
                defaults = defaults | Notification.DEFAULT_VIBRATE;
                defaults = defaults | Notification.DEFAULT_SOUND;

                mNotifyBuilder.setDefaults(defaults);
                // Set autocancel
                mNotifyBuilder.setAutoCancel(true);
                // Post a notification
                int notifyID = new Random().nextInt(9999 - 1000) + 1000;
                mNotificationManager.notify(notifyID, mNotifyBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}