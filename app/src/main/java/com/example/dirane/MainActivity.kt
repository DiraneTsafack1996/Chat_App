package com.example.dirane

import androidx.appcompat.app.AppCompatActivity
import com.example.dirane.Adapter.OnItemClick
import android.widget.TextView
import android.app.ProgressDialog
import android.graphics.Typeface
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import android.os.Bundle
import com.example.dirane.R
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseError
import com.example.dirane.MainActivity.ViewPagerAdapter
import com.example.dirane.Model.Chat
import com.example.dirane.Fragments.ChatsFragment
import com.example.dirane.Fragments.UsersFragment
import com.example.dirane.Fragments.ProfileFragment
import android.content.Intent
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.dirane.StartActivity
import com.example.dirane.ViewProfileActivity
import androidx.fragment.app.FragmentPagerAdapter
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.dirane.Model.User
import de.hdodenhof.circleimageview.CircleImageView
import java.util.ArrayList
import java.util.HashMap

class MainActivity : AppCompatActivity(), OnItemClick {
    var doubleBackToExitPressedOnce = false //on crée un variable pour lorsqu'on appui 2 fois sur le button retour du télephone
    var profile_image: CircleImageView? = null //on crée la variable pour l'image de profile
    var username: TextView? = null // nom d'utilisateur
    var dialog: ProgressDialog? = null // variable qui va contenir le dialogue
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var firebaseUser: FirebaseUser? = null // ceci permetra de récupérer la variable dans firebase [Authentification] pour l'utilisateur actuel
    var reference: DatabaseReference? = null // ceci permetra de lire/écrire la variable dans firebase [RealTimeDataBase] pour l'utilisateur actuel
    var onItemClick: OnItemClick? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //permet de définir la vue xml qu'on doit afficher
        onItemClick = this
        MRR = Typeface.createFromAsset(assets, "fonts/myriadregular.ttf")
        MR = Typeface.createFromAsset(assets, "fonts/myriad.ttf")
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("")
        profile_image = findViewById(R.id.profile_image) //récupère l'identifiant qui se trouve dans le dossier res/layout/profile_image
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        profile_image!!.setOnClickListener(View.OnClickListener {
            val tab = tabLayout.getTabAt(2)
            tab!!.select()
        })
        username = findViewById(R.id.username)
        username!!.setTypeface(MR)
        firebaseUser = FirebaseAuth.getInstance().currentUser //permet de récupérer l'identifiant de l'utilisateur actuel
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid) // initialise la où on doit lire/écrire dans [RealTimeDatabase]
        reference!!.addValueEventListener(object : ValueEventListener { // addValueEventListener permet  de recupérer les informations dans le répertoire ici pour ce cas dans [RealTimeDataBase/Users]en écoute en temps réél
            override fun onDataChange(dataSnapshot: DataSnapshot) { //détecte en temps réel s'il y'a changement
                val user = dataSnapshot.getValue(User::class.java) // dataSnapshot.getValue() récupère les données et sauvegarde dans la classe ici pour User qu'on pour utiliser plus tard
                username!!.setText(user!!.username) // comme ici on a déja récuperé dans User, on peut accéder à user.username, user.imageURL, user.email...
                if (user.imageURL == "default") {
                    profile_image!!.setImageResource(R.drawable.profile_img)
                } else {
                    //change this
                    Glide.with(applicationContext).load(user.imageURL).into(profile_image!!) // Glide.with(le contexte actuel).load(url de la photo).into(où on doit afficher)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        dialog = Utils.showLoader(this@MainActivity)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager) //ViewPagerAdapter c'est la classe qu'on a créé pour gérer les onglets Chats|Users|Profile
                var unread = 0
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.receiver == firebaseUser!!.uid && !chat.isIsseen) {
                        unread++
                    }
                }
                if (unread == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment.newInstance(onItemClick), "Chats") // Ajoute l'ongle  [Chats] avec un titre
                } else {
                    viewPagerAdapter.addFragment(ChatsFragment.newInstance(onItemClick), "($unread) Chats")
                }
                viewPagerAdapter.addFragment(UsersFragment.newInstance(onItemClick), "Users")
                viewPagerAdapter.addFragment(ProfileFragment(), "Profile")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
                if (dialog != null) {
                    dialog!!.dismiss()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // permet de créer le menu lorsqu'on voit les 3(...) pour [logout]
        menuInflater.inflate(R.menu.menu, menu)  // c'est le repertoire où on va chercher la vue xml du menu dans res/layout/menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> { //lorsqu'on click sur logout
                FirebaseAuth.getInstance().signOut() // la fonction qui permet de se déconnecter
                // change this code beacuse your app will crash
                startActivity(Intent(this@MainActivity, StartActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                return true
            }
        }
        return false
    }

    override fun onItemCLick(uid: String?, view: View?) {
        val viewProfileActivity = ViewProfileActivity.newInstance(uid, this)
        viewProfileActivity.show(supportFragmentManager,
                "view_profile")
    }

    internal inner class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) { // ceci c'est la classe Adapter qui va gérer lorsque tu glisse à gauche/droite des onglets
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>
        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        // Ctrl + O
        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }
    }

    private fun status(status: String) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reference!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        status("online")
    }

    override fun onPause() {
        super.onPause()
        status("offline")
    }

    override fun onBackPressed() { // ceci permet de déterminer si l'utilisateur a appuyé sur la touche (retour) de son téléphone
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click Back again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}