package com.se_au.stars;

import android.os.Vibrator;
import android.util.Log;

import static java.lang.Math.abs;

public class HeightCalculator {
    private float lastZ;

    private int start;

    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];

    private long timestamp;
    private double v0;

    public Vibrator v;

    public HeightCalculator(){
        start = 0;
        timestamp = 0;
        v0 = 0;
    }

    double lin_acc = 0;


    public double Calculate(float[] values) {
        final double alpha = .8;
        float deltaZ = abs(lastZ - values[2]);
        lastZ = values[2];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];

        linear_acceleration[0] = values[0] - gravity[0];
        linear_acceleration[1] = values[1] - gravity[1];
        linear_acceleration[2] = values[2] - gravity[2];

        double norm = 0;
        double maxH;
        for (int i = 0; i < 3; ++i) {
            norm += gravity[i]*gravity[i];
        }
        norm = Math.sqrt(norm);
        double lin_acc2 = 0;
        for (int i = 0; i < 3; ++i) {
            lin_acc2 += linear_acceleration[i] * gravity[i] / norm;
        }
        lin_acc = alpha*lin_acc + (1-alpha)*lin_acc2;
        if (abs(lin_acc) > 1)
            Log.d(LogLevel.Info, String.valueOf(lin_acc));
        if (start == 0) {
            if (lin_acc > 1) {
//            return lin_acc;
//            Log.d(LogLevel.Info, String.valueOf(lin_acc));

                timestamp = System.currentTimeMillis();
//                v0 = lin_acc * 0.15;
//                start = 1;
            }
            if (lin_acc < -1){
                start = 1;
            }
        } else {
            if (start == 1 && System.currentTimeMillis() - timestamp > 0.1 && Math.abs(lin_acc) < 1){
                start = 2;
                long timeDelta = (System.currentTimeMillis() - timestamp) / 2;
                maxH = getMaxHeight(v0, timeDelta);
//                Log.d(LogLevel.Info, String.valueOf(maxH));
                return abs(maxH);
            }
        }

        return 0;
    }

    public void Reset(){
        start = 0;
//        for (int i = 0; i < 3; ++i) {
//            linear_acceleration[i] = 0;
//            gravity[i] = 0;
//        }
        lin_acc=0;
    }

    private Double getMaxHeight(double v0, long timeDelta) {
        final double g = 9.81;

        double time = timeDelta / 1000.;
        return g * time * time /2;
//        double vel = v0 * time;
//        return v0 * v0 / (2 * g);
    }
}

