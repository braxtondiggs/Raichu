package com.cymbit.plastr.helpers

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Firebase {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun favorite(activity: Activity) {
        if (isAuth()) {
            this.db.collection("favorites")
                .add(hashMapOf("data" to "Something")).addOnSuccessListener { documentReference ->
                    Log.d("TAG", "DocumentSnapshot added with ID: ${documentReference.id}")
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