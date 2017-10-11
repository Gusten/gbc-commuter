package pub.gusten.gbgcommuter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class RouteAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Recipe> mDataSource;

    public RouteAdapter(Context mContext, LayoutInflater mInflater, ArrayList<Recipe> mDataSource) {
        this.mContext = mContext;
        this.mInflater = mInflater;
        this.mDataSource = mDataSource;

        // https://api.vasttrafik.se/bin/rest.exe/v2/trip?originId=9021014002210000&destId=9021014007220000&maxChanges=0&format=json
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Nullable
    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }
}
