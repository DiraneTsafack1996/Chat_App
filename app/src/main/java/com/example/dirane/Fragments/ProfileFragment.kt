package com.example.dirane.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dirane.Model.User
import com.example.dirane.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ProfileFragment() : Fragment() {
    var image_profile: CircleImageView? = null
    var profile_tv: TextView? = null
    var username: EditText? = null
    var bio_et: EditText? = null
    var edit_img: ImageView? = null
    var save: Button? = null
    var reference: DatabaseReference? = null
    var fuser: FirebaseUser? = null
    var MR: Typeface? = null
    var MRR: Typeface? = null
    var storageReference: StorageReference? = null
    private var imageUri: Uri? = null
    private var uploadTask: StorageTask<*>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        MRR = Typeface.createFromAsset(requireContext().assets, "fonts/myriadregular.ttf")
        MR = Typeface.createFromAsset(requireContext().assets, "fonts/myriad.ttf")
        image_profile = view.findViewById(R.id.profile_image)
        username = view.findViewById(R.id.username)
        profile_tv = view.findViewById(R.id.profile_tv)
        bio_et = view.findViewById(R.id.bio_et)
        edit_img = view.findViewById(R.id.edit_image)
        save = view.findViewById(R.id.save_btn)
        username!!.setTypeface(MR)
        profile_tv!!.setTypeface(MR)
        bio_et!!.setTypeface(MRR)
        save!!.setTypeface(MR)
        storageReference = FirebaseStorage.getInstance().getReference("uploads")
        fuser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
        edit_img!!.setOnClickListener(View.OnClickListener {
            save!!.setVisibility(View.VISIBLE)
            username!!.setEnabled(true)
            bio_et!!.setEnabled(true)
            username!!.setSelection(username!!.getText().length)
        })
        save!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                username!!.setEnabled(false)
                bio_et!!.setEnabled(false)
                reference!!.child("bio").setValue(bio_et!!.getText().toString().trim { it <= ' ' })
                    .addOnCompleteListener(
                        OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                //   Toast.makeText(getContext(),"Profile Updated...", Toast.LENGTH_SHORT);
                            } else {
                                //   Toast.makeText(getContext(),"Unable to Save...", Toast.LENGTH_SHORT);
                            }
                        })
                reference!!.child("username")
                    .setValue(username!!.getText().toString().trim { it <= ' ' })
                    .addOnCompleteListener(object : OnCompleteListener<Void?> {
                        override fun onComplete(task: Task<Void?>) {
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Profile Updated...", Toast.LENGTH_SHORT)
                            } else {
                                Toast.makeText(context, "Unable to Save...", Toast.LENGTH_SHORT)
                            }
                        }
                    })
                save!!.setVisibility(View.GONE)
            }
        })
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (isAdded) {
                    val user = dataSnapshot.getValue(
                        User::class.java
                    )
                    username!!.setText(user!!.username)
                    bio_et!!.setText(user.bio)
                    if ((user.imageURL == "default")) {
                        image_profile!!.setImageResource(R.drawable.profile_img)
                    } else {
                        Glide.with((context)!!).load(user.imageURL).into(image_profile!!)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        image_profile!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                openImage()
            }
        })
        return view
    }

    private fun openImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = requireContext().contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun uploadImage() {
        val pd = ProgressDialog(context)
        pd.setIndeterminateDrawable(resources.getDrawable(R.drawable.ic_picture))
        pd.setMessage("Uploading...")
        pd.show()
        if (imageUri != null) {
            val fileReference = storageReference!!.child(
                (System.currentTimeMillis()
                    .toString() + "." + getFileExtension(imageUri!!))
            )
            uploadTask = fileReference.putFile(imageUri!!)
            (uploadTask as UploadTask).continueWithTask(object : Continuation<UploadTask.TaskSnapshot?, Task<Uri>> {
                @Throws(Exception::class)
                override fun then(task: Task<UploadTask.TaskSnapshot?>): Task<Uri> {
                    if (!task.isSuccessful) {
                        throw (task.exception)!!
                    }
                    return fileReference.downloadUrl
                }
            }).addOnCompleteListener(object : OnCompleteListener<Uri?> {
                override fun onComplete(task: Task<Uri?>) {
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val mUri = downloadUri.toString()
                        reference =
                            FirebaseDatabase.getInstance().getReference("Users").child(fuser!!.uid)
                        val map = HashMap<String, Any>()
                        map["imageURL"] = "" + mUri
                        reference!!.updateChildren(map)
                        pd.dismiss()
                    } else {
                        Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
                        pd.dismiss()
                    }
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    pd.dismiss()
                }
            })
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == IMAGE_REQUEST) && (resultCode == Activity.RESULT_OK
                    ) && (data != null) && (data.data != null)
        ) {
            imageUri = data.data
            if (uploadTask != null && uploadTask!!.isInProgress) {
                Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show()
            } else {
                uploadImage()
            }
        }
    }

    companion object {
        private val IMAGE_REQUEST = 1
    }
}