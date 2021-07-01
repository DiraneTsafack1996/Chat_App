package com.example.dirane.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dirane.MessageActivity
import com.example.dirane.Model.Chat
import com.example.dirane.Model.User
import com.example.dirane.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(
    private val mContext: Context?,
    private val onItemClick: OnItemClick,
    private val mUsers: MutableList<User?>?,
    private val ischat: Boolean
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var theLastMessage: String? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false) // ceci c'est le moule xml qui va contenir la liste des users
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUsers?.get(position) // pour déterminer la position de l'utilisateur dans la liste
        holder.username.typeface = MR
        holder.last_msg.typeface = MRR
        holder.username.text = user!!.username
        if (user!!.imageURL == "default") {
            holder.profile_image.setImageResource(R.drawable.profile_img)
        } else {
            Glide.with(mContext!!).load(user!!.imageURL).into(holder.profile_image)
        }
        if (ischat) {
            lastMessage(user!!.id, holder.last_msg)
        } else {
            holder.last_msg.visibility = View.GONE
        }
        if (ischat) {
            if (user!!.status == "online") {
                holder.img_on.visibility = View.VISIBLE
                holder.img_off.visibility = View.GONE
            } else {
                holder.img_on.visibility = View.GONE
                holder.img_off.visibility = View.GONE
            }
        } else {
            holder.img_on.visibility = View.GONE
            holder.img_off.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, MessageActivity::class.java)
            intent.putExtra("userid", user!!.id)
            mContext!!.startActivity(intent)
        }
        holder.profile_image.setOnClickListener { view -> onItemClick.onItemCLick(user.id, view) }
    }

    override fun getItemCount(): Int {
        return mUsers!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // le ViewHolder c'est ici qu'on définie les identifiants des éléments se trouvant dans res/layout/user_item
        var username: TextView
        var profile_image: ImageView
        val img_on: ImageView
        val img_off: ImageView
        val last_msg: TextView

        init {
            username = itemView.findViewById(R.id.username)
            profile_image = itemView.findViewById(R.id.profile_image)
            img_on = itemView.findViewById(R.id.img_on)
            img_off = itemView.findViewById(R.id.img_off)
            last_msg = itemView.findViewById(R.id.last_msg)
        }
    }

    //check for last message
    private fun lastMessage(userid: String?, last_msg: TextView) {
        theLastMessage = "default"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (firebaseUser != null && chat != null) {
                        if (chat.receiver == firebaseUser.uid && chat.sender == userid ||
                            chat.receiver == userid && chat.sender == firebaseUser.uid
                        ) {
                            theLastMessage = chat.message
                        }
                    }
                }
                when (theLastMessage) {
                    "default" -> last_msg.text = "No Message"
                    else -> last_msg.text = theLastMessage
                }
                theLastMessage = "default"
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    init {
        if (mContext != null) {
            MRR = Typeface.createFromAsset(mContext.assets, "fonts/myriadregular.ttf")
            MR = Typeface.createFromAsset(mContext.assets, "fonts/myriad.ttf")
        }
    }
}