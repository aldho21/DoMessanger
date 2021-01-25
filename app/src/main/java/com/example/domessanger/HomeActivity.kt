package com.example.domessanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class HomeActivity : AppCompatActivity() {
    var firebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnWelcomeR = findViewById<Button>(R.id.btnWelcomeR)
        btnWelcomeR.setOnClickListener(){
            val intent = Intent(this@HomeActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        val btnWelcomeL = findViewById<Button>(R.id.btnWelcomeL)
        btnWelcomeL.setOnClickListener(){
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if(firebaseUser != null){

            val intent = Intent(this@HomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}