package uk.co.brightec.kbarcode.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.brightec.kbarcode.app.viewfinder.ViewfinderActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_xml.setOnClickListener {
            val intent = XmlActivity.getStartingIntent(this)
            startActivity(intent)
        }
        button_xml_java.setOnClickListener {
            val intent = XmlJavaActivity.getStartingIntent(this)
            startActivity(intent)
        }
        button_programmatic.setOnClickListener {
            val intent = ProgrammaticActivity.getStartingIntent(this)
            startActivity(intent)
        }
        button_viewfinder.setOnClickListener {
            val intent = ViewfinderActivity.getStartingIntent(this)
            startActivity(intent)
        }
    }
}
