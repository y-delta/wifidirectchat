package com.example.myapp.utils

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageSwitcher
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.example.myapp.ui.ledger.TakeInput

class HelpScreen : AppCompatActivity() {
    private val images = intArrayOf(R.drawable.ic_accuracy_bad,R.drawable.ic_accuracy_low,
        R.drawable.ic_accuracy_high, R.drawable.ic_accuracy_medium)
    private var index = 0
    private var imgSwitcher: ImageSwitcher? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        imgSwitcher = findViewById(R.id.help_images)
        imgSwitcher?.setFactory {
            val imgView = ImageView(applicationContext)
            imgView.scaleType = ImageView.ScaleType.FIT_CENTER
            imgView.setPadding(8, 8, 8, 8)
            imgView
        }

        imgSwitcher?.setImageResource(images[index])

        val imgIn = AnimationUtils.loadAnimation(
            this, android.R.anim.fade_in)
        imgSwitcher?.inAnimation = imgIn

        val imgOut = AnimationUtils.loadAnimation(
            this, android.R.anim.fade_out)
        imgSwitcher?.outAnimation = imgOut

        val prevButton = findViewById<Button>(R.id.prev_button)
        val nextButton = findViewById<Button>(R.id.next_button)
        val exitButton = findViewById<ImageButton>(R.id.exit_button)

        prevButton.setOnClickListener{
            index = if (index - 1 >= 0) index - 1 else 2
            imgSwitcher?.setImageResource(images[index])
        }

        exitButton.setOnClickListener {
            finish()
        }

        nextButton.setOnClickListener{
            index = if (index + 1 < images.size) index +1 else 0
            imgSwitcher?.setImageResource(images[index])
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("HelpScreen", "onDestroy() - activity destroyed")
    }
}