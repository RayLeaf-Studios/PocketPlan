package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_sleep.view.*

/**
 * A simple [Fragment] subclass.
 */
class SleepFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_sleep, container, false)
        myView.npWakeHour.minValue=0
        myView.npWakeHour.maxValue=23

        myView.npWakeMinute.minValue=0
        myView.npWakeMinute.maxValue=59

        myView.npWakeHour.setFormatter{
            i -> if(i<10) "0$i" else "$i"
        }

        myView.npWakeMinute.setFormatter{
            i -> if(i<10) "0$i" else "$i"
        }

        myView.npSleepHour.minValue=0
        myView.npSleepHour.maxValue=16

        myView.npSleepMinute.minValue=0
        myView.npSleepMinute.maxValue=59

        myView.btnApplySleepChanges.setOnClickListener{
            MainActivity.myActivity.changeToHome()
        }




        return myView
    }

}
