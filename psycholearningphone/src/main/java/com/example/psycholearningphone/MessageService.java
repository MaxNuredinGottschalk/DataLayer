package com.example.psycholearningphone;

import android.content.Intent;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class MessageService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/my_path")) {

            //retrieve possible messages from the datalayer

            final String message = new String(messageEvent.getData());

            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("data", message);

            this.sendBroadcast(messageIntent);

        }

        else {

            super.onMessageReceived(messageEvent);
        }

    }

}
