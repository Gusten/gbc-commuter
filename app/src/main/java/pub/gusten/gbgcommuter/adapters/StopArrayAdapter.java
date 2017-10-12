package pub.gusten.gbgcommuter.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pub.gusten.gbgcommuter.models.Stop;

import static pub.gusten.gbgcommuter.helpers.TextUtils.splitCamelCase;

public class StopArrayAdapter extends ArrayAdapter<Stop> {
    private LayoutInflater mInflater;
    private List<Stop> stops;
    private List<Stop> filteredStops;

    public StopArrayAdapter(Context mContext, int textViewResourceId, List<Stop> stops) {
        super(mContext, textViewResourceId, stops);
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.filteredStops = stops;
        this.stops = new ArrayList<>(stops);
    }

    @Override
    public int getCount() {
        return filteredStops.size();
    }

    @Override
    public Stop getItem(int position) {
        return filteredStops.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         View rowView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        TextView nameView = rowView.findViewById(android.R.id.text1);
        String name = splitCamelCase(filteredStops.get(position).name).replace(" , ", ", ");
        nameView.setText(name);

        return rowView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    private final Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object result) {
            String name = splitCamelCase(((Stop)result).name);
            return name.replace(" , ", ", ");
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults filterResults = new FilterResults();
            ArrayList<Stop> tempList = new ArrayList<>();
            if(prefix != null && prefix.length() > 0) {
                for (Stop stop : stops) {
                    if (stop.name.toLowerCase().startsWith(prefix.toString().toLowerCase())) {
                        tempList.add(stop);
                    }
                }

                filterResults.values = tempList;
                filterResults.count = tempList.size();
            }
            else {
                filterResults.values = stops;
                filterResults.count = stops.size();
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredStops = (ArrayList<Stop>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}
