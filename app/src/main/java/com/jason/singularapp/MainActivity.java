package com.jason.singularapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.messaging.FirebaseMessaging;
import com.jason.singularapp.databinding.ActivityMainBinding;
import com.singular.sdk.Singular;
import com.singular.sdk.SingularConfig;
import com.singular.sdk.SingularLinkHandler;
import com.singular.sdk.SingularLinkParams;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ActivityMainBinding binding;
    private Bundle deeplinkData;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(!task.isSuccessful()) {
                    Log.i(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                token = task.getResult();

                initSingularSDK(token);
            }
        });
    }

    private void initSingularSDK(String token) {

        SingularConfig config = new SingularConfig(Constants.API_KEY, Constants.SECRET)
                .withSingularLink(getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), new SingularLinkHandler() {
                    @Override
                    public void onResolved(SingularLinkParams singularLinkParams) {
                        Log.i("DEEPLINK_KEY", singularLinkParams.getDeeplink());
                        Log.i("PASSTHROUGH_KEY", singularLinkParams.getPassthrough());
                        Log.i("IS_DEFERRED_KEY", String.valueOf(singularLinkParams.isDeferred()));
                        Toast.makeText(MainActivity.this, singularLinkParams.getDeeplink(), Toast.LENGTH_LONG);
                    }
                })
                .withCustomUserId("test_user_1234")
                .withSessionTimeoutInSec(120)
                .withLoggingEnabled()
                .withDDLTimeoutInSec(300)
                .withFCMDeviceToken(token);

        Singular.init(this, config);
    }

    private void initSingularSDK() {
        SingularConfig config = new SingularConfig(Constants.API_KEY, Constants.SECRET).withSingularLink(getIntent(), new SingularLinkHandler() {
            @Override
            public void onResolved(SingularLinkParams singularLinkParams) {
                deeplinkData = new Bundle();
                deeplinkData.putString(Constants.DEEPLINK_KEY, singularLinkParams.getDeeplink());
                deeplinkData.putString(Constants.PASSTHROUGH_KEY, singularLinkParams.getPassthrough());
                deeplinkData.putBoolean(Constants.IS_DEFERRED_KEY, singularLinkParams.isDeferred());

                // When the is opened using a deeplink, we will open the deeplink tab
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });

        Singular.init(this, config);
    }
}