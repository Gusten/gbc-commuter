package pub.gusten.gbgcommuter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.TrackedRoute;
import pub.gusten.gbgcommuter.services.TrackerService;

import static pub.gusten.gbgcommuter.helpers.ColorUtils.getColorFromHex;
import static pub.gusten.gbgcommuter.helpers.TextUtils.getNameWithoutArea;
import static pub.gusten.gbgcommuter.helpers.TextUtils.splitCamelCase;

public class TrackedRouteAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<TrackedRoute> trackedRoutes;
    private TrackerService tracker;

    public TrackedRouteAdapter(Context mContext, TrackerService tracker) {
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.trackedRoutes = tracker.getTrackedRoutes();
        this.tracker = tracker;
    }

    @Override
    public int getCount() {
        return trackedRoutes.size();
    }

    @Override
    public TrackedRoute getItem(int position) {
        return trackedRoutes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.route_list_item, parent, false);

        TrackedRoute trackedRoute = getItem(position);

        TextView lineNumber = rowView.findViewById(R.id.route_list_line);
        /*lineNumber.setText(trackedRoute.line);
        lineNumber.setTextColor(getColorFromHex(trackedRoute.bgColor.substring(1)));
        lineNumber.setBackgroundColor(getColorFromHex(trackedRoute.fgColor.substring(1)));*/

        TextView name = rowView.findViewById(R.id.route_list_from);
        String from = splitCamelCase(getNameWithoutArea(trackedRoute.getFrom().name));
        String to = splitCamelCase(getNameWithoutArea(trackedRoute.getTo().name));
        name.setText(from + " <> " + to);

        // Set delete button
        ImageButton btn = rowView.findViewById(R.id.route_list_delete);
        btn.setOnClickListener(v -> {
            tracker.stopTracking(getItem(position));
            notifyDataSetChanged();
        });

        return rowView;
    }
}
