package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_modules.view.*
import kotlinx.android.synthetic.main.fragment_sleep.view.*

/**
 * A simple [Fragment] subclass.
 */
class ModulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView =  inflater.inflate(R.layout.fragment_modules, container, false)

        myView.menuPanelBirthdays.setOnClickListener{
            MainActivity.myActivity.changeToBirthdays()
        }

        myView.menuPanelSettings.setOnClickListener{
            MainActivity.myActivity.changeToSettings()
        }

        myView.menuPanelSleepReminder.setOnClickListener(){
            val animationShow =
                AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.spin)
            animationShow.duration = 300
            animationShow.fillAfter = true
            myView.sleepIcon.startAnimation(animationShow)

            MainActivity.myActivity.changeToSleepReminder()

        }

        myView.menuPanelShopping.setOnClickListener(){
            MainActivity.myActivity.changeToShopping()
        }
       return myView
    }

}
