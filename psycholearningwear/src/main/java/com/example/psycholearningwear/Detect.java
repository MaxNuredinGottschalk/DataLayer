package com.example.psycholearningwear;

import android.content.BroadcastReceiver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import android.view.View;


public class Detect extends WearableActivity implements SensorEventListener {

    private TextView textView;
    Button talkButton;
    boolean highest = false; //situation from phone-sensor
    boolean release = false; //determination, whether user has released ball at highest point or not
    boolean start = false; //ball has started to be thrown
    int stage = 0; //stages 0 to 4 to determine start and end of a jump
    SensorManager sensorManager;
    Sensor accel;
    double y; //y-acceleration of handheld


    //sounds to determine hit or miss of a shot
    MediaPlayer mpHit = new MediaPlayer();
    MediaPlayer mpMiss = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        textView = findViewById(R.id.text);
        talkButton = findViewById(R.id.button);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keep screen on

        //declare acceleration sensor
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        //register a receiver to get data from the handheld
        IntentFilter newFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();

        this.registerReceiver(messageReceiver, newFilter);

        mpHit = MediaPlayer.create(this,R.raw.hit);
        mpMiss = MediaPlayer.create(this,R.raw.miss);


    }


    public void onDestroy() {

        sensorManager.unregisterListener(this, accel); //unregister sensor
        super.onDestroy();

    }

    public void register(View view) {

        //on button-click: register the sensor and determine start of jump
        //reset values

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        String s = "jump.";
        textView.setText(s);
        stage = 0;
        start = false;


    }

    public void onAccuracyChanged(Sensor sensor, int i) {


    }

    public void onSensorChanged(SensorEvent event) {

        //check acceleration of smartwatch

        //TODO stage 1 maybe not recognized

        double x = event.values[0]; //vertical acceleration

        if (stage != 0) { //if jump has started

            if (x > 1) { //if throw has started

                start = true;
            }

            if (start) {

                if (x < 1 && x > -1) { //if throw has ended, vertical acceleration should be near 0

                    release = highest && stage == 2; //if ball has been released at the highest point, release = true
                    feedback(); //send feedback based on time the ball has been thrown
                    stage = 0;
                    start = false;
                }

            }

        }

    }

    public void feedback() {

        int check = stage;
        switch (check) { //check stage to determine time the ball has been thrown

            case 1:
                String s = "Too early.";
                mpMiss.start();
                textView.setText(s);

            case 3:
                String s2 = "Too late.";
                mpMiss.start();
                textView.setText(s2);

            case 0:
                String s3 = "Too late.";
                mpMiss.start();
                textView.setText(s3);

            case 2:
                if (release) {

                    String s4 = "In time.";
                    mpHit.start();
                    textView.setText(s4);
                } else {
                    String s5 = "Too late.";
                    mpMiss.start();
                    textView.setText(s5);
                }

        }

    }

    public class Receiver extends BroadcastReceiver {

        //receiver to get data from the handheld

        @Override
        public void onReceive(Context context, Intent intent) {

            String s = intent.getStringExtra("message");

            int index = 0;
            for(int i=0; i<s.toCharArray().length; i++){ //get index of start of y-acceleration in string

                if(s.charAt(i) == 'e'){ //"falsE" or "truE"

                    index = i+1;
                }
            }

            String val = "";

            for(int i = index; i<s.toCharArray().length; i++){ //get y-acceleration

                val += (s.charAt(i));
            }

            stage = Integer.parseInt(String.valueOf(s.charAt(0))); //get stage
            highest = Boolean.parseBoolean(s.replaceAll("[^A-Za-z]", "")); //get boolean value of having reached the highest point
            y = Double.parseDouble(val); //y-acceleration

        }

    }

}