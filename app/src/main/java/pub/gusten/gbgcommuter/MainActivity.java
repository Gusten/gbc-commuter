package pub.gusten.gbgcommuter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pub.gusten.gbgcommuter.adapters.DepartureAdapter;
import pub.gusten.gbgcommuter.adapters.StopArrayAdapter;
import pub.gusten.gbgcommuter.adapters.TrackedRouteAdapter;
import pub.gusten.gbgcommuter.models.departures.Departure;
import pub.gusten.gbgcommuter.models.Stop;
import pub.gusten.gbgcommuter.models.TrackedRoute;
import pub.gusten.gbgcommuter.services.ApiService;
import pub.gusten.gbgcommuter.services.TrackerService;

public class MainActivity extends AppCompatActivity {

    private final String PREFS_REF = "mainActivity";
    private final String PREFS_GPS_FIRST_PROMPT = "gpsPermission";

    private ApiService apiService;
    private boolean hasBoundApiService;
    private ServiceConnection apiConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundApiService = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundApiService = true;
            ApiService.LocalBinder mLocalBinder = (ApiService.LocalBinder)service;
            apiService = mLocalBinder.getService();
        }
    };

    private TrackerService tracker;
    private boolean hasBoundTracker;
    private ServiceConnection trackerConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            hasBoundTracker = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            hasBoundTracker = true;
            TrackerService.LocalBinder mLocalBinder = (TrackerService.LocalBinder)service;
            tracker = mLocalBinder.getService();
            listTrackedRoutes();

            // Make a check if the user has denied us access to coarse location.
            // If so, respect the user's decision and do not ask again for it.
            SharedPreferences prefs = getSharedPreferences(PREFS_REF, MODE_PRIVATE);
            boolean firstTimeAsking = prefs.getBoolean(PREFS_GPS_FIRST_PROMPT, true);

            int grantedStatus = ActivityCompat.checkSelfPermission((Activity) mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (grantedStatus != PackageManager.PERMISSION_GRANTED && firstTimeAsking) {
                int permission_id = getResources().getInteger(R.integer.coarse_gps_permission);
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, permission_id);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREFS_GPS_FIRST_PROMPT, false);
                editor.commit();
            }
            else if (grantedStatus == PackageManager.PERMISSION_GRANTED) {
                tracker.startLocationListener();
            }
        }
    };

    private final Context mContext = this;
    private List<Stop> availableStops;
    private Stop selectedFrom;
    private Stop selectedTo;
    private Departure selectedDeparture;

    // TODO: Move this to a modal/dialog class
    private Dialog trackNewModal;
    private AutoCompleteTextView fromSelector;
    private AutoCompleteTextView toSelector;
    private Spinner lineSpinner;
    private List<Departure> filteredDepartures;
    private DepartureAdapter departureAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.main_toolbar);

         filteredDepartures = new ArrayList<>();

        // Load locations from JSON file
        availableStops = loadAvailableStopsFromFile();

        // Build modal for track new routes
        trackNewModal = buildTrackNewModal();

        // Load UI components
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            trackNewModal.show();
            resetTrackNewModal();
        });

        // Start and bind necessary services
        startService(new Intent(this, TrackerService.class));
        bindService(new Intent(this, TrackerService.class), trackerConnection, BIND_AUTO_CREATE);

        startService(new Intent(this, ApiService.class));
        bindService(new Intent(this, ApiService.class), apiConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        int permission_id = getResources().getInteger(R.integer.coarse_gps_permission);
        if (requestCode == permission_id && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            tracker.startLocationListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(trackerConnection);
        unbindService(apiConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_toggle_tracking) {
            if (hasBoundTracker && tracker.isTracking()) {
                tracker.pauseTracking();
            }
            else if (hasBoundTracker && !tracker.isTracking()){
                tracker.resumeTracking();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void listTrackedRoutes() {
        if (!hasBoundTracker) {
            return;
        }

        ListView listView = (ListView) findViewById(R.id.main_tracked_routes);
        TrackedRouteAdapter adapter = new TrackedRouteAdapter(mContext, tracker);
        listView.setAdapter(adapter);
        /*listView.setOnItemClickListener((parent, view, position, id) -> {
            listTrackedRoutes();
        });*/

        if (tracker.getTrackedRoutes().isEmpty()) {
            showTutorial();
        }
        else {
            hideTutorial();
        }
    }

    private void listLines() {
        if (!hasBoundApiService || selectedTo == null || selectedFrom == null) {
            return;
        }

        filteredDepartures.clear();
        apiService.fetchDepartures(selectedFrom.id, selectedTo.id, new ApiService.DeparturesRequest() {
            @Override
            public void onRequestCompleted(List<Departure> departures) {
                // Filter out copies
                for (Departure departure : departures) {
                    if(!filteredDepartures.contains(departure)) {
                        filteredDepartures.add(departure);
                    }
                }
                departureAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRequestFailed() {
                departureAdapter.notifyDataSetChanged();
            }
        });
    }

    private List<Stop> loadAvailableStopsFromFile() {
        List<Stop> availableStops = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("locations.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];

            inputStream.read(buffer);
            inputStream.close();

            String stopsJson = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            availableStops = gson.fromJson(stopsJson, new TypeToken<List<Stop>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return availableStops;
    }

    private Dialog buildTrackNewModal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View modalView = layoutInflater.inflate(R.layout.modal_track_route, null);

        builder.setView(modalView)
                .setPositiveButton(R.string.modal_save, (dialog, id) -> {
                    if (hasBoundTracker && selectedDeparture != null && selectedFrom != null && selectedTo != null) {
                        tracker.startTracking(
                            new TrackedRoute(
                                selectedFrom,
                                selectedTo,
                                new ArrayList<>()
                            )
                        );
                        listTrackedRoutes();
                    }
                    selectedTo = null;
                    selectedFrom = null;
                    selectedDeparture = null;
                })
                .setNegativeButton(R.string.modal_cancel, (dialog, id) -> {
                    selectedTo = null;
                    selectedFrom = null;
                    selectedDeparture = null;
                    dialog.cancel();
                });

        Dialog dialog = builder.create();

        StopArrayAdapter stopAdapter = new StopArrayAdapter(this, android.R.layout.simple_list_item_1, availableStops);

        fromSelector = modalView.findViewById(R.id.modal_from);
        fromSelector.setAdapter(stopAdapter);
        fromSelector.setThreshold(1);
        fromSelector.setOnItemClickListener((parent, arg1, pos, id) -> {
            selectedFrom = stopAdapter.getItem(pos);
            listLines();
        });

        toSelector = modalView.findViewById(R.id.modal_to);
        toSelector.setAdapter(stopAdapter);
        toSelector.setThreshold(1);
        toSelector.setOnItemClickListener((parent, arg1, pos, id) -> {
            selectedTo = stopAdapter.getItem(pos);
            listLines();
        });

        departureAdapter = new DepartureAdapter(mContext, filteredDepartures);
        lineSpinner = modalView.findViewById(R.id.modal_line_spinner);
        lineSpinner.setAdapter(departureAdapter);
        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDeparture = departureAdapter.getItem(position);
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDeparture = null;
                ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        return dialog;
    }

    private void resetTrackNewModal() {
        ((AlertDialog)trackNewModal).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        fromSelector.setText("");
        toSelector.setText("");
        filteredDepartures.clear();
        departureAdapter.notifyDataSetChanged();
    }

    private void hideTutorial() {
        findViewById(R.id.main_tutorial_image).setVisibility(View.INVISIBLE);
        findViewById(R.id.main_tutorial_text).setVisibility(View.INVISIBLE);
    }

    private void showTutorial() {
        findViewById(R.id.main_tutorial_image).setVisibility(View.VISIBLE);
        findViewById(R.id.main_tutorial_text).setVisibility(View.VISIBLE);
    }
}
