package pub.gusten.gbgcommuter.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import pub.gusten.gbgcommuter.NetworkManager;
import pub.gusten.gbgcommuter.ScreenReceiver;
import pub.gusten.gbgcommuter.models.Departure;
import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.models.TrackedRoute;

public class TrackerService extends Service {

    private final String PREFS_REF = "trackerService";

    private NetworkManager networkManager;
    private BroadcastReceiver screenReceiver;
    private List<TrackedRoute> trackedRoutes;
    private int trackedRouteIndex;
    private boolean flipRoute;
    private SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private NotificationService notificationService;
    private boolean hasBoundNotification;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundNotification = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundNotification = true;
            NotificationService.LocalBinder mLocalBinder = (NotificationService.LocalBinder)service;
            notificationService = mLocalBinder.getService();
            notificationService.showNotification("", "", "", "");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new TrackerService.LocalBinder();

    public class LocalBinder extends Binder {
        TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
    public void onCreate() {
        networkManager = new NetworkManager(this);
        startService(new Intent(this, NotificationService.class));
        bindService(new Intent(this, NotificationService.class), serviceConnection, BIND_AUTO_CREATE);

        trackedRoutes = new ArrayList<>();
        trackedRoutes.add(new TrackedRoute("elisedal", "9021014002210000", "Mölndal", "valand", "9021014007220000", "Angered", "4"));
        trackedRoutes.add(new TrackedRoute("pilbågsgatan", "9021014005280000", "Fredriksdal", "vasaplatsen", "9021014007300000", "Backa via Kungsportsplatsen", "19"));
        trackedRouteIndex = 0;

        fullDateFormat.setTimeZone(TimeZone.getDefault());

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenReceiver = new ScreenReceiver();
        registerReceiver(screenReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!hasBoundNotification || !intent.hasExtra("action") || trackedRoutes.size() == 0) {
            return START_STICKY;
        }

        NotificationAction result = (NotificationAction)intent.getSerializableExtra("action");
        switch (result) {
            case PREVIOUS:
                trackedRouteIndex = trackedRouteIndex - 1 < 0 ? trackedRoutes.size() - 1 : trackedRouteIndex - 1;
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            case NEXT:
                trackedRouteIndex = (trackedRouteIndex + 1) % trackedRoutes.size();
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            case FLIP:
                flipRoute = !flipRoute;
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            case UPDATE:
                trackRoute(trackedRoutes.get(trackedRouteIndex));
                break;
            default:
                break;
        }
        return START_STICKY;
    }

    private void trackRoute(final TrackedRoute route) {
        final String fromStopId = flipRoute ? route.getToStopId() : route.getFromStopId();
        final String fromName = flipRoute ? route.getTo() : route.getFrom();
        final String toStopId = flipRoute ? route.getFromStopId() : route.getToStopId();
        final String toName = flipRoute ? route.getFrom() : route.getTo();

        networkManager.fetchDepartures(fromStopId, toStopId,
                new NetworkManager.DeparturesRequest() {
                    @Override
                    public void onRequestCompleted(List<Departure> departures) {
                        String[] timesTilDeparture = {"", ""};
                        int index = 0;
                        for (Departure departure: departures) {
                            if (route.tracks(departure, flipRoute)) {
                                try {
                                    Date tmpDate = fullDateFormat.parse(departure.getDate() + " " + departure.getTime());
                                    long timeDiff = tmpDate.getTime() - new Date().getTime();
                                    timesTilDeparture[index] = "" + timeDiff / (60 * 1000);
                                    index++;
                                    if (index > 1) {
                                        break;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        notificationService.showNotification(fromName + " -> " + toName, timesTilDeparture[0], timesTilDeparture[1], route.getLine());
                    }

                    @Override
                    public void onRequestFailed() {
                        notificationService.showNotification("Could not fetch departures", "", "", "");
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        unregisterReceiver(screenReceiver);
    }
}
