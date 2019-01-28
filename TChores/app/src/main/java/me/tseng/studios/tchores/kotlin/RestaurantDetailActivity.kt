package me.tseng.studios.tchores.kotlin

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import me.tseng.studios.tchores.R
import me.tseng.studios.tchores.kotlin.adapter.RatingAdapter
import me.tseng.studios.tchores.kotlin.model.Rating
import me.tseng.studios.tchores.kotlin.model.chore
import me.tseng.studios.tchores.kotlin.util.choreUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_chore_detail.fabShowRatingDialog
import kotlinx.android.synthetic.main.activity_chore_detail.recyclerRatings
import kotlinx.android.synthetic.main.activity_chore_detail.choreButtonBack
import kotlinx.android.synthetic.main.activity_chore_detail.choreCategory
import kotlinx.android.synthetic.main.activity_chore_detail.choreCity
import kotlinx.android.synthetic.main.activity_chore_detail.choreImage
import kotlinx.android.synthetic.main.activity_chore_detail.choreName
import kotlinx.android.synthetic.main.activity_chore_detail.choreNumRatings
import kotlinx.android.synthetic.main.activity_chore_detail.chorePrice
import kotlinx.android.synthetic.main.activity_chore_detail.choreRating
import kotlinx.android.synthetic.main.activity_chore_detail.viewEmptyRatings

class choreDetailActivity : AppCompatActivity(),
        EventListener<DocumentSnapshot>,
        RatingDialogFragment.RatingListener {

    private var ratingDialog: RatingDialogFragment? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var choreRef: DocumentReference
    private lateinit var ratingAdapter: RatingAdapter

    private var choreRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chore_detail)

        // Get chore ID from extras
        val choreId = intent.extras?.getString(KEY_chore_ID)
                ?: throw IllegalArgumentException("Must pass extra $KEY_chore_ID")

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get reference to the chore
        choreRef = firestore.collection("chores").document(choreId)

        // Get ratings
        val ratingsQuery = choreRef
                .collection("flurrs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)

        // RecyclerView
        ratingAdapter = object : RatingAdapter(ratingsQuery) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    recyclerRatings.visibility = View.GONE
                    viewEmptyRatings.visibility = View.VISIBLE
                } else {
                    recyclerRatings.visibility = View.VISIBLE
                    viewEmptyRatings.visibility = View.GONE
                }
            }
        }
        recyclerRatings.layoutManager = LinearLayoutManager(this)
        recyclerRatings.adapter = ratingAdapter

        ratingDialog = RatingDialogFragment()

        choreButtonBack.setOnClickListener { onBackArrowClicked() }
        fabShowRatingDialog.setOnClickListener { onAddRatingClicked() }
    }

    public override fun onStart() {
        super.onStart()

        ratingAdapter.startListening()
        choreRegistration = choreRef.addSnapshotListener(this)
    }

    public override fun onStop() {
        super.onStop()

        ratingAdapter.stopListening()

        choreRegistration?.remove()
        choreRegistration = null
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    /**
     * Listener for the Chore document ([.choreRef]).
     */
    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "chore:onEvent", e)
            return
        }

        snapshot?.let {
            val chore = snapshot.toObject(chore::class.java)
            if (chore != null) {
                onchoreLoaded(chore)
            }
        }
    }

    private fun onchoreLoaded(chore: chore) {
        choreName.text = chore.name
        choreRating.rating = chore.avgRating.toFloat()
        choreNumRatings.text = getString(R.string.fmt_num_ratings, chore.numRatings)
        choreCity.text = chore.city
        choreCategory.text = chore.category
        chorePrice.text = choreUtil.getPriceString(chore)

        // Background image
        Glide.with(choreImage.context)
                .load(chore.photo)
                .into(choreImage)
    }

    private fun onBackArrowClicked() {
        onBackPressed()
    }

    private fun onAddRatingClicked() {
        ratingDialog?.show(supportFragmentManager, RatingDialogFragment.TAG)
    }

    override fun onRating(rating: Rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(choreRef, rating)
                .addOnSuccessListener(this) {
                    Log.d(TAG, "Flurr added")

                    // Hide keyboard and scroll to top
                    hideKeyboard()
                    recyclerRatings.smoothScrollToPosition(0)
                }
                .addOnFailureListener(this) { e ->
                    Log.w(TAG, "Add rating failed", e)

                    // Show failure message and hide keyboard
                    hideKeyboard()
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add flurr",
                            Snackbar.LENGTH_SHORT).show()
                }
    }

    private fun addRating(choreRef: DocumentReference, rating: Rating): Task<Void> {
        // Create reference for new rating, for use inside the transaction
        val ratingRef = choreRef.collection("flurrs").document()

        // In a transaction, add the new rating and update the aggregate totals
        return firestore.runTransaction { transaction ->
            val chore = transaction.get(choreRef).toObject(chore::class.java)
            if (chore == null) {
                throw Exception("Resraurant not found at ${choreRef.path}")
            }

            // Compute new number of ratings
            val newNumRatings = chore.numRatings + 1

            // Compute new average rating
            val oldRatingTotal = chore.avgRating * chore.numRatings
            val newAvgRating = (oldRatingTotal + rating.rating) / newNumRatings

            // Set new chore info
            chore.numRatings = newNumRatings
            chore.avgRating = newAvgRating

            // Commit to Firestore
            transaction.set(choreRef, chore)
            transaction.set(ratingRef, rating)

            null
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if (view != null) {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {

        private const val TAG = "choreDetail"

        const val KEY_chore_ID = "key_chore_id"
    }
}
