package com.example.domessanger

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.TokenWatcher
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domessanger.Adapter.ChatAdapter
import com.example.domessanger.Model.Chat
import com.example.domessanger.Model.User
import com.example.domessanger.Notif.*
import com.example.domessanger.fragments.APIService
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatAdapter: ChatAdapter? = null
    var mChatlist: List<Chat>? = null
    var reference: DatabaseReference? = null
    private lateinit var recycler_chat: RecyclerView
    var notify = false
    var apiService: APIService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar: Toolbar = findViewById(R.id.toolbarChat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title =""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@ChatActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)


        val profileChat = findViewById<CircleImageView>(R.id.profileChat)
        val attch_img_file = findViewById<ImageView>(R.id.attch_img_file)
        recycler_chat = findViewById(R.id.recycler_chat)
        recycler_chat.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_chat.layoutManager = linearLayoutManager

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val user: User? = p0.getValue(User::class.java)
                val username_chat = findViewById<TextView>(R.id.username_chat)

                username_chat.text = user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profileChat)
                retrieveMessage(firebaseUser!!.uid,userIdVisit,user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })


        val btn_send_message = findViewById<ImageView>(R.id.btn_send_message)
        val tMessage = findViewById<EditText>(R.id.tMessage)
        btn_send_message.setOnClickListener() {
            notify = true

            val message = tMessage.text.toString()
            if (message == "") {
                Toast.makeText(this@ChatActivity, "Write a message...", Toast.LENGTH_SHORT).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            tMessage.setText("")
        }

        attch_img_file.setOnClickListener() {
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 438)
        }
        seenMessage(userIdVisit)
    }



    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chat").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListReference =
                        FirebaseDatabase.getInstance().reference.child("ChatList")
                            .child(firebaseUser!!.uid)
                            .child(userIdVisit)
                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {
                            if(!p0.exists()){
                                chatListReference.child("id").setValue(userIdVisit)

                            }
                            val chatListReceiverRef =
                                FirebaseDatabase.getInstance().reference.child("ChatList")
                                    .child(userIdVisit)
                                    .child(firebaseUser!!.uid)
                            chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }
        //push notif
        val userReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)
        userReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                if(notify){
                    sendNotification(receiverId,user!!.getUserName(),message)
                }
                notify = false
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun sendNotification(receiverId: String?, userName: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot in p0.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)

                    val data = Data(firebaseUser!!.uid, R.mipmap.ic_launcher,"$userName: $message","New Message",userIdVisit)
                    val sender = Sender(data!!,token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object :Callback<MyResponse>{
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {

                                if(response.code()==200){

                                    if(response.body()!!.success!==1){

                                        Toast.makeText(this@ChatActivity,"Failed",Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==438&&resultCode==RESULT_OK&&data!=null&& data!!.data!=null){

            val progressBar =  ProgressDialog(this  )
            progressBar.setMessage("Image is Uploading...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Image")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }

                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val downloadurl = task.result
                    val url = downloadurl.toString()


                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "Sending a image"
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chat").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                progressBar.dismiss()
                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)
                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(User::class.java)
                                        if(notify){
                                            sendNotification(userIdVisit,user!!.getUserName(),"Sending a image")
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(p0: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })

                            }
                        }
                }

            }

        }
    }
    private fun retrieveMessage(senderId: String, receiverId: String?, receiverImageUrl: String?) {
        mChatlist = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chat")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatlist as ArrayList<Chat>).clear()
                for(snapshot in p0.children){
                    val chat = snapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderId)&&chat.getSender().equals(receiverId)||chat.getReceiver().equals(receiverId)&&chat.getSender().equals(senderId)){
                        (mChatlist as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter( this@ChatActivity,(mChatlist as ArrayList<Chat>),receiverImageUrl!!)
                    recycler_chat.adapter = chatAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    var seenListener: ValueEventListener? = null
    private fun seenMessage(userId: String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chat")
        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)){

                        val hashMap = HashMap<String,Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
    }

}