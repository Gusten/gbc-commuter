package pub.gusten.gbgcommuter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pub.gusten.gbgcommuter.models.Route;

public class RouteAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Route> routes;

    public RouteAdapter(Context mContext, LayoutInflater mInflater, ArrayList<Route> routes) {
        this.mContext = mContext;
        this.mInflater = mInflater;
        this.routes = routes;

        // https://api.vasttrafik.se/bin/rest.exe/v2/trip?originId=9021014002210000&destId=9021014007220000&maxChanges=0&format=json
    }

    @Override
    public int getCount() {
        return routes.size();
    }

    @Override
    public Route getItem(int position) {
        return routes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.route_list_item, parent, false);

        Route route = getItem(position);

        TextView lineNumber = rowView.findViewById(R.id.route_list_line);
        lineNumber.setText(route.line);

        TextView name = rowView.findViewById(R.id.route_list_name);
        lineNumber.setText("What to show here I wonder");

        return rowView;
    }
}
