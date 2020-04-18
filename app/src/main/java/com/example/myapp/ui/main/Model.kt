package com.example.myapp.ui.main

import com.example.myapp.R

class Model(val locationName:String, val landmarkName:String, val latLongAcc: ArrayList<String> = arrayListOf("13.082417", "77.556861", "0"), var img:Int = R.drawable.helpwe){

    var accuracy : Float = latLongAcc[2].toFloat()
    var latitude : Float = latLongAcc[0].toFloat()
    var longitude : Float = latLongAcc[1].toFloat()

    init{
        if(accuracy <= 20F){
            img = R.drawable.ic_accuracy_high
        }
        else if(accuracy < 50F){
            img = R.drawable.ic_accuracy_medium
        }
        else if(accuracy < 100F){
            img = R.drawable.ic_accuracy_low
        }
        else{
            img = R.drawable.ic_accuracy_bad
        }
    }

}