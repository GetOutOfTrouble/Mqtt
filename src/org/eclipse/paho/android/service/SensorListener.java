package org.eclipse.paho.android.service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class SensorListener implements SensorEventListener {
    private static SensorListener s = null;
    float x;
    float y;
    float z;

    private SensorListener() {
    }


    public static SensorListener getListener() {
        if (s == null) {
            s = new SensorListener();
        }
        return s;
    }
    public String getInfo() {
        String info = "x="+this.x+";y="+this.y+";z="+this.z;
        return info;
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        this.x = event.values[0];
        this.y = event.values[1];
        this.z = event.values[2];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
