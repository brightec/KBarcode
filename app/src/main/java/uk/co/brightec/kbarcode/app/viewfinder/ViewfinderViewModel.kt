package uk.co.brightec.kbarcode.app.viewfinder

import android.app.Application
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import uk.co.brightec.kbarcode.Barcode
import uk.co.brightec.kbarcode.app.util.OpenForTesting

@OpenForTesting
internal class ViewfinderViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _barcode = MediatorLiveData<Resource<Barcode>>()
    val barcode: LiveData<Resource<Barcode>>
        get() = _barcode

    fun setData(data: LiveData<Barcode>) {
        _barcode.addSource(data) { barcode ->
            _barcode.value = Resource.Loading(barcode)
            Handler().postDelayed({
                _barcode.value = Resource.Success(barcode)
            }, DELAY)
        }
    }

    companion object {

        private const val DELAY = 5000L
    }
}
