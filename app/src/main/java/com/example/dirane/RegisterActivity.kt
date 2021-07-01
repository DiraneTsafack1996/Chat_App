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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class RegisterActivity : AppCompatActivity() {
    var username: EditText? = null
    var email: EditText? = null
    var password: EditText? = null
    var register_tv: TextView? = null
    var msg_reg_tv: TextView? = null
    var btn_register: Button? = null
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var auth: FirebaseAuth? = null // initialise la variable Authentification qui sera utile pour créér un utilisateur
    var reference: DatabaseReference? = null // initialise la variable pour la base de donnée [RealTimeDatabase]
    var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        MRR = Typeface.createFromAsset(assets, "fonts/myriadregular.ttf")
        MR = Typeface.createFromAsset(assets, "fonts/myriad.ttf")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Register")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        btn_register = findViewById(R.id.btn_register)
        register_tv = findViewById(R.id.register_tv)
        msg_reg_tv = findViewById(R.id.msg_reg_tv)
        msg_reg_tv!!.setTypeface(MRR)
        username!!.setTypeface(MRR)
        email!!.setTypeface(MRR)
        password!!.setTypeface(MRR)
        btn_register!!.setTypeface(MR)
        register_tv!!.setTypeface(MR)
        auth = FirebaseAuth.getInstance()
        btn_register!!.setOnClickListener(View.OnClickListener {
            val txt_username = username!!.getText().toString()
            val txt_email = email!!.getText().toString()
            val txt_password = password!!.getText().toString()
            Utils.hideKeyboard(this@RegisterActivity)
            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(
                    txt_password
                )
            ) {
                Toast.makeText(this@RegisterActivity, "All fileds are required", Toast.LENGTH_SHORT)
                    .show()
            } else if (txt_password.length < 6) {
                Toast.makeText(
                    this@RegisterActivity,
                    "password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                register(txt_username, txt_email, txt_password) // voir la fonction register() plus bas
            }
        })
    }

    private fun register(username: String, email: String, password: String) {
        dialog = Utils.showLoader(this@RegisterActivity)
        auth!!.createUserWithEmailAndPassword(email, password) // cette fonction est fournie par firebase qui permet de créer un utlisateur
            .addOnCompleteListener { task -> //ensuite
                if (task.isSuccessful) {
                    val firebaseUser = auth!!.currentUser!!
                    val userid = firebaseUser.uid // récupère l'unique identifiant de l'utilisateur
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid) // créer un répertoire dans [RealTimeDatabase/Users] pour sauvegarder
                    val hashMap = HashMap<String, String>()
                    hashMap["id"] = userid // il sauvergarte les données de l'utilisateur actuel dans une classe HasMap (similaire à une classe User)
                    hashMap["username"] = username // c'est similaire à val user = New User() , puis  user.username = username
                    hashMap["imageURL"] = "default"
                    hashMap["status"] = "offline"
                    hashMap["bio"] = ""
                    hashMap["search"] = username.toLowerCase()
                    if (dialog != null) {
                        dialog!!.dismiss()
                    }
                    reference!!.setValue(hashMap).addOnCompleteListener { task -> // setValue(valeur) cest çà qui sauvegarde/écrit dans firebase
                        if (task.isSuccessful) { // si c'est bon
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent) // on l'envoit à MainActivity
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "You can't register woth this email or password",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (dialog != null) {
                        dialog!!.dismiss()
                    }
                }
            }
    }
}