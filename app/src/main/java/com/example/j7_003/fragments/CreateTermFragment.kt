package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.j7_003.R
import kotlinx.android.synthetic.main.fragment_create_term.view.*
import kotlinx.android.synthetic.main.fragment_write_note.*
import org.w3c.dom.Text

/**
 * A simple [Fragment] subclass.
 */
class CreateTermFragment : Fragment() {

    lateinit var etTermTitle: EditText
    lateinit var etTermInfo: EditText
    lateinit var tvTermDate: TextView
    lateinit var tvTermTime: TextView

    lateinit var panelTermDate: ConstraintLayout
    lateinit var panelTermTime: ConstraintLayout

    lateinit var btnDuration30m: Button
    lateinit var btnDuration90m: Button
    lateinit var btnDuration120m: Button
    lateinit var btnDuration180m: Button

    lateinit var btnSaveTerm: Button
    lateinit var myView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myView = inflater.inflate(R.layout.fragment_create_term, container, false)

        //input for title and info
        etTermTitle = myView.etTermTitle
        etTermInfo = myView.etTermInfo

        //display of date and time
         tvTermDate = myView.tvTermDate
         tvTermTime = myView.tvTermTime

        //panels to tap to pick date and time
         panelTermDate = myView.panelTermDate
         panelTermTime = myView.panelTermTime

        //buttons for quick duration
         btnDuration30m = myView.btnDuration30m
         btnDuration90m = myView.btnDuration90m
         btnDuration120m = myView.btnDuration120m
         btnDuration180m = myView.btnDuration180m

        //button to save term
         btnSaveTerm = myView.btnSaveTerm

        //duration button onclick listeners
        btnDuration30m.setOnClickListener{
            addDuration(30)
        }

        btnDuration90m.setOnClickListener{
            addDuration(90)
        }

        btnDuration120m.setOnClickListener{
            addDuration(120)
        }

        btnDuration180m.setOnClickListener{
            addDuration(180)
        }

        //time and date panels
        panelTermDate.setOnClickListener{
            openDatePicker()
        }

        panelTermTime.setOnClickListener{
            openTimePicker()
        }

        btnSaveTerm.setOnClickListener(){
            saveTerm()
        }
        return myView
    }

    fun addDuration(minutes: Int){

    }

    fun saveTerm(){
        val termTitle = etTermTitle.text
        val termInfo = etTermInfo.text
    }

    fun openDatePicker(){}

    fun openTimePicker(){}

}
