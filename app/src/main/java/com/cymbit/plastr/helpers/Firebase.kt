package com.cymbit.plastr.helpers

import android.app.Activity
import android.util.Log
import android.view.View
import com.cymbit.plastr.service.RedditFetch
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Firebase {
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun favorite(
        data: RedditFetch.RedditChildrenData,
        activity: Activity,
        view: View,
        text: String
    ) {
        if (isAuth()) {
            this.db.document("favorites/" + this.auth.currentUser?.uid + data.id).set(data)
                .addOnSuccessListener {
                    Snackbar.make(
                        view,
                        text,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        } else {
            login(activity)
        }
    }

    fun unfavorite(listing_id: String, activity: Activity, view: View, text: String) {
        if (isAuth()) {
            this.db.document("favorites/" + this.auth.currentUser?.uid + listing_id).delete()
                .addOnSuccessListener {
                    Snackbar.make(
                        view,
                        text,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        } else {
            login(activity)
        }
    }

    private fun isAuth(): Boolean {
        return auth.currentUser != null
    }

    private fun login(activity: Activity) {
        auth.signInAnonymously().addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
            } else {

            }
        }
    }

}