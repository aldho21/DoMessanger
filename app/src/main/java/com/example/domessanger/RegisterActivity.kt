package com.example.domessanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var refuser: DatabaseReference
    private var firebaseuserID: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarRegister)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val usernameRegister = findViewById<EditText>(R.id.usernameRegister)
        val emailRegister = findViewById<EditText>(R.id.emailRegister)
        val passRegister = findViewById<EditText>(R.id.passRegister)
        val username: String = usernameRegister.text.toString()
        val email: String = emailRegister.text.toString()
        val password: String = passRegister.text.toString()

        if (username == ""){
            Toast.makeText(this@RegisterActivity, "Input Username", Toast.LENGTH_SHORT).show()

        }else if (email == ""){
            Toast.makeText(this@RegisterActivity, "Input Email Address", Toast.LENGTH_SHORT).show()

        }else if (password == ""){
            Toast.makeText(this@RegisterActivity, "Input Password", Toast.LENGTH_SHORT).show()

        }else{
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
                if(task.isSuccessful){
                    firebaseuserID = auth.currentUser!!.uid
                    refuser = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseuserID)

                    val userHashMap = HashMap<String,Any>()
                    userHashMap["uid"] = firebaseuserID
                    userHashMap["username"] = username
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/domessanger.appspot.com/o/profile.jpg?alt=media&token=e5186186-47b7-4038-a468-f4d9ab66b160"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/domessanger.appspot.com/o/cover.jpg?alt=media&token=645784ee-eb27-48e1-b70f-f8c0eb168970"
                    userHashMap["status"] = "Offline"
                    userHashMap["search"] = username.toLowerCase()
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["twitter"] = "https://m.twitter.com"

                    refuser.updateChildren(userHashMap).addOnCompleteListener{task ->
                        if(task.isSuccessful){
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                    }

                }else{
                        Toast.makeText(this@RegisterActivity, "Error message: "+task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}