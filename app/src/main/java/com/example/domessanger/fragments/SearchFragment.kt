package com.example.domessanger.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domessanger.Adapter.UserAdapter
import com.example.domessanger.Model.User
import com.example.domessanger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {



    private var userAdapter: UserAdapter? = null
    private var mUser: List<User>? = null
    private var searchUserApp: EditText? = null
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View =  inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView= view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText= view.findViewById(R.id.searchUserApp)
        mUser = ArrayList()
        retrieveAllUser()

        searchEditText!!.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {

                searchForUser(cs.toString().toLowerCase())

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    private fun retrieveAllUser() {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refuser = FirebaseDatabase.getInstance().reference.child("Users")
        refuser.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUser as ArrayList<User>).clear()
                if(searchEditText!!.text.toString() == ""){
                    for(snapshot in p0 .children){
                        val user: User? = snapshot.getValue(User::class.java)
                        if(!(user!!.getUID()).equals(firebaseUserID)){
                            (mUser as ArrayList<User>).add(user)
                        }
                    }
                    userAdapter = UserAdapter(context!!, mUser!!, false)
                    recyclerView!!.adapter = userAdapter
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun searchForUser(str: String){
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUser = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str).endAt(str+"\uf8ff")
        queryUser.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUser as ArrayList<User>).clear()
                for(snapshot in p0 .children){
                    val user: User? = snapshot.getValue(User::class.java)
                    if(!(user!!.getUID()).equals(firebaseUserID)){
                        (mUser as ArrayList<User>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!, mUser!!, false)
                recyclerView!!.adapter = userAdapter





            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}