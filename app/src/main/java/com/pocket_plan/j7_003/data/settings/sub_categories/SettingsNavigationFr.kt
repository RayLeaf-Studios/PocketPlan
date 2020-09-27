import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.R

/**
 * A simple [Fragment] subclass.
 */
class SettingsNavigationFr : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_navigation, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {
    }

    private fun initializeAdapters() {
    }

    private fun initializeDisplayValues() {
    }

    private fun initializeListeners() {
    }
}
