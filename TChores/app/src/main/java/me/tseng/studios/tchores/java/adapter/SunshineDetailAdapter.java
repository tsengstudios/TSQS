package me.tseng.studios.tchores.java.adapter;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.ChoreDetailActivity;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.util.FlurrUtil;


public class SunshineDetailAdapter extends RecyclerView.Adapter<SunshineDetailAdapter.ViewHolder> {

    private int selectedPos = RecyclerView.NO_POSITION;
    private Sunshine mSunshine;
    private OnSunshineDetailSelectedListener mListener;

    public interface OnSunshineDetailSelectedListener {

        void onSunshineDetailSelected(int position);

    }

    public SunshineDetailAdapter(OnSunshineDetailSelectedListener listener) {
        super();
        mListener = listener;
    }

    public void updateSunshine(Sunshine sunshine) {
        mSunshine = sunshine;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_sunshinechores, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(position, mListener);
        holder.itemView.setSelected(selectedPos == position);
    }

    @Override
    public int getItemCount() {
        return (mSunshine != null) ? mSunshine.getChoreNames().size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sunshineChoreCheckmarkImageView)
        ImageView sunshineChoreCheckmarkImageView;

        @BindView(R.id.sunshineChoreNameView)
        TextView sunshineChoreNameView;

        @BindView(R.id.sunshineChoreTimestamp)
        TextView sunshineChoreTimestampView;

        final LocalDateTime ZEROLOCALDATETIME = LocalDateTime.of(2000,1,1,1,1);

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final int position,
                         final OnSunshineDetailSelectedListener listener) {
            Resources resources = itemView.getResources();

            sunshineChoreNameView.setText(mSunshine.getChoreNames().get(position));
            switch (mSunshine.getChoreFlState().get(position)) {
                case ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED :
                    sunshineChoreNameView.setTypeface(null, Typeface.BOLD);
                    sunshineChoreCheckmarkImageView.setVisibility(View.VISIBLE);
                    break;
                default:
                    sunshineChoreNameView.setTypeface(null, Typeface.NORMAL);
                    sunshineChoreCheckmarkImageView.setVisibility(View.INVISIBLE);
                    break;
            }

            Timestamp timestamp = mSunshine.getChoreFlTimestamp().get(position);
            LocalDateTime ldt = timestamp.toDate().toInstant().atZone(ZoneOffset.systemDefault()).toLocalDateTime();
            if (ldt.isBefore(ZEROLOCALDATETIME))
                sunshineChoreTimestampView.setText("");
            else {
                sunshineChoreTimestampView.setText(ldt.format(FlurrUtil.timestampDateTimeFormatter));
            }
            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onSunshineDetailSelected(position);
                    }
                }
            });
        }

    }
}
