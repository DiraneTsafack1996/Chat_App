package com.example.dirane.Fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dirane.Adapter.OnItemClick
import com.example.dirane.Adapter.UserAdapter
import com.example.dirane.Model.Chatlist
import com.example.dirane.Model.User
import com.example.dirane.Notifications.Token
import com.example.dirane.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*


class ChatsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    var MR: Typeface? = null
    var MRR: Typeface? = null
    private var userAdapter: UserAdapter? = null
    private var mUsers: MutableList<User>? = null
    var frameLayout: FrameLayout? = null
    var es_descp: TextView? = null
    var es_title: TextView? = null
    var fuser: FirebaseUser? = null
    var reference: DatabaseReference? = null
    private var usersList: MutableList<Chatlist?>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)
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
        fuser = FirebaseAuth.getInstance().currentUser
        usersList = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser!!.uid)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersList!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val chatlist = snapshot.getValue(Chatlist::class.java)
                    usersList!!.add(chatlist)
                }
                if (usersList!!.size == 0) {
                    frameLayout!!.setVisibility(View.VISIBLE)
                } else {
                    frameLayout!!.setVisibility(View.GONE)
                }
                chatList()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        //updateToken(FirebaseInstanceId.getInstance().getToken());
        updateToken(FirebaseMessaging.getInstance().token)
        return view
    }

    private fun updateToken(token: Task<String?>?) {
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(fuser!!.uid).setValue(token1)
    }

    private fun chatList() {
        mUsers = ArrayList()
        reference = FirebaseDatabase.getInstance().getReference("Users")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUsers!!.clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(
                        User::class.java
                    )
                    for (chatlist in usersList!!) {
                        if (user != null && user.id != null && chatlist != null && chatlist.id != null && user.id == chatlist.id) {
                            mUsers!!.add(user)
                        }
                    }
                }
                userAdapter = onItemClick?.let { UserAdapter(context, it,
                    mUsers as MutableList<User?>, true) }
                recyclerView!!.adapter = userAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    companion object {
        var onItemClick: OnItemClick? = null
        fun newInstance(click: OnItemClick?): ChatsFragment {
            onItemClick = click
            val args = Bundle()
            val fragment = ChatsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}