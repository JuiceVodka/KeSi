package dh.ae.kesi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.icu.text.SimpleDateFormat
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.parse.ParseObject
import dh.ae.kesi.databinding.FragmentCameraBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class CameraFragment : Fragment() {

    public interface CamFragmentInterface{
        fun switchToListFragment()
    }


    private lateinit var binding : FragmentCameraBinding

    var picCount :Int = 0
    var picArray = ArrayList<Bitmap>()

    var lat :Double? = null
    var long :Double? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var mainInter:CamFragmentInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocationPrem()
        getLoc()
        mainInter = activity as CamFragmentInterface
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        binding.takePic.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.postData.setOnClickListener {
            postToDb()
        }

        binding.goToListFragment.setOnClickListener {
            mainInter.switchToListFragment()
        }

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

var photoFile :File? = null
    private fun dispatchTakePictureIntent() {
        //request camera permission if not granted
        if (!MainActivity.CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            MainActivity.CameraPermissionHelper.requestCameraPermission(requireActivity())
        }
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create the File where the photo should go
        photoFile = createImageFile()

        // Continue only if the File was successfully created
        if(photoFile != null){
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".provider", // Your package
                photoFile!!)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        }

        if (requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // Start the image capture intent to take photo
            startActivityForResult(takePictureIntent, 1)
        }

    }


    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            val currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            val imageBitmap =  BitmapFactory.decodeFile(photoFile!!.absolutePath)

            picArray.add(Bitmap.createScaledBitmap(imageBitmap, 700, 900, false))

            if(picCount < 4){
                picCount++
                dispatchTakePictureIntent()
            }else{
                binding.imageView1.setImageBitmap(picArray[0].toRoundedCorners(40F))
                binding.imageView2.setImageBitmap(picArray[1].toRoundedCorners(40F))
                binding.imageView3.setImageBitmap(picArray[2].toRoundedCorners(40F))
                binding.imageView4.setImageBitmap(picArray[3].toRoundedCorners(40F))
                binding.imageView5.setImageBitmap(picArray[4].toRoundedCorners(40F))
            }

            //show second UI
            binding.firstLayout.visibility = View.GONE
            binding.secondLayout.visibility = View.VISIBLE
        }
    }


    fun encodeImgToBase64(image: Bitmap) :String{
        val byteStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG,100,byteStream)
        val byteArr = byteStream.toByteArray()
        val imgString = Base64.encodeToString(byteArr, Base64.NO_WRAP)
        return imgString
    }

    fun Bitmap.toRoundedCorners(
        cornerRadius: Float = 25F
    ):Bitmap?{
        val bitmap = Bitmap.createBitmap(
            width, // width in pixels
            height, // height in pixels
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        // path to draw rounded corners bitmap
        val path = Path().apply {
            addRoundRect(
                RectF(0f,0f,width.toFloat(),height.toFloat()),
                cornerRadius,
                cornerRadius,
                Path.Direction.CCW
            )
        }
        canvas.clipPath(path)

        // draw the rounded corners bitmap on canvas
        canvas.drawBitmap(this,0f,0f,null)
        return bitmap
    }

    fun postToDb(){
        Log.d(TAG, "SHARING")
        val toBase = ParseObject("Locations")

        val sharedPreference =  requireActivity().getSharedPreferences("User_data", Context.MODE_PRIVATE)
        val uname = sharedPreference.getString("uname", "Anonymous")
        if (uname != null) {
            toBase.put("username", uname)
        }else{
            toBase.put("username", "Anonymous")
        }

        toBase.put("img1", encodeImgToBase64(picArray[0]))
        toBase.put("img2", encodeImgToBase64(picArray[1]))
        toBase.put("img3", encodeImgToBase64(picArray[2]))
        toBase.put("img4", encodeImgToBase64(picArray[3]))
        toBase.put("img5", encodeImgToBase64(picArray[4]))

        getLoc()

        if(lat != null && long != null){
            toBase.put("lat", lat.toString())
            toBase.put("long", long.toString())
        }else{
            Toast.makeText(activity, "Turn on location to post your KeSi", Toast.LENGTH_LONG)
                .show()
            return
        }

        toBase.saveInBackground {
            if (it != null) {
                it.localizedMessage?.let { message -> Log.e(TAG, message) }
            } else {
                Log.d(TAG, toBase.toString())
                Log.d(TAG, "Object saved.")
            }
        }
        Toast.makeText(activity, "Photos and location posted!", Toast.LENGTH_LONG)
            .show()
        picCount = 0
        picArray.clear()
        binding.firstLayout.visibility = View.VISIBLE
        binding.secondLayout.visibility = View.GONE
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

                Log.d(TAG, lat.toString() + "  " + long.toString())
            }
    }

    companion object {
        val TAG = "CameraFragment"
    }
}