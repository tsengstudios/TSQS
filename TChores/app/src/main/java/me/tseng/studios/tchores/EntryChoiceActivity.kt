package me.tseng.studios.tchores

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
                Choice(
                        "Java",
                        "Run the Cloud Firestore quickstart written in Java.",
                        Intent(this, me.tseng.studios.tchores.java.MainActivity::class.java)),
                Choice(
                        "Kotlin",
                        "Run the Cloud Firestore quickstart written in Kotlin.",
                        Intent(this, me.tseng.studios.tchores.kotlin.MainActivity::class.java))
        )
    }
}
