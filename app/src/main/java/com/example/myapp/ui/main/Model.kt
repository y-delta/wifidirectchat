package com.example.myapp.ui.main

import com.example.myapp.R

class Model(val locationName:String, val landmarkName:String, val latLongAcc: ArrayList<String> = arrayListOf("13.082417", "77.556861", "0"), var requiredItems:ArrayList<String> = arrayListOf(), var img:Int = R.drawable.contact){

    var accuracy : Float = latLongAcc[2].toFloat()
    var latitude : Float = latLongAcc[0].toFloat()
    var longitude : Float = latLongAcc[1].toFloat()

    init{
        img = when {
            accuracy <= 20F -> {
                R.drawable.ic_accuracy_high
            }
            accuracy < 50F -> {
                R.drawable.ic_accuracy_medium
            }
            accuracy < 100F -> {
                R.drawable.ic_accuracy_low
            }
            else -> {
                R.drawable.ic_accuracy_bad
            }
        }
    }

}