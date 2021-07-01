package com.example.dirane

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    var email: EditText? = null
    var password: EditText? = null
    var btn_login: Button? = null
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var dialog: ProgressDialog? = null
    var auth: FirebaseAuth? = null
    var forgot_password: TextView? = null
    var login_tv: TextView? = null
    var msg_tv: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        MRR = Typeface.createFromAsset(assets, "fonts/myriadregular.ttf")
        MR = Typeface.createFromAsset(assets, "fonts/myriad.ttf")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Login")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        auth = FirebaseAuth.getInstance() // initialise la variable Authentification qui sera utile pour la connection
        login_tv = findViewById(R.id.login_tv)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        btn_login = findViewById(R.id.btn_login)
        forgot_password = findViewById(R.id.forgot_password)
        msg_tv = findViewById(R.id.msg_tv)
        msg_tv!!.setTypeface(MRR)
        login_tv!!.setTypeface(MR)
        email!!.setTypeface(MRR)
        password!!.setTypeface(MRR)
        btn_login!!.setTypeface(MRR)
        forgot_password!!.setTypeface(MRR)
        forgot_password!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    ResetPasswordActivity::class.java
                )
            )
        })
        btn_login!!.setOnClickListener(View.OnClickListener {
            val txt_email = email!!.getText().toString() // converti le texte que le l'utilisateur a saisi en chaine de caractere [toString()]
            val txt_password = password!!.getText().toString()
            Utils.hideKeyboard(this@LoginActivity)
            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) { //vérifie si l'email ou password est vide
                Toast.makeText(this@LoginActivity, "All fields are required", Toast.LENGTH_SHORT)
                    .show()
            } else {
                dialog = Utils.showLoader(this@LoginActivity)
                auth!!.signInWithEmailAndPassword(txt_email, txt_password) // cette fonction est fournie par firebase qui permet de connecter l'utlisateur
                    .addOnCompleteListener { task -> // ceci addOnCompleteListener permet de continuer si c'est bon
                        if (task.isSuccessful) {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java) // on initialise la direction pour aller à mainActivity
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (dialog != null) {
                                dialog!!.dismiss()
                            }
                            startActivity(intent) // puis on le dirige vers le MainActivity
                            finish()
                        } else {
                            if (dialog != null) {
                                dialog!!.dismiss()
                            }
                            Toast.makeText(
                                this@LoginActivity,
                                "Authentication failed!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        })
    }
}