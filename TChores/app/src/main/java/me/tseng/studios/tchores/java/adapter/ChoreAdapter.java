package me.tseng.studios.tchores.java.adapter;

import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Chore;
import me.tseng.studios.tchores.java.util.ChoreUtil;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of chores.
 */
public class ChoreAdapter extends FirestoreAdapter<ChoreAdapter.ViewHolder> {

    public interface OnChoreSelectedListener {

        void onchoreSelected(DocumentSnapshot chore);

    }

    private OnChoreSelectedListener mListener;

    public ChoreAdapter(Query query, OnChoreSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_chore, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.choreItemImage)
        ImageView imageView;

        @BindView(R.id.choreItemName)
        TextView nameView;

        @BindView(R.id.choreItemRating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.choreItemNumRatings)
        TextView numRatingsView;

        @BindView(R.id.choreItemPrice)
        TextView priceView;

        @BindView(R.id.choreItemCategory)
        TextView categoryView;

        @BindView(R.id.choreItemCity)
        TextView cityView;

        @BindView(R.id.choreItemADTime)
        TextView aDTimeView;

        @BindView(R.id.choreItemRecurringInterval)
        TextView recurringIntervalView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnChoreSelectedListener listener) {

            Chore chore = snapshot.toObject(Chore.class);
            Resources resources = itemView.getResources();

            // Load image
            String tempPhoto = chore.getPhoto();
            if (ChoreUtil.isURL(tempPhoto)) {
                Glide.with(imageView.getContext())
                        .load(tempPhoto)
                        .into(imageView);
            } else {
                try {
                    int tp = Integer.valueOf(tempPhoto);
                    imageView.setImageResource(tp);
                } catch (Exception e){
                    // not an int or not a resource number; use default image
                }
            }
            nameView.setText(chore.getName());
            ratingBar.setRating((float) chore.getAvgRating());
            cityView.setText(chore.getCity());
            categoryView.setText(chore.getCategory());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    chore.getNumRatings()));
            priceView.setText(ChoreUtil.getPriceString(chore));

            LocalDateTime ldt = LocalDateTime.parse(chore.getADTime());
            String textTimeView = "";
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE,  h:mm a");
            textTimeView = dtf.format(ldt);
            if (LocalDateTime.now().isBefore(ldt)) {
                aDTimeView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.chore_future_text_color));

            }else {
                aDTimeView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.chore_past_text_color));
            }
            aDTimeView.setText(textTimeView);
            recurringIntervalView.setText(chore.getRecuranceInterval());

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onchoreSelected(snapshot);
                    }
                }
            });
        }

    }
}
