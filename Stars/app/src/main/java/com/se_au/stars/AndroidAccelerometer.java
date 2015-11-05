package com.se_au.stars;

//import java.util.Timer
import android.content.IntentSender;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

public class AndroidAccelerometer extends Activity implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public final static String EXTRA_MESSAGE = "com.se_au.stars.MESSAGE";

    private HeightCalculator heightCalculator;
    private GoogleApiClient mGoogleApiClient;
    private float lastZ;

    private AchievementsProvider mAchievementsProvider;
    private LeaderboardsProvider mLeaderboardsProvider;

    private float deltaZMax = 0;
    private float deltaZ = 0;
    private TextView currentZ, maxZ;

    public Vibrator v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_accelerometer);
        initializeViews();

        heightCalculator = new HeightCalculator();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mAchievementsProvider = new AchievementsProvider(mGoogleApiClient);
        mLeaderboardsProvider = new LeaderboardsProvider(mGoogleApiClient);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Log.d("WARN", "Device without accelerometer");
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
    }
    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
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
        /* TODO: Не получится ли так, что мы получим новый результат прежде чем отпишемся
        от событий сенсора? */
        double result = heightCalculator.Calculate(event.values);
        if(result < 0){
            return;
        }

        DisplayValue(result);
        //sensorManager.unregisterListener(this);
        mAchievementsProvider.Submit(result);
        mLeaderboardsProvider.Submit(result);
    }

    public void reset(View v) {
        heightCalculator.Reset();
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        deltaZMax = 0.0f;
        maxZ.setText("0.0");
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void showAchievements(View v) {
        try{
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 0);
        }
        catch (Exception e){
            Log.d(LogLevel.Warning, "Cannot open Achievements");
            Log.d(LogLevel.Info, e.toString());
            mGoogleApiClient.reconnect();
        }
    }

    public void showLeaderboard(View v) {
        try{
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    mLeaderboardsProvider.GetGlobalLeaderboardId()), 10);
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

    private void initializeViews() {
        maxZ = (TextView) findViewById(R.id.maxZ);
    }

    private void displayCleanValues() {
        currentZ.setText("0.0");
    }

    private void DisplayValue(double value){
        maxZ.setText(String.format("%.2f", value));
    }

    // display the current x,y,z accelerometer values
    private void displayCurrentValues(double value) {
        currentZ.setText(String.format("%.2f", value));
    }

    // display the max x,y,z accelerometer values
    private void displayMaxValues() {
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }
}
