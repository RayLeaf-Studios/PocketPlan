package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_calender.view.*

/**
 * A simple [Fragment] subclass.
 */
class CalenderFragment : Fragment() {

    lateinit var myView: View
    lateinit var btnAddTerm: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_calender, container, false)
        btnAddTerm = myView.btnAddTerm
        btnAddTerm.setOnClickListener{
            changeToCreateTermFragment()
        }


        return myView
    }

    fun changeToCreateTermFragment(){
        MainActivity.myActivity.changeToCreateTerm()
    }

}
