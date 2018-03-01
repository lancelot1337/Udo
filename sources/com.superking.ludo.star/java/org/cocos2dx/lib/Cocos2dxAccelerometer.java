package org.cocos2dx.lib;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.view.WindowManager;

public class Cocos2dxAccelerometer implements SensorEventListener {
    static final float ALPHA = 0.25f;
    private static final String TAG = Cocos2dxAccelerometer.class.getSimpleName();
    final float[] accelerometerValues = new float[3];
    final float[] compassFieldValues = new float[3];
    private final Sensor mAccelerometer;
    private final Sensor mCompass;
    private final Context mContext;
    private final int mNaturalOrientation;
    private final SensorManager mSensorManager;

    public static native void onSensorChanged(float f, float f2, float f3, long j);

    public Cocos2dxAccelerometer(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        this.mCompass = this.mSensorManager.getDefaultSensor(2);
        this.mNaturalOrientation = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getOrientation();
    }

    public void enableCompass() {
        this.mSensorManager.registerListener(this, this.mCompass, 1);
    }

    public void enableAccel() {
        this.mSensorManager.registerListener(this, this.mAccelerometer, 1);
    }

    public void setInterval(float interval) {
        if (VERSION.SDK_INT < 11) {
            this.mSensorManager.registerListener(this, this.mAccelerometer, 1);
        } else {
            this.mSensorManager.registerListener(this, this.mAccelerometer, (int) (100000.0f * interval));
        }
    }

    public void disable() {
        this.mSensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == 1) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            this.accelerometerValues[0] = x;
            this.accelerometerValues[1] = y;
            this.accelerometerValues[2] = z;
            int orientation = this.mContext.getResources().getConfiguration().orientation;
            float tmp;
            if (orientation == 2 && this.mNaturalOrientation != 0) {
                tmp = x;
                x = -y;
                y = tmp;
            } else if (orientation == 1 && this.mNaturalOrientation != 0) {
                tmp = x;
                x = y;
                y = -tmp;
            }
            Cocos2dxGLSurfaceView.queueAccelerometer(x, y, z, sensorEvent.timestamp);
        } else if (sensorEvent.sensor.getType() == 2) {
            this.compassFieldValues[0] = sensorEvent.values[0];
            this.compassFieldValues[1] = sensorEvent.values[1];
            this.compassFieldValues[2] = sensorEvent.values[2];
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
