package com.pocket_plan.j7_003.data.settings.sub_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings_backup.view.*

class SettingsBackupFr : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_settings_backup, container, false)

        myView.btnConnect.setOnClickListener {
            MainActivity.act.toast("connect")

        }
        myView.btnDisconnect.setOnClickListener {
            MainActivity.act.toast("disconnect")
        }
        myView.btnSend.setOnClickListener {
            MainActivity.act.toast("send")
        }
        return myView
    }
}