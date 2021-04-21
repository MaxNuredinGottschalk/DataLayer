package com.example.psycholearningphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView text; //display acceleration
    protected Handler myHandler;
    boolean highest = false; //highest point
    int stage = 0; //stages 0-4 to define start and end of jump
    boolean second = false; //check if jump has ended


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.textView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keep screen on


        SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this,accel,SensorManager.SENSOR_DELAY_FASTEST); //register acceleration sensor


        myHandler = new Handler(new Handler.Callback() {

            //handler to retrieve and obtain a message

            @Override
            public boolean handleMessage(@NonNull Message msg){

                return true;
            }
        });

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){


    }

    public void onSensorChanged(SensorEvent event){

        //check acceleration and send to wearable

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){


            double y = event.values[1]; //acceleration on y axis(vertical)(should be (near)0, when reaching highest point)
            String s = Double.toString(y);
            highest = y < 1 && y > -1; //mean value to when y is near 0, might change later


            //TODO maybe adjust value to determine when jump started
            if(stage == 0 && y > 2 || y < -2){stage = 1;} //if jump has started

            if(stage == 3){ //person is landing

                text.setText(s); //display acceleration
                highest = y < 1 && y > -1; //mean value to when y is near 0, might change later


                String msg = Integer.toString(stage) + Boolean.toString(highest) + Double.toString(y); //send stage, boolean and acceleration
                new DataThread("/my_path", msg).start(); //start Thread to send data to wearable

                if(!highest){

                    second = true;
                }

                if(second){

                    if(highest){ //person has landed

                        stage = 0;
                        second = false;

                    }
                }

            }


            if(stage == 2){ //highest point has been reached

                if(!highest){ //person is landing

                    stage = 3;
                    return;
                }

                text.setText(s); //display acceleration
                highest = y < 1 && y > -1; //mean value to when y is near 0, might change later

                String msg = Integer.toString(stage) + Boolean.toString(highest) + Double.toString(y); //send stage, boolean and acceleration
                new DataThread("/my_path", msg).start(); //start Thread to send data to wearable


            }


            if(stage==1) { //jump has started

                if(highest){ //highest point has been reached

                    stage = 2;
                    return;

                }

                text.setText(s); //display acceleration
                highest = y < 1 && y > -1; //mean value to when y is near 0, might change later

                String msg = Integer.toString(stage) + Boolean.toString(highest) + Double.toString(y); //send stage, boolean and acceleration
                new DataThread("/my_path", msg).start(); //start Thread to send data to wearable


            }

        }

    }


    public void sendmsg(String msgtxt){

        //send the message to the handler

        Bundle bundle = new Bundle();
        bundle.putString("message", msgtxt);
        Message msg = myHandler.obtainMessage();
        msg.setData(bundle);
        myHandler.sendMessage(msg);

    }

    class DataThread extends Thread {

        //Thread to send acceleration data with a path to the wearable

        String path;
        String msg;

        DataThread(String p, String m){

            path = p;
            msg = m;
        }

        public void run(){

            //check nodes to get connected wearable and send it the data

            Task<List<com.google.android.gms.wearable.Node>> wearableList = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

            try {

                List<com.google.android.gms.wearable.Node> nodes = Tasks.await(wearableList);

                for(com.google.android.gms.wearable.Node node: nodes){

                    Task<Integer> sendmsgTask =
                            Wearable.getMessageClient(MainActivity.this).sendMessage(node.getId(),path,msg.getBytes());
                    try{

                        Integer result = Tasks.await(sendmsgTask);
                        sendmsg(Boolean.toString(highest));

                    }  catch (ExecutionException exception) {


                    }  catch (InterruptedException exception){


                    }

                }


            }  catch (ExecutionException exception) {



            }  catch (InterruptedException exception) {



            }

        }

    }

}
