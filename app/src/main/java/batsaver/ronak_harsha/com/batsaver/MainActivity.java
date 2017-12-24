package batsaver.ronak_harsha.com.batsaver;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.app.Service.START_STICKY;
import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;

public class MainActivity extends Activity {

    SensorManager sensorManager;
    Sensor accelerometerSensor;
    Sensor temperatureSensor, proximitySensor;
    boolean accelerometerSensorPresent;
    boolean temperatureSensorPresent = true;
    boolean proximitySensorPresent = true;
    float currTemp;
    Button bsetEmergNum,bStartCalCount;

    AudioManager am;

    TextView face;
    TextView tmpDisplay,tmpBool,tmpproxBool;
    EditText etEmergNum,etWeight;
    String number;
    String weight;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        face = (TextView)findViewById(R.id.tPhoneFace);
        tmpBool = (TextView)findViewById(R.id.tTempBool);
        tmpDisplay = (TextView)findViewById(R.id.tTempDisplay);
        tmpproxBool = (TextView) findViewById(R.id.tProxBool);
        etEmergNum = (EditText)findViewById(R.id.tEmergNum);
        etWeight = (EditText)findViewById(R.id.tWeight);
        bsetEmergNum = (Button)findViewById(R.id.btSetEmergNum);
        bStartCalCount = (Button)findViewById(R.id.btStartCalorieCount);

        bsetEmergNum.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                number = etEmergNum.getText().toString();
                if(number.matches(""))
                    Toast.makeText(getApplicationContext(),
                            "Emergency Number cannot be empty",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(),
                            "Emergency Number saved", Toast.LENGTH_SHORT).show();
                etEmergNum.setText("");
            }
        });


        bStartCalCount.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    weight = etWeight.getText().toString();
                    if(weight.matches(""))
                        Toast.makeText(getApplicationContext(),
                            "Weight cannot be empty",Toast.LENGTH_SHORT).show();
                    else {
                        Intent mIntent = new Intent(MainActivity.this, CalorieCalculator.class);
                        mIntent.putExtra("Weight",weight);
                        startActivity(mIntent);
                    }
            }
        });


        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        if(sensorList.size() > 0){
            accelerometerSensorPresent = true;
            accelerometerSensor = sensorList.get(0);
        }
        else{
            accelerometerSensorPresent = false;
            face.setText("No accelerometer present!");
        }
        if(proximitySensor == null) {
            Log.e(TAG, "Proximity sensor not available.");
            tmpproxBool.setText("Proximity sensor not available");
            proximitySensorPresent = false;
            //finish(); // Close app
        }
        else{
            tmpproxBool.setText("Proximity sensor is available");
        }
        if(temperatureSensor == null) {
            Log.e(TAG, "Temperature sensor not available.");
            tmpBool.setText("Temperature sensor not available.");
            temperatureSensorPresent = false;
            //finish(); // Close app
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(accelerometerSensorPresent){
            sensorManager.registerListener(accelerometerListener, accelerometerSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(temperatureSensorPresent){
            sensorManager.registerListener(temperatureSensorListener,
                    temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if(proximitySensorPresent){
            sensorManager.registerListener(proximitySensorListener,
                    proximitySensor,2*1000*1000);
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if(accelerometerSensorPresent){
            sensorManager.unregisterListener(accelerometerListener);
        }
        if(proximitySensorPresent){
            sensorManager.unregisterListener(proximitySensorListener);
        }
        if(temperatureSensorPresent){
            sensorManager.unregisterListener(temperatureSensorListener);
        }
    }

    private SensorEventListener accelerometerListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            // TODO Auto-generated method stub

            //For accelerometer
            float z = arg0.values[2];
            WindowManager.LayoutParams params = getWindow().getAttributes();
            float x,y;
            if (z >= 0){
                face.setText("Phone Face Up");
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = -1f;
                getWindow().setAttributes(params);
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
            else{
                face.setText("Phone Face Down");
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = 0;
                getWindow().setAttributes(params);
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            }
        }
    };



    SensorEventListener proximitySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.values[0] < proximitySensor.getMaximumRange()) {
                // Detected something nearby

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                //callIntent.setData(Uri.parse("tel:16319979258"));

                if(number != null && !number.isEmpty() && !number.matches("")) {
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "Emergency Contact Not Saved",Toast.LENGTH_SHORT).show();
                }

            } else {
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };



    SensorEventListener temperatureSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            currTemp = sensorEvent.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };
}

