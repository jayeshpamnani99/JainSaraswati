package com.saraswati.jain.jainsaraswati;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.saraswati.jain.jainsaraswati.Activities.HomeActivity;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

public class MainActivity extends AppCompatActivity {

    private static final int HIGH_PRIORITY_UPDATE = 4;
    private static final int MEDIUM_PRIORITY_UPDATE = 2;

    private static final int DAYS_FOR_FLEXIBLE_UPDATE = 3;
    private static final int DAYS_FOR_IMMEDIATE_UPDATE = 7;

    private static final int IMMEDIATE_UPDATE_REQUEST_CODE = 1;
    private static final int FLEXIBLE_UPDATE_REQUEST_CODE = 2;


    AppUpdateManager appUpdateManager;

    Task<AppUpdateInfo> appUpdateInfoTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();


        appUpdateInfoTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                finish();
            }
        });


        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                    if((appUpdateInfo.updatePriority() >= HIGH_PRIORITY_UPDATE || (appUpdateInfo.clientVersionStalenessDays() != null
                            && appUpdateInfo.clientVersionStalenessDays() >= DAYS_FOR_IMMEDIATE_UPDATE)) && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.IMMEDIATE,
                                    this,
                                    IMMEDIATE_UPDATE_REQUEST_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }else if((appUpdateInfo.updatePriority() >= MEDIUM_PRIORITY_UPDATE || (appUpdateInfo.clientVersionStalenessDays() != null
                            && appUpdateInfo.clientVersionStalenessDays() >= DAYS_FOR_FLEXIBLE_UPDATE)) && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                        try {

                            InstallStateUpdatedListener listener = state -> {
                                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                                    popupSnackbarForCompleteUpdate();
                                }
                            };

                            appUpdateManager.registerListener(listener);

                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.FLEXIBLE,
                                    this,
                                    FLEXIBLE_UPDATE_REQUEST_CODE);

                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }

                }else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }
                    },1000);
                }
            });





    }


    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.activity_main_update),
                        "Finished Downloading the update, please Restart",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FLEXIBLE_UPDATE_REQUEST_CODE || requestCode == IMMEDIATE_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                startActivity(new Intent(MainActivity.this,HomeActivity.class));
                finish();
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {

                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            IMMEDIATE,
                                            this,
                                            1);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                                popupSnackbarForCompleteUpdate();
                            }

                        });
    }
}
