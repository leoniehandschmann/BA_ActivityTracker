package com.example.activitytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyDataExport")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Erinnerung zum Daten Export")
                .setContentTitle("Hast du deine Daten heute schon bewertet und exportiert?")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            context.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS));
            return;
        }
        notificationManager.notify(200, builder.build());

    }
}
