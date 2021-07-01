package com.example.dirane.Fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dirane.Adapter.OnItemClick
import com.example.dirane.Adapter.UserAdapter
import com.example.dirane.Model.User
import com.example.dirane.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class UsersFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var frameLayout: FrameLayout? = null
    var es_descp: TextView? = null
    var es_title: TextView? = null
    private var userAdapter: UserAdapter? = null
    private var mUsers: MutableList<User?>? = null
    var search_users: EditText? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)
        MRR = Typeface.createFromAsset(requireContext().assets, "fonts/myriadregular.ttf")
        MR = Typeface.createFromAsset(requireContext().assets, "fonts/myriad.ttf")
        recyclerView = view.findViewById(R.id.recycler_view)
        frameLayout = view.findViewById(R.id.es_layout)
        es_descp = view.findViewById(R.id.es_descp)
        es_title = view.findViewById(R.id.es_title)
        es_descp!!.setTypeface(MR)
        es_title!!.setTypeface(MRR)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.setLayoutManager(LinearLayoutManager(context))
        val dividerItemDecoration =
            DividerItemDecoration(recyclerView!!.getContext(), DividerItemDecoration.VERTICAL)
        recyclerView!!.addItemDecoration(dividerItemDecoration)
        mUsers = ArrayList()
        readUsers()
        search_users = view.findViewById(R.id.search_users)
        search_users!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                searchUsers(charSequence.toString().toLowerCase())
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        return view
    }

    private fun searchUsers(s: String) {
        val fuser = FirebaseAuth.getInstance().currentUser
        val query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
            .startAt(s)
            .endAt(s + "\uf8ff")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(
                        User::class.java
                    )!!
                    assert(fuser != null)
                    if (user.id != fuser!!.uid) {
                        mUsers!!.add(user)
                    }
                }
                userAdapter = onItemClick?.let { UserAdapter(context, it, mUsers, false) }
                recyclerView!!.adapter = userAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun readUsers() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (search_users!!.text.toString() == "") {
                    mUsers!!.clear()
                    for (snapshot in dataSnapshot.children) {
                        val user = snapshot.getValue(
                            User::class.java
                        )
                        if (user != null && user.id != null && firebaseUser != null && user.id != firebaseUser.uid) {
                            mUsers!!.add(user)
                        }
                    }
                    if (mUsers!!.size == 0) {
                        frameLayout!!.visibility = View.VISIBLE
                    } else {
                        frameLayout!!.visibility = View.GONE
                    }
                    userAdapter = onItemClick?.let { UserAdapter(context, it, mUsers, false) }
                    recyclerView!!.adapter = userAdapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    companion object {
        var onItemClick: OnItemClick? = null
        fun newInstance(click: OnItemClick?): UsersFragment {
            onItemClick = click
            val args = Bundle()
            val fragment = UsersFragment()
            fragment.arguments = args
            return fragment
        }
    }
}