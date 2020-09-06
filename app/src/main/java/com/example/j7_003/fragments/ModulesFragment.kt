package com.example.j7_003.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_modules.view.*

/**
 * A simple [Fragment] subclass.
 */
class ModulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(R.layout.fragment_modules, container, false)

        myView.menuPanelBirthdays.setOnClickListener {
            MainActivity.act.changeToBirthdays()
        }

        myView.menuPanelSettings.setOnClickListener {
            MainActivity.act.changeToSettings()
        }

        myView.menuPanelSleepReminder.setOnClickListener {
            val animationShow =
                AnimationUtils.loadAnimation(MainActivity.act, R.anim.spin)
            animationShow.duration = 300
            animationShow.fillAfter = true
            myView.sleepIcon.startAnimation(animationShow)

            MainActivity.act.changeToSleepReminder()

        }

        myView.menuPanelAbout.setOnClickListener {
            MainActivity.act.changeToAbout()
        }
        return myView
    }
}
