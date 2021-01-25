package com.example.domessanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.squareup.picasso.Picasso

class ViewImageActivity : AppCompatActivity() {
    private var imageViewer: ImageView? = null
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        imageUrl = intent.getStringExtra("url").toString()
        imageViewer = findViewById(R.id.imageViewer)
        Picasso.get().load(imageUrl).into(imageViewer)
    }
}