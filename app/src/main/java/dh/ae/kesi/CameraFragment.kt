package dh.ae.kesi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dh.ae.kesi.databinding.FragmentCameraBinding


class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding

    var picCount :Int = 0
    var picArray = ArrayList<Bitmap>()

    var lat :Double? = null
    var long :Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocationPrem()
        getLoc()


    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        binding.takePic.setOnClickListener {
            takePhoto()
        }
        //Log.d("CAMERA", picCount.toString())

        return binding.root
    }


    object CameraPermissionHelper {
        private const val CAMERA_PERMISSION_CODE = 0
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

        /** Check to see we have the necessary permissions for this app.  */
        fun hasCameraPermission(activity: Activity): Boolean {
            return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
        }

        /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
        fun requestCameraPermission(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(CAMERA_PERMISSION), CAMERA_PERMISSION_CODE)
        }

        /** Check to see if we need to show the rationale for this permission.  */
        fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA_PERMISSION)
        }

        /** Launch Application Setting to grant permission.  */
        fun launchPermissionSettings(activity: Activity) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!MainActivity.CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            Toast.makeText(activity, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!MainActivity.CameraPermissionHelper.shouldShowRequestPermissionRationale(requireActivity())) {
                // Permission denied with checking "Do not ask again".
                MainActivity.CameraPermissionHelper.launchPermissionSettings(requireActivity())
            }
            activity?.finish()
        }

        activity?.recreate()
    }


    fun takePhoto(){
        if (!MainActivity.CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            MainActivity.CameraPermissionHelper.requestCameraPermission(requireActivity())
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
//                    imageUri = getImageUri(context, imageFile)
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(takePictureIntent, 1)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {

            val image = data?.extras?.get("data") as Bitmap
            picArray.add(image)
            Log.d("CAMERA", image.toString())
            Log.d("CAMERA", picCount.toString())

            if(picCount < 5){
                picCount++
                takePhoto()
            }else{
                binding.firstLayout.visibility = View.GONE
                binding.secondLayout.visibility = View.VISIBLE}

            //binding.firstLayout.visibility = View.GONE
            //binding.secondLayout.visibility = View.VISIBLE

            /*image.apply {
                view?.findViewById<ImageView>(R.id.memoImage)?.setImageBitmap(this)
                // create rounded corners bitmap
                view?.findViewById<ImageView>(R.id.memoImage)?.setImageBitmap(toRoundedCorners(8F))
            }*/



        }
    }

    fun getLocationPrem(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission
                    .ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale (requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("Debug", "snekbar")
                Snackbar.make(
                    binding.root,
                    R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.ok) {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        8
                    )
                }.show()
            } else{
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    8
                )
            }
            return
        }
    }

    @SuppressLint("MissingPermission")
    fun getLoc(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                lat = location?.latitude
                long = location?.longitude


                //TODO do some check for if location os off
            }
    }



    companion object {

    }
}