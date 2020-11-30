package com.pocket_plan.j7_003.data.settings.sub_categories

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings_about.view.*
import kotlinx.android.synthetic.main.header_navigation_drawer.view.*

class SettingsAboutFr : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_settings_about, container, false)
        myView.clGithubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/estep248/Pocket-Plan"))
            startActivity(intent)
        }

        //display current version name
        val versionString = "v "+MainActivity.act.packageManager.getPackageInfo(MainActivity.act.packageName, 0).versionName
        myView.tvVersionAbout.text = versionString
        return myView
    }

}

