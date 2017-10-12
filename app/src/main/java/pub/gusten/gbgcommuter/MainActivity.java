package pub.gusten.gbgcommuter;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pub.gusten.gbgcommuter.adapters.DepartureAdapter;
import pub.gusten.gbgcommuter.adapters.StopArrayAdapter;
import pub.gusten.gbgcommuter.adapters.TrackedRouteAdapter;
import pub.gusten.gbgcommuter.models.Departure;
import pub.gusten.gbgcommuter.models.Stop;
import pub.gusten.gbgcommuter.models.TrackedRoute;
import pub.gusten.gbgcommuter.services.ApiService;
import pub.gusten.gbgcommuter.services.TrackerService;

public class MainActivity extends AppCompatActivity {


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
        }
    };

    private final Context mContext = this;
    private List<Stop> availableStops;
    private Stop selectedFrom;
    private Stop selectedTo;
    private Departure selectedDeparture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set ui components
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            View modalView = layoutInflater.inflate(R.layout.modal_track_route, null);

            builder.setView(modalView)
                    .setPositiveButton(R.string.modal_save, (dialog, id) -> {
                        if (hasBoundTracker && selectedDeparture != null && selectedFrom != null && selectedTo != null) {
                            tracker.startTracking(new TrackedRoute(selectedFrom.name, Long.toString(selectedFrom.id), selectedTo.name, Long.toString(selectedTo.id), selectedDeparture.line, selectedDeparture.bgColor, selectedDeparture.fgColor));
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

            StopArrayAdapter adapter = new StopArrayAdapter(this, android.R.layout.simple_list_item_1, availableStops);

            AutoCompleteTextView fromSelector = modalView.findViewById(R.id.modal_from);
            fromSelector.setAdapter(adapter);
            fromSelector.setThreshold(1);
            fromSelector.setOnItemClickListener((parent, arg1, pos, id) -> {
                selectedFrom = adapter.getItem(pos);
                listLines(modalView);
            });

            AutoCompleteTextView toSelector = modalView.findViewById(R.id.modal_to);
            toSelector.setAdapter(adapter);
            toSelector.setThreshold(1);
            toSelector.setOnItemClickListener((parent, arg1, pos, id) -> {
                selectedTo = adapter.getItem(pos);
                listLines(modalView);
            });

            dialog.show();
        });

        // Load locations from JSON file
        loadLocations();

        // Start necessary services
        startService(new Intent(this, TrackerService.class));
        bindService(new Intent(this, TrackerService.class), trackerConnection, BIND_AUTO_CREATE);

        startService(new Intent(this, ApiService.class));
        bindService(new Intent(this, ApiService.class), apiConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void listTrackedRoutes() {
        if (!hasBoundTracker) {
            return;
        }
        ListView listView = (ListView) findViewById(R.id.main_tracked_routes);
        TrackedRouteAdapter adapter = new TrackedRouteAdapter(mContext, tracker.getTrackedRoutes());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            tracker.stopTracking(adapter.getItem(position));
            listTrackedRoutes();
        });
    }

    private void listLines(View modal) {
        if (!hasBoundApiService || selectedTo == null || selectedFrom == null) {
            return;
        }

        apiService.fetchDepartures(Long.toString(selectedFrom.id), Long.toString(selectedTo.id), new ApiService.DeparturesRequest() {
            @Override
            public void onRequestCompleted(List<Departure> departures) {
                List<Departure> filteredDepartures = new ArrayList<>();
                // Filter out copies
                for (Departure departure : departures) {
                    if(!filteredDepartures.contains(departure)) {
                        filteredDepartures.add(departure);
                    }
                }

                Spinner modalLineSpinner = modal.findViewById(R.id.modal_line_spinner);
                DepartureAdapter adapter = new DepartureAdapter(mContext, filteredDepartures);
                modalLineSpinner.setAdapter(adapter);
                modalLineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedDeparture = adapter.getItem(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedDeparture = null;
                    }
                });
            }

            @Override
            public void onRequestFailed() {

            }
        });
    }

    private void loadLocations() {
        availableStops = new ArrayList<>();

        try {
            InputStream inputStream = getAssets().open("locations.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];

            inputStream.read(buffer);
            inputStream.close();

            String json = new String(buffer, "UTF-8");
            JSONArray locations = new JSONArray(json);

            for(int i = 0; i < locations.length(); i++) {
                JSONObject tmp = locations.getJSONObject(i);
                Stop tmpStop = new Stop(
                    tmp.getString("name"),
                    tmp.getLong("id"),
                    tmp.getDouble("lat"),
                    tmp.getDouble("lon"),
                    tmp.getInt("weight")
                );
                availableStops.add(tmpStop);
            }
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
        }
    }
}
