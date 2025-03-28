package com.sv.sos;

import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.content.Context;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.os.Looper;
import android.os.Vibrator;

import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.widget.LinearLayout;
import android.telephony.SmsManager;

import com.sv.sos.dbms;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity {
    EditText phoneInput;
    ImageButton saveButton;
    dbms databaseHelper ;
    ImageButton callButton;
    Button viewContactsButton;
    SQLiteDatabase db;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final double COLLISION_THRESHOLD = 20.0; // Adjust as needed
    private Timer collisionTimer;
    private boolean collisionDetected = false;
    private static final long HIGH_SPEED_DELAY = 5000; // 5 seconds for high speed
    private static final long LOW_SPEED_DELAY = 10000;  // 10 seconds for low speed
    private boolean isAlarming = false;
    private Vibrator vibrator;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        databaseHelper = new dbms(this);
        db = databaseHelper.getWritableDatabase();
        setContentView(R.layout.openingpage);
        // Start the FallDetectionService
        Intent serviceIntent = new Intent(this, FallDetectionService.class);
        startService(serviceIntent);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer not available!", Toast.LENGTH_SHORT).show();
            finish();
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, Looper.getMainLooper());
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener, Looper.getMainLooper());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.activity_main); // Set the main layout here


                // Set up window insets listener for the root view
                View rootView = findViewById(R.id.main); // Assuming 'main' is the ID of your root view in activity_main.xml
                ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

                // Set onClickListeners for buttons
                findViewById(R.id.button_police).setOnClickListener(v -> makePhoneCall("100"));
                findViewById(R.id.button_fire).setOnClickListener(v -> makePhoneCall("101"));
                findViewById(R.id.button_ambulance).setOnClickListener(v -> makePhoneCall("102"));
                findViewById(R.id.button_women).setOnClickListener(v -> makePhoneCall("1090"));

                ImageButton saveButton = findViewById(R.id.savebutton);
                ImageButton callButton = findViewById(R.id.phonebutton);
                EditText phoneInput = findViewById(R.id.phoneinput);
                Button viewContactsButton = findViewById(R.id.viewcon);
                phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
                phoneInput.setHint("Enter the phone Number");// Assuming 'numberlayout' is a ConstraintLayout
                ConstraintLayout constraintLayout = findViewById(R.id.main);
                phoneInput.setId(View.generateViewId()); // Generate a unique ID for the EditText
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(30, 30, 30, 30);




                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String phoneNumber = phoneInput.getText().toString();
                        if(!phoneNumber.isEmpty()){
                            ContentValues values = new ContentValues();
                            values.put("phone_number", phoneNumber); // Reference to column name values.put("emergency_contacts",phoneNumber)
                            long newRowId = db.insert("emergency_contacts", null, values); // Reference to table name

                            if (newRowId != -1){
                                Toast.makeText(MainActivity.this,"Saved number",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(MainActivity.this,"Error saving Number",Toast.LENGTH_SHORT).show();
                            }

                        }
                        phoneInput.getText().clear();
                    }
                });
                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String phoneNumber = phoneInput.getText().toString();
                        if (!phoneNumber.isEmpty()) {
                            makePhoneCall(phoneNumber);
                        }

                        phoneInput.getText().clear();
                    }
                });
                viewContactsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Query the database to get all phone numbers
                        Cursor cursor = db.query("emergency_contacts", new String[]{"phone_number"}, null, null, null, null, null);

                        if (cursor.getCount() > 0) {
                            // If there are phone numbers, build a list to display
                            StringBuilder numbers = new StringBuilder();
                            while (cursor.moveToNext()) {
                                String number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                                numbers.append(number).append("\n");
                            }

                            // Display the numbers in an AlertDialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Saved Phone Numbers")
                                    .setMessage(numbers.toString())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User clicked OK button
                                        }
                                    });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        } else {
                            // No numbers found
                            Toast.makeText(MainActivity.this, "No numbers saved", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();
                    }
                });
            }
        }, 2000);
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(x * x + y * y + z * z);
                if (acceleration > COLLISION_THRESHOLD) {
                    detectCollision();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private void detectCollision() {
        if (!collisionDetected) {
            collisionDetected = true;
            isAlarming = true;
            // Vibrate
            vibrator.vibrate(new long[]{0, 1000, 500}, 0);


            // Alarm
            try {
                Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Ringtone ringtone = RingtoneManager.getRingtone(this, alarmUri);
                ringtone.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
            startCollisionTimer();
        }
    }
    private void startCollisionTimer() {
        if (collisionTimer != null) {
            collisionTimer.cancel();
        }
        long delay;

        double speed = 50;
        if (speed > 40) {
            delay = HIGH_SPEED_DELAY;
        } else {
            delay = LOW_SPEED_DELAY;
        }
        collisionTimer = new Timer();
        collisionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (isAlarming) {
                        isAlarming = false;
                        vibrator.cancel();
                        sendEmergencySMS();
                    }
                    collisionDetected = false;
                });
            }
        }, delay);
    }

    private void sendEmergencySMS() {
        // Retrieve phone numbers from the database
        List<String> phoneNumbers = getEmergencyPhoneNumbers();
        String message;
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();
            String googleMapsUrl = "http://maps.google.com/maps?q=" + latitude + "," + longitude;
            message = "Emergency! Collision detected. " +
                    "Location: " + googleMapsUrl;
        } else {
             message = "Emergency! Collision detected. " +
                    "Location: not available";

        }

        if (phoneNumbers.isEmpty()) {
            Toast.makeText(this, "No emergency contacts found.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String number : phoneNumbers) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(number, null, message, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent to " + number, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "SMS failed to " + number, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private List<String> getEmergencyPhoneNumbers() {
        List<String> numbers = new ArrayList<>();
        Cursor cursor = db.query("emergency_contacts", new String[]{"phone_number"}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String number = cursor.getString(cursor.getColumnIndexOrThrow("phone_number"));
                numbers.add(number);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return numbers;
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        if (vibrator != null) {
            vibrator.cancel();
        }
        if(locationManager!=null){
            locationManager.removeUpdates(locationListener);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission if it's not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }
        // Start the call after permission is granted
        startActivity(intent);
    }

}
