package me.tseng.studios.tchores.kotlin

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import me.tseng.studios.tchores.R
import me.tseng.studios.tchores.kotlin.model.Rating
import kotlinx.android.synthetic.main.dialog_flurr.restaurantFormRating
import kotlinx.android.synthetic.main.dialog_flurr.restaurantFormText
import kotlinx.android.synthetic.main.dialog_flurr.view.restaurantFormButton
import kotlinx.android.synthetic.main.dialog_flurr.view.restaurantFormCancel

/**
 * Dialog Fragment containing rating form.
 */
class RatingDialogFragment : DialogFragment() {

    private var ratingListener: RatingListener? = null

    internal interface RatingListener {

        fun onRating(rating: Rating)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.dialog_flurr, container, false)

        v.restaurantFormButton.setOnClickListener { onSubmitClicked() }
        v.restaurantFormCancel.setOnClickListener { onCancelClicked() }

        return v
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is RatingListener) {
            ratingListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun onSubmitClicked() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val rating = Rating(
                    user,
                    restaurantFormRating.rating.toDouble(),
                    restaurantFormText.text.toString())

            ratingListener?.onRating(rating)
        }

        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }

    companion object {

        const val TAG = "RatingDialog"
    }
}
