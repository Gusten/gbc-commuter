package pub.gusten.gbgcommuter.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.threeten.bp.LocalDateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.models.Stop;
import pub.gusten.gbgcommuter.models.TrackedRoute;
import pub.gusten.gbgcommuter.models.departures.Departure;
import pub.gusten.gbgcommuter.receivers.ScreenActionReceiver;

public class TrackerService extends Service {

    private final String PREFS_REF = "trackerService";
    private final String PREFS_TRACKEDROUTES = "trackedRoutes";
    private final int MIN_GPS_UPDATE_INTERVAL = 10 * 60 * 1000; // 10 minutes

    private Gson gson;
    private BroadcastReceiver screenReceiver;
    private List<TrackedRoute> trackedRoutes;
    private int trackedRouteIndex;
    private boolean flipRoute;

    private NotificationService notificationService;
    private boolean hasBoundNotification;
    private ServiceConnection notificationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundNotification = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundNotification = true;
            NotificationService.LocalBinder mLocalBinder = (NotificationService.LocalBinder) service;
            notificationService = mLocalBinder.getService();
            trackRoute();
        }
    };

    private ApiService apiService;
    private boolean hasBoundApiService;
    private ServiceConnection apiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundApiService = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundApiService = true;
            ApiService.LocalBinder mLocalBinder = (ApiService.LocalBinder) service;
            apiService = mLocalBinder.getService();
            trackRoute();
        }
    };

    private boolean isTracking;
    private boolean gpsEnabled;
    private Location lastKnownLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new TrackerService.LocalBinder();

    public class LocalBinder extends Binder {
        public TrackerService getService() {
            return TrackerService.this;
        }
    }

    @Override
    public void onCreate() {
        gson = new Gson();
        isTracking = true;
        trackedRoutes = new ArrayList<>();
        trackedRouteIndex = 0;

        // Fetch tracked routes from shared prefs
        SharedPreferences prefs = getSharedPreferences(PREFS_REF, MODE_PRIVATE);
        String storedJsonRoutes = prefs.getString(PREFS_TRACKEDROUTES, "[]");
        trackedRoutes = gson.fromJson(storedJsonRoutes, new TypeToken<List<TrackedRoute>>(){}.getType());

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenReceiver = new ScreenActionReceiver();
        registerReceiver(screenReceiver, intentFilter);

        // Are we allowed to start location tracking?
        int grantedStatus = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (grantedStatus == PackageManager.PERMISSION_GRANTED) {
            startLocationTracking();
        }

        startService(new Intent(this, NotificationService.class));
        bindService(new Intent(this, NotificationService.class), notificationServiceConnection, BIND_AUTO_CREATE);

        startService(new Intent(this, ApiService.class));
        bindService(new Intent(this, ApiService.class), apiServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!hasBoundNotification ||
            !hasBoundApiService ||
            !intent.hasExtra("action") ||
            trackedRoutes.size() == 0) {
            return START_STICKY;
        }

        NotificationAction result = (NotificationAction)intent.getSerializableExtra("action");
        switch (result) {
            case PREVIOUS:
                trackedRouteIndex = trackedRouteIndex - 1 < 0 ? trackedRoutes.size() - 1 : trackedRouteIndex - 1;
                trackRoute();
                break;
            case NEXT:
                trackedRouteIndex = (trackedRouteIndex + 1) % trackedRoutes.size();
                trackRoute();
                break;
            case FLIP:
                flipRoute = !flipRoute;
                // A bit ugly but make sure GPS is turned of for this one update
                boolean tmp = gpsEnabled;
                gpsEnabled = false;
                trackRoute();
                gpsEnabled = tmp;
                break;
            case UPDATE:
                if (!isTracking) {
                    break;
                }
                trackRoute();
                break;
            default:
                break;
        }
        return START_STICKY;
    }

    public void startTracking(Context context, TrackedRoute newRoute) {
        if (!trackedRoutes.contains(newRoute)) {
            trackedRoutes.add(newRoute);
            updateLocalStorage();
        }
        trackRoute();
    }

    public void refreshTracking() {
        updateLocalStorage();
        trackRoute();
    }

    public void stopTracking(TrackedRoute route) {
        for (TrackedRoute trackedRoute : trackedRoutes) {
            if (route.equals(trackedRoute)) {
                trackedRoutes.remove(trackedRoute);
                updateLocalStorage();
                if (trackedRoutes.isEmpty()) {
                    notificationService.pause();
                }
                else {
                    trackRoute();
                }
                break;
            }
        }
    }

    public List<TrackedRoute> getTrackedRoutes() {
        return trackedRoutes;
    }

    public void pauseTracking() {
        notificationService.pause();
        isTracking = false;
    }

    public void resumeTracking() {
        if (trackedRoutes.isEmpty()) {
            return;
        }

        trackRoute();
        isTracking = true;
    }

    public boolean isTracking() {
        return isTracking;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(notificationServiceConnection);
        unbindService(apiServiceConnection);
        unregisterReceiver(screenReceiver);
    }

    private void updateLocalStorage() {
        Type trackedRouteType = new TypeToken<List<TrackedRoute>>(){}.getType();
        String trackedRoutesJson = gson.toJson(trackedRoutes, trackedRouteType);

        SharedPreferences prefs = getSharedPreferences(PREFS_REF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_TRACKEDROUTES, trackedRoutesJson);
        editor.commit();
    }

    private void trackRoute() {
        if (!hasBoundNotification ||
            !hasBoundApiService ||
            trackedRoutes.isEmpty()) {
            return;
        }

        trackedRouteIndex = (trackedRouteIndex + trackedRoutes.size()) % trackedRoutes.size();

        TrackedRoute route = trackedRoutes.get(trackedRouteIndex);
        int nrOfEnabledRoutes = 0;
        boolean foundEnabledRoute = false;
        for(int i = 0; i < trackedRoutes.size(); i++) {
            int indexOffsetted = (trackedRouteIndex + i) % trackedRoutes.size();
            if (trackedRoutes.get(indexOffsetted).isEnabled()) {
                route = trackedRoutes.get(indexOffsetted);
                foundEnabledRoute = true;
                nrOfEnabledRoutes++;
                break;
            }
        }
        if (!foundEnabledRoute) {
            notificationService.pause();
            return;
        }

        isTracking = true;

        // If gps is enabled, use gps to determine which location to watch
        long fromStopId;
        long toStopId;
        if (gpsEnabled && lastKnownLocation != null) {
            flipRoute = distanceFromDeviceTo(route.getFrom()) > distanceFromDeviceTo(route.getTo());
        }
        fromStopId = flipRoute ? route.getTo().id : route.getFrom().id;
        toStopId = flipRoute ? route.getFrom().id : route.getTo().id;

        TrackedRoute finalRoute = route;
        int finalNrOfEnabledRoutes = nrOfEnabledRoutes;
        apiService.fetchDepartures(fromStopId, toStopId,
                new ApiService.DeparturesRequest() {
                    @Override
                    public void onRequestCompleted(List<Departure> departures) {
                        finalRoute.upComingDepartures.clear();

                        for (Departure nextDeparture: departures) {
                            if (finalRoute.tracks(nextDeparture)) {
                                finalRoute.upComingDepartures.add(nextDeparture);
                            }
                        }

                        notificationService.showNotification(finalRoute, flipRoute, true, finalNrOfEnabledRoutes <= 1);
                    }

                    @Override
                    public void onRequestFailed(String error) {
                        // Remove departures that have already left if we couldn't fetch new ones.
                        LocalDateTime timeNow = LocalDateTime.now();
                        for (int i = finalRoute.upComingDepartures.size() - 1; i >= 0; i--) {
                            if (timeNow.isAfter(finalRoute.upComingDepartures.get(i).getDepartureDateTime())) {
                                finalRoute.upComingDepartures.remove(i);
                            }
                        }

                        notificationService.showNotification(finalRoute, flipRoute, false, finalNrOfEnabledRoutes <= 1);
                    }
                });
    }

    @SuppressLint("MissingPermission") // This is checked elsewhere
    public void startLocationTracking() {
        if (gpsEnabled) {
            return;
        }

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lastKnownLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_GPS_UPDATE_INTERVAL, 0, locationListener);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        gpsEnabled = true;
    }

    private double distanceFromDeviceTo(Stop stop) {
        double totalDistance = 0;
        totalDistance += Math.abs(lastKnownLocation.getLatitude() - stop.lat);
        totalDistance += Math.abs(lastKnownLocation.getLongitude() - stop.lon);
        return totalDistance;
    }
}
