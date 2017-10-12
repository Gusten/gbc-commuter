package pub.gusten.gbgcommuter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.TrackedRoute;

import static pub.gusten.gbgcommuter.helpers.ColorUtils.getColorFromHex;
import static pub.gusten.gbgcommuter.helpers.TextUtils.getNameWithoutArea;

public class TrackedRouteAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<TrackedRoute> trackedRoutes;

    public TrackedRouteAdapter(Context mContext, List<TrackedRoute> trackedRoutes) {
        this.mContext = mContext;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.trackedRoutes = trackedRoutes;
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
        lineNumber.setText(trackedRoute.line);
        lineNumber.setTextColor(getColorFromHex(trackedRoute.bgColor.substring(1)));
        lineNumber.setBackgroundColor(getColorFromHex(trackedRoute.fgColor.substring(1)));

        TextView name = rowView.findViewById(R.id.route_list_from);
        name.setText(getNameWithoutArea(trackedRoute.from) + " <> " + getNameWithoutArea(trackedRoute.to));

        return rowView;
    }
}
