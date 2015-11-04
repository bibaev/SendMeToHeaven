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
        Log.d("aaaaaa", "onStart(): connecting");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        long startTime = System.currentTimeMillis();

        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaZ < 2)
            deltaZ = 0;

        lastZ = event.values[2];

        if (deltaZ > 2.5)
        {
            long timeUp = System.currentTimeMillis() - startTime;
            maxZ.setText(Float.toString(getMaxHeight(timeUp/1000f)));
        }

        float result = 2.0f;
        if(isGameComplete){
            sensorManager.unregisterListener(this);
            mAchievementsProvider.Submit(result);
            mLeaderboardsProvider.Submit(result);
        }
    }

    public float getMaxHeight(float timeUp)
    {
        final float g = 9.81f;
        //v = v0 + a*t; -> v0 = v - a*t, a = -g_z
        float v0 = g * timeUp;  //
        float h = v0 * timeUp + g * timeUp * timeUp / 2;
        return h;
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
    }

    public void showAchievements(View v) {
        if(mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 0);
        }
    }

    public void showLeaderboard(View v) {
        if(mGoogleApiClient.isConnected()) {
            Log.d("INFO", mLeaderboardsProvider.GetGlobalLeaderboardId());
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    mLeaderboardsProvider.GetGlobalLeaderboardId()), 10);
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Success!!", "onStop(): disconnecting");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("ffffffuuuuuuuuuu", connectionResult.toString());

        try {
            connectionResult.startResolutionForResult(this, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.d("ERROR", "Resolution failed");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.d("INFO", "Result ok");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.d("ERROR", "Result not ok");
                        break;
                    default:
                        break;
                }
    }
}

    /** Called when the user clicks the Send button */
//    public void sendMessage(View view) {
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
//    }

