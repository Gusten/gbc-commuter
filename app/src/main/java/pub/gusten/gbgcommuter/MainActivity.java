package pub.gusten.gbgcommuter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import pub.gusten.gbgcommuter.models.Route;
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
        }
    };

    private boolean routeSelected;
    private Route selectedRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set ui components
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (routeSelected && hasBoundTracker) {
                tracker.startTracking(selectedRoute);
            }
        });

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
}
