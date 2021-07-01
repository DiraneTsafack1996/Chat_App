package com.example.dirane.Notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseIdService : FirebaseMessagingService() {
    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e("NEW_TOKEN", s)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        //String refreshToken = FirebaseMessaging.getInstance().getToken();
        if (firebaseUser != null) {
            updateToken(s)
        }
    }

    private fun updateToken(refreshToken: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(refreshToken)
        reference.child(firebaseUser!!.uid).setValue(token)
    }
}