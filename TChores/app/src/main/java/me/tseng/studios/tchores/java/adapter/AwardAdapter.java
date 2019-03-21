package me.tseng.studios.tchores.java.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.model.Award;


public class AwardAdapter extends FirestoreAdapter<AwardAdapter.ViewHolder> {

    protected int selectedPos = RecyclerView.NO_POSITION;

    public interface OnAwardSelectedListener {

        void onAwardSelected(DocumentSnapshot awardSnapshot);

    }

    private OnAwardSelectedListener mListener;

    public AwardAdapter(Query query, OnAwardSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        String sAwardType = getSnapshot(position).getString(Award.FIELD_AWARDTYPE);
        Award.AwardType awardType = Award.AwardType.valueOf(sAwardType);
        return awardType.ordinal(); //  .toObject(Award.class).getAwardTypeAsEnum().ordinal();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Award.AwardType awardType = Award.AwardType.values()[viewType];

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(awardType.getLayoutId(), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DocumentSnapshot snapshot = getSnapshot(position);
        holder.bind(snapshot, mListener);
        holder.itemView.setSelected(selectedPos == position);
        holder.itemView.setClipToOutline(true);

        if (selectedPos == position)
            mListener.onAwardSelected(snapshot);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.awardImage)
        ImageView imageView;

        @BindView(R.id.awardTitle)
        TextView titleView;

        @BindView(R.id.awardNumber)
        TextView numberView;

        @BindView(R.id.awardUsername)
        TextView usernameView;

        @BindView(R.id.awardDate)
        TextView awardDateTextView;

        @BindView(R.id.awardTargetNumber)
        TextView targetNumberTextView;



        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnAwardSelectedListener listener) {
            Award award = snapshot.toObject(Award.class);
            // Resources resources = itemView.getResources();

            if (award.getFlagAwarded() == false) {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                titleView.setText(award.getAwardTypeAsEnum().getTitle1());
                usernameView.setText(award.getUsername());
                int countRepeats = award.getCountRepeats();
                String sCountRepeats = (countRepeats < 2) ? "" : String.valueOf(countRepeats);
                numberView.setText(sCountRepeats);
                awardDateTextView.setText(LocalDate.parse(award.getDateLastCounted()).format(DateTimeFormatter.ofPattern("yy/MM/dd")));

                int targetNumber = award.getTarget();
                String sTargetNumber = (targetNumber < 2) ? "" : String.valueOf(targetNumber);
                targetNumberTextView.setText(sTargetNumber);

                // Click listener
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
            }
        }

    }
}
