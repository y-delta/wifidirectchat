package com.example.myapp.utils

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.myapp.R
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType

class HelpScreen : AppIntro()
{
    private val colorPrimary: Int = Color.rgb(98,0,238)
    private val colorSecondary: Int = Color.rgb(55,0,179)
    private val background : Int = Color.rgb(250,250,250)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance(
            title = "Welcome to HELPAPP",
            description = "Grant the app permission to view your device's location to help locate nearby peers",
            imageDrawable = R.drawable.permish,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Set your Username",
            description = "Tap on the 3 dots and set your username from the menu",
            imageDrawable = R.drawable.user,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Viewing Network Details",
            description = "See your set Username, number of peers and much more",
            imageDrawable = R.drawable.info,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Set up a Bridge Connection",
            description = "Tap on the option to enable Legacy WiFi and connect to other devices ",
            imageDrawable = R.drawable.bridge,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Create or Join a WiFi Direct Group",
            description = "Tap on the network icon to initiate connection",
            imageDrawable = R.drawable.connection,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Direct Chat",
            description = "Find all the people your device is connected to\nTap on their name to begin chatting,\n" +
                    "Swipe down to refresh!",
            imageDrawable = R.drawable.chatlist,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "One-on-One Personal Chat",
            description = "Type your message and send it with the press of a finger\n" +
                    "Get confirmation that your message reached its destination",
            imageDrawable = R.drawable.chatscreen,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Group Chat",
            description = "Communicate at once with all connected to you\nShare Real-Time updates with other users",
            imageDrawable = R.drawable.group,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Ledger",
            description = "Find people around you who need help. Pull down to Refresh! Tap on the entry to view " +
                    "their location and assist them\n" +
                    "Or tap on the button in the corner to update your situation.\nRemember, Safety is the utmost priority! ",
            imageDrawable = R.drawable.ledger,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Share your Location and Needs",
            description = "Select any of the items you are in need of\nAdd a landmark to better assist people " +
                    "trying to help",
            imageDrawable = R.drawable.input,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        addSlide(AppIntroFragment.newInstance(
            title = "Getting Started...",
            description = "Start by turning on WiFi and Location Services to locate peers near you",
            imageDrawable = R.drawable.onoff,
            backgroundColor = background,
            titleColor = colorSecondary,
            descriptionColor = colorPrimary
        ))
        askForPermissions(
            permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            slideNumber = 1,
            required = true
        )

        setIndicatorColor(colorSecondary,colorPrimary)
        setNextArrowColor(colorPrimary)
        setColorDoneText(colorPrimary)
        setColorSkipButton(colorPrimary)
        setTransformer(AppIntroPageTransformerType.Fade)
        isSystemBackButtonLocked = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }
}