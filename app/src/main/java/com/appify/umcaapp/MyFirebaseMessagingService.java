package com.appify.umcaapp;

import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.constraint.Constraints.TAG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static String MY_PREFS = "my prefs";
    public static String KEY_ID = "id";
    public static String TOKEN = "token";
    private static String CHANNEL_ID = "id";
    private static int NOTIF_ID = 455;
    private String id;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        buildNotification(remoteMessage);
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        sendRegistrationToServer(token);
    }

    private void buildNotification(RemoteMessage remoteMessage) {
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        nBuilder.setSmallIcon(R.drawable.ic_action_pause)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("body"))
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(NOTIF_ID, nBuilder.build());
    }

    private void sendRegistrationToServer(final String token) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference().child("NotificationTokens");
        SharedPreferences sharedPrefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPrefs.edit();
        id = sharedPrefs.getString(KEY_ID, null);
        if (id == null) {
            id = tokenRef.push().getKey();
        }
        tokenRef.child(id).child("id").setValue(token).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        editor.putString(KEY_ID, id);
                        editor.putString(TOKEN, token);
                        editor.commit();
                        Log.d(TAG, "Token has been successfully registered");
                    }
                });
    }
}
