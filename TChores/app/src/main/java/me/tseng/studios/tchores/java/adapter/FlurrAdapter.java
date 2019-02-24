package me.tseng.studios.tchores.java.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Flurr;

import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tseng.studios.tchores.java.util.FlurrUtil;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of {@link Flurr}.
 */
public class FlurrAdapter extends FirestoreAdapter<FlurrAdapter.ViewHolder> {

    public FlurrAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flurr, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position).toObject(Flurr.class));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private static final SimpleDateFormat FORMAT  = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);

        @BindView(R.id.ratingItemName)
        TextView nameView;

        @BindView(R.id.ratingItemRating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.ratingItemText)
        TextView textView;

        @BindView(R.id.ratingItemDate)
        TextView dateView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Flurr flurr) {
            nameView.setText(flurr.getUserName());
            ratingBar.setRating((float) flurr.getFlurr());
            textView.setText(flurr.getText());

            if (flurr.getTimestamp() != null) {
                LocalDateTime ldt = flurr.getTimestamp().toDate().toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
                String stringLdt = ldt.format(FlurrUtil.timestampDateTimeFormatter);
                dateView.setText(stringLdt);
            }
        }
    }

}
