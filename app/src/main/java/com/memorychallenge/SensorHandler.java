package com.memorychallenge;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorHandler implements SensorEventListener {

    public interface TiltListener {
        void onTiltRight();
        void onTiltLeft();
        void onRotationCompleted();
    }

    private final SensorManager manager;
    private final Sensor accelerometer;
    private final Sensor gyroscope;
    private TiltListener listener;
    private float rotationAngle = 0;
    private static final float ROTATION_THRESHOLD = 90f; // Rotation de 90 degrés
    private int rotationCount = 0;
    private long lastGyroTimestamp = 0;
    private static final String TAG = "SensorHandler";

    public SensorHandler(Context ctx) {
        manager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    public void setListener(TiltListener l) {
        listener = l;
    }

    public void register() {
        if (accelerometer != null) {
            manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        if (gyroscope != null) {
            manager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void unregister() {
        manager.unregisterListener(this);
    }

    public void resetRotationCount() {
        rotationCount = 0;
        rotationAngle = 0;
        lastGyroTimestamp = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            if (x > 7 && listener != null) listener.onTiltLeft();
            if (x < -7 && listener != null) listener.onTiltRight();
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            if (lastGyroTimestamp != 0) {
                // Calcul du dt réel entre les événements
                float dt = (event.timestamp - lastGyroTimestamp) * 1.0f / 1000000000.0f; // Conversion en secondes
                
                // Intégration de la vitesse angulaire pour obtenir l'angle
                float rotationSpeed = event.values[2]; // Rotation autour de l'axe Z
                rotationAngle += Math.toDegrees(rotationSpeed) * dt;

                if (Math.abs(rotationAngle) >= ROTATION_THRESHOLD) {
                    rotationCount++;
                    Log.d(TAG, "Rotation détectée ! Compte : " + rotationCount);
                    rotationAngle = 0;
                    
                    if (rotationCount >= 2 && listener != null) {
                        Log.d(TAG, "Deux rotations complétées !");
                        listener.onRotationCompleted();
                        rotationCount = 0;
                    }
                }
            }
            lastGyroTimestamp = event.timestamp;
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
