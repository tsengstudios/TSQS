package me.tseng.studios.tchores.kotlin.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import me.tseng.studios.tchores.R
import me.tseng.studios.tchores.kotlin.model.chore
import me.tseng.studios.tchores.kotlin.util.choreUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.item_chore.view.choreItemCategory
import kotlinx.android.synthetic.main.item_chore.view.choreItemCity
import kotlinx.android.synthetic.main.item_chore.view.choreItemImage
import kotlinx.android.synthetic.main.item_chore.view.choreItemName
import kotlinx.android.synthetic.main.item_chore.view.choreItemNumRatings
import kotlinx.android.synthetic.main.item_chore.view.choreItemPrice
import kotlinx.android.synthetic.main.item_chore.view.choreItemRating

/**
 * RecyclerView adapter for a list of chores.
 */
open class choreAdapter(query: Query, private val listener: OnchoreSelectedListener) :
        FirestoreAdapter<choreAdapter.ViewHolder>(query) {

    interface OnchoreSelectedListener {

        fun onchoreSelected(chore: DocumentSnapshot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_chore, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            snapshot: DocumentSnapshot,
            listener: OnchoreSelectedListener?
        ) {

            val chore = snapshot.toObject(chore::class.java)
            if (chore == null) {
                return
            }

            val resources = itemView.resources

            // Load image
            Glide.with(itemView.choreItemImage.context)
                    .load(chore.photo)
                    .into(itemView.choreItemImage)

            val numRatings: Int = chore.numRatings

            itemView.choreItemName.text = chore.name
            itemView.choreItemRating.rating = chore.avgRating.toFloat()
            itemView.choreItemCity.text = chore.city
            itemView.choreItemCategory.text = chore.category
            itemView.choreItemNumRatings.text = resources.getString(
                    R.string.fmt_num_ratings,
                    numRatings)
            itemView.choreItemPrice.text = choreUtil.getPriceString(chore)

            // Click listener
            itemView.setOnClickListener {
                listener?.onchoreSelected(snapshot)
            }
        }
    }
}
