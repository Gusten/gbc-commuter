package pub.gusten.gbgcommuter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import pub.gusten.gbgcommuter.R;
import pub.gusten.gbgcommuter.models.SelectableLine;

import static pub.gusten.gbgcommuter.helpers.ColorUtils.getColorFromHex;

public class SelectableLineAdapter extends RecyclerView.Adapter {
    private List<SelectableLine> selectableLines;
    private Context context;
    private View view;
    private RecyclerView.ViewHolder viewHolder;
    private OnDepartureOnClickListener listener;

    public SelectableLineAdapter(Context context, List<SelectableLine> selectableLines, OnDepartureOnClickListener listener) {
        this.context = context;
        this.selectableLines = selectableLines;
        this.listener = listener;
    }

    public interface OnDepartureOnClickListener {
        void onDepartureClick(SelectableLine selectableLine);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView lineTextView;
        public ImageView selectedImageView;

        public ViewHolder(View v){
            super(v);
            lineTextView = v.findViewById(R.id.modal_line_number);
            selectedImageView = v.findViewById(R.id.modal_line_selected);
        }

        public void bind(final SelectableLine selectableLine, final OnDepartureOnClickListener listener) {
            lineTextView.setText(selectableLine.line.name);
            lineTextView.setTextColor(getColorFromHex(selectableLine.line.bgColor.substring(1)));
            lineTextView.setBackgroundColor(getColorFromHex(selectableLine.line.fgColor.substring(1)));
            lineTextView.setOnClickListener(v -> {
                selectableLine.isSelected = !selectableLine.isSelected;
                if(selectableLine.isSelected) {
                    selectedImageView.setVisibility(View.VISIBLE);
                }
                else {
                    selectedImageView.setVisibility(View.INVISIBLE);
                }
                listener.onDepartureClick(selectableLine);
            });
            // Default is selected
            selectedImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.modal_line_icon, parent,false);

        viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SelectableLine selectableLine = selectableLines.get(position);
        ViewHolder viewHolder = (ViewHolder)holder;
        viewHolder.bind(selectableLine, listener);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return selectableLines.size();
    }
}
