package uk.co.brightec.kbarcode.app

import android.app.Application
import uk.co.brightec.kbarcode.KBarcode

internal class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        KBarcode.setDebugging(BuildConfig.DEBUG)
    }
}
