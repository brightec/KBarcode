package uk.co.brightec.kbarcode.app.testutil

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Used for testing fragments inside a fake activity.
 */
internal open class SingleFragmentActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        container = FrameLayout(this)
        container.id = View.generateViewId()
        setContentView(container)
    }

    fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(container.id, fragment, "TEST")
            .commit()
    }
}
