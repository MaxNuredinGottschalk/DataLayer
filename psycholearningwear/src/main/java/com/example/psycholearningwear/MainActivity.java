package com.example.psycholearningwear;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;

public class MainActivity extends WearableActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Enables Always-on
        setAmbientEnabled();
    }

    public void detect(View v){

        //start activity to detect jump

        Intent intent = new Intent(this,Detect.class);
        startActivity(intent);

    }

    public void train(View v){

        //start activity to train neural network

        Intent intent = new Intent(this,Learn.class);
        startActivity(intent);
    }


}
