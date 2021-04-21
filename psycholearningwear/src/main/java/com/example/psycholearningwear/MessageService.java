package com.example.psycholearningwear;

import com.google.android.gms.wearable.MessageEvent;
import android.content.Intent;

import com.google.android.gms.wearable.WearableListenerService;

public class MessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent){

        //retrieve messages

        if(messageEvent.getPath().equals("/my_path")){

            final String message = new String(messageEvent.getData());
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("data", message);

            this.sendBroadcast(messageIntent);

        }

        else{

            super.onMessageReceived(messageEvent);

        }

    }

}
