package com.se_au.stars;

import android.util.Log;

import static java.lang.Math.abs;

public class HeightCalculator {
    private final static double g = 9.81;

    private int mIsGameStarted;

    private double[] mGravity = new double[3];
    private double[] mLinearAcceleration = new double[3];
    private double mLinAcc = 0;

    private long mTimestamp;

    public HeightCalculator(){
        mIsGameStarted = 0;
        mTimestamp = 0;
    }

    public double Calculate(float[] values) {
        final double alpha = .8;

        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * values[0];
        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * values[1];
        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * values[2];

        mLinearAcceleration[0] = values[0] - mGravity[0];
        mLinearAcceleration[1] = values[1] - mGravity[1];
        mLinearAcceleration[2] = values[2] - mGravity[2];

        double norm = 0;
        double maxH;
        for (int i = 0; i < 3; ++i) {
            norm += mGravity[i]* mGravity[i];
        }
        norm = Math.sqrt(norm);
        double lin_acc2 = 0;
        for (int i = 0; i < 3; ++i) {
            lin_acc2 += mLinearAcceleration[i] * mGravity[i] / norm;
        }
        mLinAcc = alpha* mLinAcc + (1-alpha)*lin_acc2;
        if (abs(mLinAcc) > 1)
            Log.d(LogLevel.Info, String.valueOf(mLinAcc));
        if (mIsGameStarted == 0) {
            if (mLinAcc > 1) {
                mTimestamp = System.currentTimeMillis();
            }
            if (mLinAcc < -1){
                mIsGameStarted = 1;
            }
        } else {
            if (mIsGameStarted == 1) {
                long timeDelta = (System.currentTimeMillis() - mTimestamp) / 2;
                if (Math.abs(mLinAcc) < 1 && timeDelta > 0.1) {
                    mIsGameStarted = 2;
                    maxH = getMaxHeight(timeDelta);
                    mTimestamp = System.currentTimeMillis();
                    return abs(maxH);
                }
                if (mLinAcc > 1){
                    mIsGameStarted = 0;
                }
            }
        }

        return 0;
    }

    public void Reset(){
        mIsGameStarted = 0;
        mLinAcc =0;
    }

    private Double getMaxHeight(long timeDelta) {
        double time = timeDelta / 1000.;
        return g * time * time /2;
    }
}
