package pub.gusten.gbgcommuter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.threeten.bp.DayOfWeek;

import java.util.List;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.TimeInterval;
import pub.gusten.gbgcommuter.models.TrackedRoute;
import pub.gusten.gbgcommuter.services.TrackerService;

import static pub.gusten.gbgcommuter.helpers.TextUtils.getNameWithoutArea;
import static pub.gusten.gbgcommuter.helpers.TextUtils.splitCamelCase;

public class TrackedRouteAdapter extends BaseExpandableListAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<TrackedRoute> trackedRoutes;
    private TrackerService tracker;

    public TrackedRouteAdapter(Context context, TrackerService tracker) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.trackedRoutes = tracker.getTrackedRoutes();
        this.tracker = tracker;
    }

    @Override
    public int getGroupCount() {
        return trackedRoutes.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public TrackedRoute getGroup(int groupPosition) {
        return trackedRoutes.get(groupPosition);
    }

    @Override
    public TrackedRoute getChild(int groupPosition, int childPosition) {
        return trackedRoutes.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.route_list_item, null);
        }

        TrackedRoute trackedRoute = getGroup(groupPosition);

        TextView lineNumber = convertView.findViewById(R.id.route_list_line);

        TextView name = convertView.findViewById(R.id.route_list_from);
        String from = splitCamelCase(getNameWithoutArea(trackedRoute.getFrom().name));
        String to = splitCamelCase(getNameWithoutArea(trackedRoute.getTo().name));
        name.setText(from + " <> " + to);

        // Set delete button
        ImageButton btn = convertView.findViewById(R.id.route_list_delete);
        btn.setOnClickListener(v -> {
            tracker.stopTracking(getGroup(groupPosition));
            notifyDataSetChanged();
            parent.invalidate();
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.route_list_child_view, null);

        TrackedRoute route = getGroup(groupPosition);

        ListView timeIntervals = convertView.findViewById(R.id.route_time_interval_list);
        TimeIntervalAdapter adapter = new TimeIntervalAdapter(context, route.activeIntervals, tracker, timeIntervals);
        timeIntervals.setAdapter(adapter);

        Button newIntervalBtn = convertView.findViewById(R.id.route_time_new);
        newIntervalBtn.setOnClickListener(v -> {
            route.activeIntervals.add(new TimeInterval());
            adapter.notifyDataSetChanged();
        });

        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_monday), DayOfWeek.MONDAY);
        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_tuesday), DayOfWeek.TUESDAY);
        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_wednesday), DayOfWeek.WEDNESDAY);
        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_thursday), DayOfWeek.THURSDAY);
        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_friday), DayOfWeek.FRIDAY);
        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_saturday), DayOfWeek.SATURDAY);
        setWeekdayCheckboxListener(route, convertView.findViewById(R.id.modal_checkbox_sunday), DayOfWeek.SUNDAY);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private void setWeekdayCheckboxListener(TrackedRoute route, CheckBox checkBox, DayOfWeek weekday) {
        checkBox.setChecked(route.activeDays.contains(weekday));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                route.activeDays.add(weekday);
            }
            else {
                route.activeDays.remove(weekday);
            }
            tracker.refreshTracking();
        });
    }
}
