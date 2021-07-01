package com.example.dirane

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    var send_email: EditText? = null
    var btn_reset: Button? = null
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var hint_tv: TextView? = null
    var firebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        MRR = Typeface.createFromAsset(assets, "fonts/myriadregular.ttf")
        MR = Typeface.createFromAsset(assets, "fonts/myriad.ttf")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Reset Password")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        send_email = findViewById(R.id.send_email)
        btn_reset = findViewById(R.id.btn_reset)
        hint_tv = findViewById(R.id.hint_tv)
        send_email!!.setTypeface(MRR)
        btn_reset!!.setTypeface(MR)
        hint_tv!!.setTypeface(MR)
        firebaseAuth = FirebaseAuth.getInstance()
        btn_reset!!.setOnClickListener(View.OnClickListener {
            val email = send_email!!.getText().toString()
            Utils.hideKeyboard(this@ResetPasswordActivity)
            if (email == "") {
                Toast.makeText(
                    this@ResetPasswordActivity,
                    "All fileds are required!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                firebaseAuth!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Please check you Email",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    } else {
                        val error = task.exception!!.message
                        Toast.makeText(this@ResetPasswordActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}