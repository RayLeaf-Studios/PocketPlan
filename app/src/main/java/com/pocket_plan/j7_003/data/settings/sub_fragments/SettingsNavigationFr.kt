import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 */
class SettingsNavigationFr : Fragment() {
    lateinit var spDrawerSide: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_navigation_drawer, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {

        //initialize references to view
        spDrawerSide = myView.spDrawerSide
    }

    private fun initializeAdapters() {
        //Spinner for drawer side
        val spAdapterDrawerSide = ArrayAdapter<String>(
            MainActivity.act,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.drawerSides)
        )
        spAdapterDrawerSide.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDrawerSide.adapter = spAdapterDrawerSide
    }

    private fun initializeDisplayValues() {
        spDrawerSide.setSelection(
            when (SettingsManager.getSetting(SettingId.DRAWER_SIDE)) {
                //false = left side
                false -> 0
                //true = right side
                else -> 1
            }
        )
    }

    private fun initializeListeners() {
        //Listener for drawerSide spinner
        spDrawerSide.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val setTo = when (spDrawerSide.selectedItemPosition) {
                    1 -> Pair(true, Gravity.END)
                    else -> Pair(false, Gravity.START)
                }
                SettingsManager.addSetting(SettingId.DRAWER_SIDE, setTo.first)

                MainActivity.drawerGravity = setTo.second
                MainActivity.params.gravity = setTo.second
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }
}
