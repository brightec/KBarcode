package uk.co.brightec.kbarcode.app.viewfinder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import uk.co.brightec.kbarcode.app.R

internal class ViewfinderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewfinder)
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.black))

        var fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (fragment == null) {
            fragment = ViewfinderFragment()
            setFragment(fragment)
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.frame_container, fragment, "TEST")
            .commit()
    }

    companion object {

        fun getStartingIntent(context: Context) = Intent(context, ViewfinderActivity::class.java)
    }
}
