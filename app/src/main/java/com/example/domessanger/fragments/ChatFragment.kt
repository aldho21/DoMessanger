package com.example.domessanger.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domessanger.Adapter.UserAdapter
import com.example.domessanger.Model.Chatlist
import com.example.domessanger.Model.User
import com.example.domessanger.Notif.Token
import com.example.domessanger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId


class ChatFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<User>? = null
    private var userChatList: List<Chatlist>? = null
    lateinit var recycler_view_chat: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recycler_view_chat = view.findViewById(R.id.recycler_view_chat)
        recycler_view_chat.setHasFixedSize(true)
        recycler_view_chat.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (userChatList as ArrayList).clear()
                for(dataSnapshot in p0.children){
                    val chatlist = dataSnapshot.getValue(Chatlist::class.java)
                    (userChatList as ArrayList).add(chatlist!!)
                }
                retriveChatList()
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        updateToken(FirebaseInstanceId.getInstance().token)
        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)

    }

    private fun retriveChatList(){
        mUsers = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList).clear()
                for(dataSnapshot in p0.children){
                    val user = dataSnapshot.getValue(User::class.java)
                    for(eachChatList in userChatList!!){
                        if(user!!.getUID().equals(eachChatList.getId())){
                            (mUsers as ArrayList).add(user!!)

                        }
                    }
                }
                userAdapter = UserAdapter(context!!, (mUsers as ArrayList<User>), true)
                recycler_view_chat.adapter = userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }
}