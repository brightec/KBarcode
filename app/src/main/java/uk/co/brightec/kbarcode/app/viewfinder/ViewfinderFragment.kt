package uk.co.brightec.kbarcode.app.viewfinder

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_viewfinder.*
import uk.co.brightec.kbarcode.app.R

internal class ViewfinderFragment : Fragment() {

    @VisibleForTesting
    lateinit var viewModel: ViewfinderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the viewModel if it hasn't already
        // It could be set already in a test
        if (!::viewModel.isInitialized) {
            viewModel = ViewModelProvider(this).get(ViewfinderViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_viewfinder, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().setTitle(R.string.title_viewfinder)

        lifecycle.addObserver(view_barcode)

        viewModel.setData(view_barcode.barcode)
        viewModel.barcode.observe(viewLifecycleOwner, Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    progress.visibility = View.GONE
                    val barcode = resource.data
                    Toast.makeText(
                        requireContext(), barcode.displayValue, Toast.LENGTH_SHORT
                    ).show()
                    view_barcode.resume()
                }
                is Resource.Error -> {
                    progress.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        R.string.error_processing_barcode, Toast.LENGTH_SHORT
                    ).show()
                    view_barcode.resume()
                }
                is Resource.Loading -> {
                    view_barcode.pause()
                    progress.visibility = View.VISIBLE
                }
            }
        })

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA -> if (
                grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                view_barcode.start()
            }
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            )
        ) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_camera_rationale)
                .setMessage(R.string.message_camera_rationale)
                .setPositiveButton(R.string.action_ok) { _: DialogInterface, _: Int ->
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_PERMISSION_CAMERA
                    )
                }
                .show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION_CAMERA
            )
        }
    }

    companion object {

        private const val REQUEST_PERMISSION_CAMERA = 1
    }
}
