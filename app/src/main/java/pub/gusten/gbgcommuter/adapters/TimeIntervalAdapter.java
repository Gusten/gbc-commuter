package pub.gusten.gbgcommuter.adapters;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.threeten.bp.LocalTime;

import java.util.List;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.TimeInterval;
import pub.gusten.gbgcommuter.services.TrackerService;

import static pub.gusten.gbgcommuter.helpers.DateUtils.timeOnlyFormatter;

public class TimeIntervalAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<TimeInterval> timeIntervals;
    private TrackerService tracker;
    private ListView parent;

    public TimeIntervalAdapter(Context context, List<TimeInterval> timeIntervals, TrackerService tracker, ListView parent) {
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.timeIntervals = timeIntervals;
        this.tracker = tracker;
        this.parent = parent;

        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return timeIntervals.size();
    }

    @Override
    public TimeInterval getItem(int position) {
        return timeIntervals.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.route_time_child_item, null);
        }

        TimeInterval interval = getItem(position);

        TextView from = convertView.findViewById(R.id.route_time_from);
        from.setText(timeOnlyFormatter.format(interval.start));
        from.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                interval.start = LocalTime.of(hourOfDay, minute);
                tracker.refreshTracking();
                notifyDataSetChanged();
            }, interval.start.getHour(), interval.start.getMinute(), true);
            timePickerDialog.show();
        });

        TextView to = convertView.findViewById(R.id.route_time_to);
        to.setText(timeOnlyFormatter.format(interval.end));
        to.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                interval.end = LocalTime.of(hourOfDay, minute);
                tracker.refreshTracking();
                notifyDataSetChanged();
            }, interval.end.getHour(), interval.end.getMinute(), true);
            timePickerDialog.show();
        });

        ImageButton deleteBtn = convertView.findViewById(R.id.route_time_delete);
        deleteBtn.setOnClickListener(v -> {
            timeIntervals.remove(interval);
            tracker.refreshTracking();
            notifyDataSetChanged();
        });

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        int totalHeight = 0;
        for (int i = 0; i < this.getCount(); i++) {
            View listItem = this.getView(i, null, parent);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = parent.getLayoutParams();
        params.height = totalHeight + (parent.getDividerHeight() * (this.getCount() - 1));
        parent.setLayoutParams(params);
        parent.requestLayout();
    }
}
