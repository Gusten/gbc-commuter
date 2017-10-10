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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
            notificationService.showEmptyNotification();
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
        trackedRoutes.add(new TrackedRoute("Elisedal", "9021014002210000", "Valand", "9021014007220000", "4"));
        trackedRoutes.add(new TrackedRoute("Pilbågsgatan", "9021014005280000", "Vasaplatsen", "9021014007300000", "19"));
        //trackedRoutes.add(new TrackedRoute("Hjalmar Brantingsplatsen", "9021014003180000", "Vasaplatsen", "9021014007300000", "10"));
        trackedRouteIndex = 0;

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
        final String toStopId = flipRoute ? route.getFromStopId() : route.getToStopId();
        final String fromName = flipRoute ? route.getTo() : route.getFrom();
        final String toName = flipRoute ? route.getFrom() : route.getTo();

        networkManager.fetchDepartures(fromStopId, toStopId,
                new NetworkManager.DeparturesRequest() {
                    @Override
                    public void onRequestCompleted(List<Departure> departures) {
                        List<Departure> trackedDepartures = new ArrayList<>();

                        int index = 0;
                        Iterator<Departure> iterator = departures.iterator();
                        while(index < 2 && iterator.hasNext()) {
                            Departure nextDeparture = iterator.next();
                            if (route.tracks(nextDeparture)) {
                                trackedDepartures.add(nextDeparture);
                                index++;
                            }
                        }

                        notificationService.showNotification(fromName, toName, trackedDepartures);
                    }

                    @Override
                    public void onRequestFailed() {
                        notificationService.showEmptyNotification();
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
