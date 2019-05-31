package com.appify.umcaapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.appify.umcaapp.fragments.AudioFragment;
import com.appify.umcaapp.fragments.DirectoryFragment;
import com.appify.umcaapp.fragments.EventsFragment;
import com.appify.umcaapp.fragments.HomeFragment;
import com.appify.umcaapp.fragments.VideoFragment;
import com.appify.umcaapp.model.Audio;
import com.appify.umcaapp.model.Member;
import com.appify.umcaapp.model.Video;
import com.appify.umcaapp.utils.BottomNavigationViewHelper;
import com.appify.umcaapp.utils.Utilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import static android.support.constraint.Constraints.TAG;
import static com.appify.umcaapp.MyFirebaseMessagingService.KEY_ID;
import static com.appify.umcaapp.MyFirebaseMessagingService.TOKEN;

public class MainActivity extends AppCompatActivity implements VideoFragment.OnFragmentInteractionListener, DirectoryFragment.OnFragmentInteractionListener, AudioFragment.OnFragmentInteractionListener {
    private BottomNavigationView bottomNavigationBar;
    private ActionBar toolbar;

    private NetworkReceiver networkReceiver;
    private IntentFilter intentFilter;
    private FirebaseAuth mAuth;

    private LinearLayout layout;

    private boolean isConnected = false;
    private String id;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter = new IntentFilter();
        intentFilter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);

        checkToken();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth == null) {
            signInAnonymously();
        }

        networkReceiver = new NetworkReceiver();

        layout = findViewById(R.id.layout);

        bottomNavigationBar = (BottomNavigationView) findViewById(R.id.bottomNavBar);

        BottomNavigationViewHelper.disableShiftMode(bottomNavigationBar);

        toolbar = getSupportActionBar();

        bottomNavigationBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {

                    case R.id.home:
                        fragment = new HomeFragment();
                        loadFragment(fragment);
                        toolbar.setTitle("Home");
                        return true;
                    case R.id.video:
                        fragment = new VideoFragment();
                        loadFragment(fragment);
                        toolbar.setTitle("Videos");
                        return true;
                    case R.id.audio:
                        fragment = new AudioFragment();
                        loadFragment(fragment);
                        toolbar.setTitle("Audio Messages");
                        return true;
                    case R.id.directory:
                        fragment = new DirectoryFragment();
                        loadFragment(fragment);
                        toolbar.setTitle("Chapel Directory");
                        return true;

                    case R.id.events:
                        fragment = new EventsFragment();
                        loadFragment(fragment);
                        toolbar.setTitle("Events");
                        return true;
                }
                return false;
            }
        });
        bottomNavigationBar.setSelectedItemId(R.id.home);
    }

    @Override
    public void onVideoItemClick(Video video) {
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra("video", video);
        startActivity(intent);
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onMemberClick(Member member) {
        Intent intent = new Intent(MainActivity.this, MemberProfileActivity.class);
        intent.putExtra("member", member);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(networkReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(networkReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Service", "Service terminated");
    }

    @Override
    public void onFragmentInteraction(Audio audio) {
        if (isConnected){
            Intent intent = new Intent(MainActivity.this, AudioMsgActivity.class);
            intent.putExtra("audio", audio);
            startActivity(intent);
        } else {
            Utilities.makeSnackBar(layout, "There is no active Internet connection");
        }
    }

    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();

            if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                isConnected = true;
                Utilities.makeSnackBar(layout, "Connected");
            } else if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
                isConnected = false;
                Utilities.makeSnackBar(layout, "Not Connected");
            }
        }
    }

    private void checkToken() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            isConnected = true;
        }

        if (isConnected) {
            final DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference().child("NotificationTokens");
            SharedPreferences sharedPrefs = getSharedPreferences(MyFirebaseMessagingService.MY_PREFS, MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPrefs.edit();
            id = sharedPrefs.getString(KEY_ID, null);
            token = sharedPrefs.getString(TOKEN, null);
            if (id == null || token == null) {
                id = tokenRef.push().getKey();
                Log.d(TAG, "Token has not been registered");

                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (task.isSuccessful()) {
                                    token = task.getResult().getToken();
                                    tokenRef.child(id).child("id").setValue(token).
                                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    editor.putString(KEY_ID, id);
                                                    editor.putString(TOKEN, token);
                                                    editor.commit();
                                                    Log.d(TAG, "Token has been successfully registered");
                                                    Log.d(TAG, "Token ID is " + id);
                                                }
                                            });
                                }
                            }
                        });
            } else {
                Log.d("Token Status", "Token already registered");
            }

        }

    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
            // do your stuff
            Log.d("mAuth", "Successfully logged in");
            }
        }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
            Log.e(TAG, "signInAnonymously:FAILURE", exception);
                    }
                });
    }

    private void registerToken() {
        if (isConnected) {
            final DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference().child("NotificationTokens");
            SharedPreferences sharedPrefs = getSharedPreferences(MyFirebaseMessagingService.MY_PREFS, MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPrefs.edit();
            id = sharedPrefs.getString(KEY_ID, null);
            token = sharedPrefs.getString(TOKEN, null);
            if (id == null) {
                id = tokenRef.push().getKey();
                Log.d(TAG, "Token has not been registered");
            }
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (task.isSuccessful()) {
                                token = task.getResult().getToken();
                                tokenRef.child(id).child("id").setValue(token).
                                        addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                editor.putString(KEY_ID, id);
                                                editor.putString(TOKEN, token);
                                                editor.commit();
                                                Log.d(TAG, "Token has been successfully registered");
                                                Log.d(TAG, "Token ID is " + id);
                                            }
                                        });
                            }
                        }
                    });

        } else {
            Utilities.makeSnackBar(layout, "Unable to register token because you're offline");
        }

    }
}
