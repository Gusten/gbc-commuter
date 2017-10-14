package pub.gusten.gbgcommuter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.departures.Departure;

import static pub.gusten.gbgcommuter.helpers.ColorUtils.getColorFromHex;

public class DepartureAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Departure> departures;

    public DepartureAdapter(Context mContext, List<Departure> departures) {
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        this.departures = departures;
    }

    @Override
    public int getCount() {
        return departures.size();
    }

    @Override
    public Departure getItem(int position) {
        return departures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = mInflater.inflate(R.layout.modal_line_icon, parent, false);

        Departure departure = getItem(position);

        TextView lineNumber = rowView.findViewById(R.id.modal_line_number);
        lineNumber.setText(departure.getLine());
        lineNumber.setTextColor(getColorFromHex(departure.getBgColor().substring(1)));
        lineNumber.setBackgroundColor(getColorFromHex(departure.getFgColor().substring(1)));

        return rowView;
    }
}
