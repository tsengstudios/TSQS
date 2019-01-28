package me.tseng.studios.tchores.kotlin.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.tseng.studios.tchores.R
import me.tseng.studios.tchores.kotlin.model.Rating
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_flurr.view.ratingItemDate
import kotlinx.android.synthetic.main.item_flurr.view.ratingItemName
import kotlinx.android.synthetic.main.item_flurr.view.ratingItemRating
import kotlinx.android.synthetic.main.item_flurr.view.ratingItemText
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * RecyclerView adapter for a list of [Rating].
 */
open class RatingAdapter(query: Query) : FirestoreAdapter<RatingAdapter.ViewHolder>(query) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_flurr, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position).toObject(Rating::class.java))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(rating: Rating?) {
            if (rating == null) {
                return
            }

            itemView.ratingItemName.text = rating.userName
            itemView.ratingItemRating.rating = rating.rating.toFloat()
            itemView.ratingItemText.text = rating.text

            if (rating.timestamp != null) {
                itemView.ratingItemDate.text = FORMAT.format(rating.timestamp)
            }
        }

        companion object {

            private val FORMAT = SimpleDateFormat(
                    "MM/dd/yyyy", Locale.US)
        }
    }
}
