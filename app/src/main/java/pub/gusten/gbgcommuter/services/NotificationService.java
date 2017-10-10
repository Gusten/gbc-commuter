package pub.gusten.gbgcommuter.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;

import java.util.List;

import pub.gusten.gbgcommuter.DateUtils;
import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.Departure;
import pub.gusten.gbgcommuter.models.NotificationAction;

public class NotificationService extends Service {
    private NotificationManager notificationManager;
    private final int NOTIFICATION_ID = R.integer.notification_id;
    private String NOTIFICATION_CHANNEL_ID;
    private String NOTIFICATION_CHANNEL_NAME;

    private PendingIntent prevPendingIntent;
    private PendingIntent nextPendingIntent;
    private PendingIntent flipPendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        NOTIFICATION_CHANNEL_ID = getString(R.string.notification_channel_id);
        NOTIFICATION_CHANNEL_NAME = getString(R.string.notification_channel_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Intent for selecting previous route
        Intent prevIntent = new Intent(this, TrackerService.class);
        prevIntent.putExtra("action", NotificationAction.PREVIOUS);
        prevPendingIntent = PendingIntent.getService(
                this,
                0,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Intent for selecting next route
        Intent nextIntent = new Intent(this, TrackerService.class);
        nextIntent.putExtra("action", NotificationAction.NEXT);
        nextPendingIntent = PendingIntent.getService(
                this,
                1,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        // Intent for selecting flipping route
        Intent flipIntent = new Intent(this, TrackerService.class);
        flipIntent.putExtra("action", NotificationAction.FLIP);
        flipPendingIntent = PendingIntent.getService(
                this,
                2,
                flipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void showNotification(String from, String to, List<Departure> departures) {
        if (departures.isEmpty()) {
            showNoDepartures(from, to);
            return;
        }

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);

        // Set time til departure
        Departure nextDeparture = departures.get(0);
        LocalDateTime now = LocalDateTime.now();
        String timeTilDepartureText = Duration.between(now, nextDeparture.getTimeInstant()).toMinutes() + "min";

        if (departures.size() > 1) {
            timeTilDepartureText += "  (" + Duration.between(now, departures.get(1).getTimeInstant()).toMinutes() + "min)";
        }
        contentView.setTextViewText(R.id.notification_timeTilDeparture, timeTilDepartureText);

        contentView.setInt(R.id.notification_line, "setBackgroundColor", getColorFromHex(nextDeparture.getFgColor().substring(1)));
        contentView.setTextViewText(R.id.notification_line, nextDeparture.getLine());
        contentView.setTextColor(R.id.notification_line, getColorFromHex(nextDeparture.getBgColor().substring(1)));
        contentView.setTextViewText(R.id.notification_route, from + "  >>  " + to);
        contentView.setTextViewText(R.id.notification_updatedAt, "Updated: " + DateUtils.timeOnlyFormatter.format(LocalTime.now()));
        contentView.setOnClickPendingIntent(R.id.notification_prevBtn, prevPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_nextBtn, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_flipBtn, flipPendingIntent);

        Notification notification = new NotificationCompat.Builder(this)
                .setChannel(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_24dp)
                .setContent(contentView)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }


    public void showEmptyNotification() {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_empty_notification);
        contentView.setTextViewText(R.id.notification_error, "");
        contentView.setOnClickPendingIntent(R.id.notification_prevBtn, prevPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_nextBtn, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_flipBtn, flipPendingIntent);

        Notification notification = new NotificationCompat.Builder(this)
                .setChannel(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_24dp)
                .setContent(contentView)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void showNoDepartures(String from, String to) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_empty_notification);
        contentView.setTextViewText(R.id.notification_route, from + "  >>  " + to);
        contentView.setTextViewText(R.id.notification_error, "No departures found");
        contentView.setOnClickPendingIntent(R.id.notification_prevBtn, prevPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_nextBtn, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.notification_flipBtn, flipPendingIntent);

        Notification notification = new NotificationCompat.Builder(this)
                .setChannel(NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_train_24dp)
                .setContent(contentView)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private int getColorFromHex(String colorString) {
        int color = (int)Long.parseLong(colorString, 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }
}
