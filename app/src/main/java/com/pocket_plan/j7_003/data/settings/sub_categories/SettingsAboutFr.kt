package com.pocket_plan.j7_003.data.settings.sub_categories

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.App
import com.pocket_plan.j7_003.databinding.FragmentSettingsAboutBinding

class SettingsAboutFr : Fragment() {

    private var _fragmentSettingsAboutBinding: FragmentSettingsAboutBinding? = null
    private val fragmentSettingsAboutBinding get() = _fragmentSettingsAboutBinding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSettingsAboutBinding = FragmentSettingsAboutBinding.inflate(inflater, container, false)

        fragmentSettingsAboutBinding.clGithubLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/RayLeaf-Studios/PocketPlan"))
            startActivity(intent)
        }

        fragmentSettingsAboutBinding.tvStudioMail.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/mail"
            //shareIntent.flags = Intent.CATEGORY_APP_EMAIL
            activity?.startActivity(Intent.createChooser(shareIntent, "Send Mail"))
        }

        //display current version name
        val versionString = "v "+ App.instance.packageManager.getPackageInfo(App.instance.packageName, 0).versionName
        fragmentSettingsAboutBinding.tvVersionAbout.text = versionString
        return fragmentSettingsAboutBinding.root
    }

}

