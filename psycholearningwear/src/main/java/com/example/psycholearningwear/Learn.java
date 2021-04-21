package com.example.psycholearningwear;

import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
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


public class Learn extends WearableActivity implements SensorEventListener {

    private TextView textView;
    Button talkButton;
    boolean highest = false; //situation from phone-sensor
    boolean release = false; //determination, whether user has released ball at highest point or not
    boolean start = false; //ball has started to be thrown
    int stage = 0; //stages 0 to 4 to determine start and end of a jump
    SensorManager sensorManager;
    Sensor accel;
    double y; //y-acceleration of handheld
    double yOnThrow; //y-acceleration on start of throw
    double yOnThrow2; //y-acceleration on end of throw
    NeuralNetwork net;
    int index; //index of maximum output from neural net
    boolean feed = false; //boolean to determine whether net has been fed or not

    //sounds to determine hit or miss of a shot
    MediaPlayer mpHit = new MediaPlayer();
    MediaPlayer mpMiss = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        textView = findViewById(R.id.text);
        talkButton = findViewById(R.id.button);

        net = new NeuralNetwork(3,4,2); //create neural network to make predictions

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

        SharedPreferences prefs = getSharedPreferences("com.example.psycholearningwear",
                Context.MODE_PRIVATE);

        int key = 0; //get saved net-weights from shared preferences
        for(int i=0; i<net.get_input_layer().length; i++){

            for(int j=0; j<net.get_input_layer()[i].return_weights().length; j++){

                net.get_input_layer()[i].set_specific_weight(j,
                        Double.parseDouble(prefs.getString(Integer.toString(key),"")));
                key += 1;

            }
        }

        for(int i=0; i<net.get_hidden_layer().length; i++){

            for(int j=0; j<net.get_hidden_layer()[i].return_weights().length; j++){

                net.get_hidden_layer()[i].set_specific_weight(j,
                        Double.parseDouble(prefs.getString(Integer.toString(key),"")));
                key += 1;
            }
        }

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

                yOnThrow = y;
                start = true;
            }

            if (start) {

                if (x < 1 && x > -1) { //if throw has ended, vertical acceleration should be near 0

                    yOnThrow2 = y;
                    release = highest && stage == 2; //if ball has been released at the highest point, release = true
                    feedback(); //send feedback based on time the ball has been thrown
                    predict(); //feed forward net and make prediction
                    stage = 0;
                    start = false;
                }

            }

        }

    }

    public void saveNet(){

        //save current net-weights into shared preferences

        SharedPreferences prefs = this.getSharedPreferences("com.example.psycholearningwear",
                Context.MODE_PRIVATE);

        int key = 0;
        for(int i=0; i<net.get_input_layer().length;i ++){

            for(int j=0; j<net.get_input_layer()[i].return_weights().length; j++) {

                String s = Double.toString(net.get_input_layer()[i].return_weights()[j]);
                prefs.edit().putString(Integer.toString(key),s).apply();
                key += 1;

            }
        }

        for(int i=0; i<net.get_hidden_layer().length; i++){

            for(int j=0; j<net.get_hidden_layer()[i].return_weights().length; j++){

                String s = Double.toString((net.get_hidden_layer()[i].return_weights()[j]));
                prefs.edit().putString(Integer.toString(key),s).apply();
                key += 1;

            }
        }

    }

    public void predict(){

        //put input for feed_forward and make prediction

        double [] input = new double[4];
        double dStage = (double) stage;
        input[0] = dStage; //TODO possible feedback, suggestions, visualization, dashboard, different input
        input[1] = yOnThrow;
        input[2] = yOnThrow2;
        input[3] = 1.0;
        net.feed_forward(input);

        double max = 0.0;
        for(int i=0; i< net.get_outputs().length; i++){ //get maximum output and make prediction based on whats the maximum

            if(net.get_outputs()[i] > max){

                max = net.get_outputs()[i];
                index = i;
            }
        }

        String s = "Shot has been made?";
        String s2 = "Shot has been missed?";

        if(index == 0){

            textView.append("\n" + s);

        }

        else if(index == 1){

            textView.append("\n" + s2);
        }

        feed = true;

    }

    public void yes(View v){

        //approve prediction and backpropagate with current outputs

        if(feed){

            //set target of outputs based on maximum output-index
            if(index == 0){

                net.get_output_layer()[0].set_target(1);
                net.get_output_layer()[1].set_target(0);
            }

            if(index == 1){

                net.get_output_layer()[0].set_target(0);
                net.get_output_layer()[1].set_target(1);
            }

            net.back_prop(net.get_outputs()); //update weights
            textView.append("OK"); //notify, that net has backpropagated
        }

        feed = false;
        saveNet(); //save net-weights into preferences
    }

    public void no(View v){

        //disapprove prediction and backpropagate with current outputs


        if(feed) {

            //set target of outputs based on maximum output-index

            if (index == 0) {

                net.get_output_layer()[0].set_target(0);
                net.get_output_layer()[1].set_target(1);
            }

            if (index == 1) {

                net.get_output_layer()[0].set_target(1);
                net.get_output_layer()[1].set_target(0);
            }

            net.back_prop(net.get_outputs()); //update weights
            textView.append("OK"); //notify, that net has backpropagated
        }

        feed = false;
        saveNet(); //save net-weights into preferences
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