package com.se_au.stars;

//import java.util.Timer
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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.se_au.stars.R;

public class AndroidAccelerometer extends Activity
        implements SensorEventListener {
    public final static String EXTRA_MESSAGE = "com.se_au.stars.MESSAGE";

    // Fragments
    WinFragment mWinFragment;

    private float lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaZMax = 0;
    private float deltaZ = 0;
    private float vibrateThreshold = 0;
    private TextView currentZ, maxZ;

    public Vibrator v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_accelerometer);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }


        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

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
        deltaZMax = 0.0f;
        maxZ.setText("0.0");
    }

    public void show_highscore(View v) {

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
//}

