package com.example.domessanger.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.domessanger.Model.User
import com.example.domessanger.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class SettingFragment : Fragment() {
    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        firebaseUser=FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")
        userReference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val user: User? = p0.getValue(User::class.java)
                    if(context!=null){
                        view.findViewById<TextView>(R.id.usernameSetting).text = user!!.getUserName()
                        Picasso.get().load(user.getProfile()).into(view.findViewById<ImageView>(R.id.settingImage))
                        Picasso.get().load(user.getProfile()).into(view.findViewById<ImageView>(R.id.cover_image_setting))
                    }

                    Picasso.get().load(user!!.getProfile())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        view.findViewById<CircleImageView>(R.id.settingImage).setOnClickListener(){
            pickImage()
        }
        view.findViewById<ImageView>(R.id.cover_image_setting).setOnClickListener(){
            coverChecker = "cover"
            pickImage()
        }
        view.findViewById<ImageView>(R.id.facebook_img).setOnClickListener(){
            socialChecker = "facebook"
            setSocialLink()
        }
        view.findViewById<ImageView>(R.id.instagram_img).setOnClickListener(){
            socialChecker = "instagram"
            setSocialLink()
        }
        view.findViewById<ImageView>(R.id.twitter_img).setOnClickListener(){
            socialChecker = "twitter"
            setSocialLink()
        }


        return view
    }
    private fun setSocialLink(){
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        if(socialChecker == "twitter"){
            builder.setTitle("Write URL:")
        }else{
            builder.setTitle("Write Username:")
        }
        val editText = EditText(context)
        if(socialChecker == "twitter"){
            editText.hint = "e.g www.google.com"
        }else{
            editText.hint = "e.g contoh"
        }
        builder.setView(editText)
        builder.setPositiveButton("Create",DialogInterface.OnClickListener(){
            dialog, which ->
            val str = editText.text.toString()
            if(str==""){
                Toast.makeText(context,"Write something...",Toast.LENGTH_SHORT).show()
            }else{
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener(){
                dialog, which ->
            dialog.cancel()
        })
        builder.show()

    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String,Any>()
        //mapCoverImg["cover"] = url
        //userReference!!.updateChildren(mapCoverImg)
        when(socialChecker){
            "facebook" ->
            {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" ->
            {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "twitter" ->
            {
                mapSocial["twitter"] = "https://m.twitter.com/$str"
            }
        }
        userReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                Toast.makeText(context,"Update Success",Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data!= null){
            imageUri = data.data
            Toast.makeText(context,"Uploading...",Toast.LENGTH_SHORT).show()
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase() {
        val progressBar =  ProgressDialog(context)
        progressBar.setMessage("Image is Uploading...")
        progressBar.show()
        if(imageUri!=null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot,Task<Uri>>{ task ->
                if(task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }

                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val downloadurl = task.result
                    val url = downloadurl.toString()
                    if(coverChecker == "cover"){
                        val mapCoverImg = HashMap<String,Any>()
                        mapCoverImg["cover"] = url
                        userReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""

                    }else{
                        val mapProfileImg = HashMap<String,Any>()
                        mapProfileImg["profile"] = url
                        userReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""

                    }
                    progressBar.dismiss()
                }
            }

        }
    }
}