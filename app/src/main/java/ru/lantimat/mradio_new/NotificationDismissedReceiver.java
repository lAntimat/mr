package ru.lantimat.mradio_new;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("deleteIntent");
      /* Your code to handle the event here */

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MediaPlayerService.ACTION_STOP_SELF);
        context.sendBroadcast(broadcastIntent);
        Log.d("Notification", "Received");

    }
}
