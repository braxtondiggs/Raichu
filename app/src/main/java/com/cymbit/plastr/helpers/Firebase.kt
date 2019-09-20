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

    fun favorite(data: RedditFetch.RedditChildrenData, activity: Activity, view: View, text: String) {
        if (isAuth()) {
            data.user = this.auth.currentUser!!.uid
            this.db.document("favorites/" + data.user + data.id).set(data).addOnSuccessListener {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        } else {
            login(data, activity, view, text, true)
        }
    }

    fun unfavorite(data: RedditFetch.RedditChildrenData, activity: Activity, view: View, text: String) {
        if (isAuth()) {
            data.user = this.auth.currentUser!!.uid
            this.db.document("favorites/" + data.user + data.id).delete().addOnSuccessListener {
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                    Log.w("TAG", "Error adding document", e)
                }
        } else {
            login(data, activity, view, text, false)
        }
    }

    fun isAuth(): Boolean {
        return auth.currentUser != null
    }

    private fun login(data: RedditFetch.RedditChildrenData, activity: Activity, view: View, text: String, favorite: Boolean) {
        auth.signInAnonymously().addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                if (favorite) {
                    favorite(data, activity, view, text)
                } else {
                    unfavorite(data, activity, view, text)
                }
            }
        }
    }

}