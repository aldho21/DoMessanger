package com.example.domessanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val emailRegister = findViewById<EditText>(R.id.emailRegister)
        val passRegister = findViewById<EditText>(R.id.passRegister)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarLogin)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()


        btnLogin.setOnClickListener() {
            loginUser()
        }
    }

    private fun loginUser() {
        val emailLogin = findViewById<EditText>(R.id.emailLogin)
        val passLogin = findViewById<EditText>(R.id.passLogin)
        val email: String = emailLogin.text.toString()
        val password: String = passLogin.text.toString()
        if (email == ""){
            Toast.makeText(this@LoginActivity, "Input Email Address", Toast.LENGTH_SHORT).show()

        }else if (password == ""){
            Toast.makeText(this@LoginActivity, "Input Password", Toast.LENGTH_SHORT).show()
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this@LoginActivity, "Error message: "+task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}