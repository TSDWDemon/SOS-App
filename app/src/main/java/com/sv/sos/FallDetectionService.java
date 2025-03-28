package com.sv.sos;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class FallDetectionService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;

    // Fall detection variables
    private static final float FALL_THRESHOLD = 15.0f;
    private static final int TIME_BETWEEN_SAMPLES_MS = 100;
    private long lastUpdate = 0;
    private float lastX, lastY, lastZ;

    public FallDetectionService(MainActivity mainActivity, SensorManager sensorManager, Sensor accelerometer) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastUpdate) > TIME_BETWEEN_SAMPLES_MS) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = Math.abs(x + y + z - lastX - lastY - lastZ);

                if (acceleration > FALL_THRESHOLD) {
                    detectFall();
                }

                lastX = x;
                lastY = y;
                lastZ = z;
                lastUpdate = currentTime;
            }
        }
    }

    private void detectFall() {
        vibrator.vibrate(500); // Vibrate for 500ms
        // Trigger emergency protocol
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("emergency", true);
        startActivity(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}
