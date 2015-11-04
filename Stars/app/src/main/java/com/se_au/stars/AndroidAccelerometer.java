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

    private static String LOG_INFO = "INFO";
    private static String LOG_WARN = "WARN";
    private static String LOG_ERROR = "ERROR";
    private static String LOG_FATAL = "FATAL";

    // Fragments
//    WinFragment mWinFragment;
    private GoogleApiClient mGoogleApiClient;
    private float lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private boolean isGameComplete;
    private AchievementsProvider mAchievementsProvider;
    private LeaderboardsProvider mLeaderboardsProvider;

    private float deltaZMax = 0;
    private float deltaZ = 0;
    private TextView currentZ, maxZ;

    public Vibrator v;

    public AndroidAccelerometer() {
        start = true;
        timestamp = 0;
        v0 = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_accelerometer);
        initializeViews();

        isGameComplete = false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mAchievementsProvider = new AchievementsProvider(mGoogleApiClient);
        mLeaderboardsProvider = new LeaderboardsProvider(mGoogleApiClient);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            float vibrateThreshold = accelerometer.getMaximumRange() / 2;
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
        Log.d(LOG_INFO, "in onStart. Call connect()");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_INFO, "in onStop. Call disconnect()");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    public void initializeViews() {
        currentZ = (TextView) findViewById(R.id.currentZ);
        maxZ = (TextView) findViewById(R.id.maxZ);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    boolean start;
    long timestamp;
    double v0;
    double[] gravity = new double[3];
    double[] linear_acceleration = new double[3];
    final double alpha = .8;
    double max = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {

        displayCleanValues();
        displayCurrentValues();
        deltaZ = Math.abs(lastZ - event.values[2]);
        lastZ = event.values[2];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        double lin_acc = 0;
        double norm = 0;
        for (int i = 0; i < 3; ++i) {
            norm += gravity[i]*gravity[i];
        }
        norm = Math.sqrt(norm);

        for (int i = 0; i < 3; ++i) {
            lin_acc += linear_acceleration[i] * gravity[0] / norm;
        }

        if (Math.abs(lin_acc) > 5)
        {
            if (start) {
                timestamp = System.currentTimeMillis();
                v0 = lin_acc;
                start = false;
            } else {
                long timeDelta = (System.currentTimeMillis() - timestamp) / 2;
                maxZ.setText(Double.toString(getMaxHeight(v0, timeDelta)));
                start = true;
            }
        }

        float result = 2.0f;
        if(isGameComplete){
            sensorManager.unregisterListener(this);
            mAchievementsProvider.Submit(result);
            mLeaderboardsProvider.Submit(result);
        }
    }

    public Double getMaxHeight(Double v0, long timeDelta)
    {
        final double g = 9.81f;

        Float time = timeDelta / 2000f;
        double vel = v0 * time;
        return vel * vel / 2 * g;
    }

    public void displayCleanValues() {
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }
    public void reset(View v) {
        isGameComplete = false;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        deltaZMax = 0.0f;
        maxZ.setText("0.0");
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void showAchievements(View v) {
        try{
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 0);
        }
        catch (Exception e){
            Log.d(LOG_INFO, "Cannot open Achievements");
        }
    }

    public void showLeaderboard(View v) {
        try{
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    mLeaderboardsProvider.GetGlobalLeaderboardId()), 10);
        }
        catch (Exception ex){
            Log.d(LOG_INFO, "Cannot open Leaderboard");
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_INFO, "onStop(): connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_WARN, "connection suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_INFO, "Can not connect: " + connectionResult.toString());

        try {
            Log.d(LOG_INFO, "Try auth");
            connectionResult.startResolutionForResult(this, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.d(LOG_ERROR, "Auth failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                // All required changes were successfully made
                Log.d(LOG_INFO, "Result ok");
                break;
            case Activity.RESULT_CANCELED:
                // The user was asked to change settings, but chose not to
                Log.d(LOG_INFO, "Result not ok");
                break;
            default:
                break;
        }
    }
}
