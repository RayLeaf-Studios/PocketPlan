package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings, container, false)

        //todo initialize correct display of saved settings, (switch for enabled checkboxes)

        myView.swEnableCheckBoxesTodo.setOnClickListener{
            if (myView.swEnableCheckBoxesTodo.isChecked){
                //todo enable and save setting of checkboxes are visible
            }else{
                //todo disable use of checkboxes, uncheck all tasks
            }
        }

        return myView
    }

}
