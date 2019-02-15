package me.tseng.studios.tchores.java.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Sunshine;
import me.tseng.studios.tchores.java.util.ChoreUtil;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;


public class SunshineAdapter extends FirestoreAdapter<SunshineAdapter.ViewHolder> {

    private int selectedPos = RecyclerView.NO_POSITION;

    public interface OnSunshineSelectedListener {

        void onSunshineSelected(Sunshine sunshine);

    }

    private OnSunshineSelectedListener mListener;

    public SunshineAdapter(Query query, OnSunshineSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_sunshine, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
        holder.itemView.setSelected(selectedPos == position);
        holder.itemView.setClipToOutline(true);

    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sunshineItemImage)
        ImageView imageView;

        @BindView(R.id.sunshineDayTextView)
        TextView dayView;

        @BindView(R.id.sunshineBPreCalced)
        TextView bPreCalcedView;

        @BindView(R.id.sunshineAwardPerfectDay)
        TextView awardPerfectDayView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnSunshineSelectedListener listener) {

            final Sunshine sunshine = snapshot.toObject(Sunshine.class);
            final Context context = itemView.getContext();

            final LocalDate ld = LocalDate.parse(sunshine.getDay());
            dayView.setText(String.valueOf(ld.getDayOfMonth()));
            bPreCalcedView.setText(sunshine.getBPreCalced() ? "PreCalced" : "not calc");
            if (sunshine.getAwardPerfectDay() ) {
                imageView.setColorFilter(context.getColor(R.color.colorAccent));
                awardPerfectDayView.setText(context.getString(R.string.sunshine_display_perfect_day_short));
            } else {
                imageView.setColorFilter(context.getColor(R.color.design_default_color_primary_dark));
                awardPerfectDayView.setText(context.getString(R.string.sunshine_display_imperfect_day_short));
            }

            boolean isAfterToday = LocalDate.now().isBefore(ld);
            if (isAfterToday) {
                imageView.setColorFilter(context.getColor(R.color.greyDisabledColorFilter));
                dayView.setTextColor(context.getColor(R.color.greyDisabled));
                bPreCalcedView.setTextColor(context.getColor(R.color.greyDisabled));
                awardPerfectDayView.setTextColor(context.getColor(R.color.greyDisabled));
            } else {
                dayView.setTextColor(context.getColor(R.color.text_dark_title));
                bPreCalcedView.setTextColor(context.getColor(R.color.text_dark_title));
                awardPerfectDayView.setTextColor(context.getColor(R.color.text_dark_title));
            }

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    notifyItemChanged(selectedPos);
                    selectedPos = getLayoutPosition();
                    notifyItemChanged(selectedPos);

                    if (listener != null) {
                        listener.onSunshineSelected(sunshine);
                    }
                }
            });
        }

    }
}