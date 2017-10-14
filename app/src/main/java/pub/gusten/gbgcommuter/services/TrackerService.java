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
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pub.gusten.gbgcommuter.receivers.ScreenActionReceiver;
import pub.gusten.gbgcommuter.models.Departure;
import pub.gusten.gbgcommuter.models.NotificationAction;
import pub.gusten.gbgcommuter.models.TrackedRoute;

public class TrackerService extends Service {

    private final String PREFS_REF = "trackerService";
    private final String PREFS_TRACKEDROUTES = "trackedRoutes";

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
        isTracking = true;
        trackedRoutes = new ArrayList<>();
        trackedRouteIndex = 0;

        // Fetch tracked routes from shared prefs
        SharedPreferences prefs = getSharedPreferences(PREFS_REF, MODE_PRIVATE);
        String storedJsonRoutes = prefs.getString(PREFS_TRACKEDROUTES, "[]");
        try {
            JSONArray tmpArray = new JSONArray(storedJsonRoutes);
            for (int i = 0; i < tmpArray.length(); i++) {
                JSONObject tmpObj = tmpArray.getJSONObject(i);
                trackedRoutes.add(new TrackedRoute(
                        tmpObj.getString("from"),
                        tmpObj.getLong("fromStopId"),
                        tmpObj.getString("to"),
                        tmpObj.getLong("toStopId"),
                        tmpObj.getString("line"),
                        tmpObj.getString("bgColor"),
                        tmpObj.getString("fgColor")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenReceiver = new ScreenActionReceiver();
        registerReceiver(screenReceiver, intentFilter);

        // Are we allowed to start location tracking?
        int grantedStatus = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (grantedStatus == PackageManager.PERMISSION_GRANTED) {
            startLocationListener();
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
                trackRoute();
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

    public void startTracking(TrackedRoute newRoute) {
        if (!trackedRoutes.contains(newRoute)) {
            trackedRoutes.add(newRoute);
            updateLocalStorage();
        }
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
        JSONArray jsonArray = new JSONArray();
        try {
            for (TrackedRoute trackedRoute : trackedRoutes) {
                jsonArray.put(trackedRoute.toJsonObject());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_REF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREFS_TRACKEDROUTES, jsonArray.toString());
        editor.commit();
    }

    private void trackRoute() {
        if (!hasBoundNotification ||
            !hasBoundApiService ||
            trackedRoutes.isEmpty()) {
            return;
        }

        isTracking = true;
        trackedRouteIndex = (trackedRouteIndex + trackedRoutes.size()) % trackedRoutes.size();

        TrackedRoute route = trackedRoutes.get(trackedRouteIndex);
        final long fromStopId = flipRoute ? route.toStopId : route.fromStopId;
        final long toStopId = flipRoute ? route.fromStopId : route.toStopId;

        // Remove departures that have already left
        LocalDateTime timeNow = LocalDateTime.now();
        for (int i = route.upComingDepartures.size() - 1; i >= 0; i--) {
            if (timeNow.isAfter(route.upComingDepartures.get(i).timeInstant)) {
                route.upComingDepartures.remove(i);
            }
        }

        apiService.fetchDepartures(fromStopId, toStopId,
                new ApiService.DeparturesRequest() {
                    @Override
                    public void onRequestCompleted(List<Departure> departures) {
                        route.upComingDepartures.clear();

                        int index = 0;
                        Iterator<Departure> iterator = departures.iterator();
                        while(index < 2 && iterator.hasNext()) {
                            Departure nextDeparture = iterator.next();
                            if (route.tracks(nextDeparture)) {
                                route.upComingDepartures.add(nextDeparture);
                                index++;
                            }
                        }

                        notificationService.showNotification(route, flipRoute, true, trackedRoutes.size() <= 1);
                    }

                    @Override
                    public void onRequestFailed() {
                        notificationService.showNotification(route, flipRoute, false, trackedRoutes.size() <= 1);
                    }
                });
    }

    @SuppressLint("MissingPermission") // This is checked elsewhere
    public void startLocationListener() {
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                // makeUseOfNewLocation(location);
                Log.i("Tracker", "Lon: " + location.getLongitude() + "  Lat: " + location.getLatitude());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
}
