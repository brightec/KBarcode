package uk.co.brightec.kbarcode.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uk.co.brightec.kbarcode.app.camerax.CameraXActivity
import uk.co.brightec.kbarcode.app.databinding.ActivityMainBinding
import uk.co.brightec.kbarcode.app.viewfinder.ViewfinderActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonXml.setOnClickListener {
            val intent = XmlActivity.getStartingIntent(this)
            startActivity(intent)
        }
        binding.buttonXmlJava.setOnClickListener {
            val intent = XmlJavaActivity.getStartingIntent(this)
            startActivity(intent)
        }
        binding.buttonProgrammatic.setOnClickListener {
            val intent = ProgrammaticActivity.getStartingIntent(this)
            startActivity(intent)
        }
        binding.buttonViewfinder.setOnClickListener {
            val intent = ViewfinderActivity.getStartingIntent(this)
            startActivity(intent)
        }
        binding.buttonCamerax.setOnClickListener {
            val intent = CameraXActivity.getStartingIntent(this)
            startActivity(intent)
        }
    }
}
