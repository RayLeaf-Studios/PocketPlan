package com.pocket_plan.j7_003.data.settings.sub_categories

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.App
import com.pocket_plan.j7_003.R
import kotlinx.android.synthetic.main.fragment_settings_about.view.*

class SettingsAboutFr : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myView = inflater.inflate(R.layout.fragment_settings_about, container, false)
        myView.clGithubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RayLeaf-Studios/PocketPlan"))
            startActivity(intent)
        }

        myView.tvStudioMail.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/mail"
            //shareIntent.flags = Intent.CATEGORY_APP_EMAIL
            activity?.startActivity(Intent.createChooser(shareIntent, "Send Mail"))
        }

        //display current version name
        val versionString = "v "+ App.instance.packageManager.getPackageInfo(App.instance.packageName, 0).versionName
        myView.tvVersionAbout.text = versionString
        return myView
    }

}

