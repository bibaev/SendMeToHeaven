package com.se_au.stars;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

public class AndroidAccelerometer extends Activity implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public final static String EXTRA_MESSAGE = "com.se_au.stars.MESSAGE";

    private HeightCalculator mHeightCalculator;
    private GoogleApiClient mGoogleApiClient;

    private AchievementsProvider mAchievementsProvider;
    private LeaderboardsProvider mLeaderboardsProvider;

    private double mMaximum;
    private TextView mMaxTextView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    public Vibrator mVibrator;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_accelerometer);
        mMaxTextView = (TextView) findViewById(R.id.maxZ);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mMaximum = 0x0;

        mHeightCalculator = new HeightCalculator();
        mHeightCalculator.Reset();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mAchievementsProvider = new AchievementsProvider(mGoogleApiClient);
        mLeaderboardsProvider = new LeaderboardsProvider(mGoogleApiClient);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer != null) {
            // success! we have an mAccelerometer
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            Log.d(LogLevel.Warning, "Device without mAccelerometer");
        }

        mVibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LogLevel.Info, "in onStart. Call connect()");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LogLevel.Info, "in onStop. Call disconnect()");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double result = mHeightCalculator.Calculate(event.values);
        mMaximum = Math.max(result, mMaximum);
        DisplayValue(mMaximum);
        if (mMaximum > .001) {
            mSensorManager.unregisterListener(this);
            if (mAchievementsProvider.Submit(result)){
                mVibrator.vibrate(50);
            }

            mLeaderboardsProvider.Submit(result);
        }
    }

    public void reset(View v) {
        mHeightCalculator.Reset();
        Log.d(LogLevel.Info, "____________________________________________________");
        mMaximum = 0;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onShowAchievements(View v) {
        try{
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 0);
        }
        catch (Exception e){
            Log.d(LogLevel.Warning, "Cannot open Achievements");
            Log.d(LogLevel.Info, e.toString());
            mGoogleApiClient.reconnect();
        }
    }

    public void onShowLeaderboard(View v) {
        try{
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    mLeaderboardsProvider.GetGlobalLeaderboardId()), 0);
        }
        catch (Exception e){
            Log.d(LogLevel.Warning, "Cannot open Leaderboard");
            Log.d(LogLevel.Info, e.toString());
            mGoogleApiClient.reconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LogLevel.Info, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LogLevel.Error, "connection suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LogLevel.Info, "Can not connect: " + connectionResult.toString());

        try {
            Log.d(LogLevel.Info, "Try auth");
            connectionResult.startResolutionForResult(this, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.d(LogLevel.Error, "Auth failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                // All required changes were successfully made
                Log.d(LogLevel.Info, "Result ok");
                break;
            case Activity.RESULT_CANCELED:
                // The user was asked to change settings, but chose not to
                Log.d(LogLevel.Warning, "Result not ok");
                break;
            default:
                break;
        }
    }

    private void DisplayValue(double value){
        if(value >= 100f){
            value = 99.99;
        }

        mMaxTextView.setText(String.format("%.2f", value));
    }
}
