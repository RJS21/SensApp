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

public class CalorieCalculator extends Activity implements SensorEventListener,Listener {
    private Detector detector;
    private SensorManager sensorManager;
    private Sensor sensor;
    private int numSteps;
    TextView TvSteps,TvCalories;
    Button BtnStart,BtnStop;
    float w;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calorie_count_display);
        Bundle extras = getIntent().getExtras();
        String weight = extras.getString("Weight");
        w = Float.parseFloat(weight);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        detector = new Detector();
        detector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        TvCalories = (TextView)findViewById(R.id.tv_calories);
        BtnStart = (Button) findViewById(R.id.btn_start);
        BtnStop = (Button) findViewById(R.id.btn_stop);



        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                TvSteps.setText("");
                TvCalories.setText("");
                numSteps = 0;
                sensorManager.registerListener(CalorieCalculator.this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });


        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(CalorieCalculator.this);
                float tmp = (float)(w*0.57/2100);
                TvCalories.setText("Number of calories burnt = " + tmp*numSteps);
            }
        });



    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detector.updateAccelReading(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void calculateNumSteps(long timeNs) {
        numSteps++;
        TvSteps.setText("Number of Steps: " + numSteps);
    }

}