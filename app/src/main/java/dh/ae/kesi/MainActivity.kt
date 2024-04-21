package dh.ae.kesi

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dh.ae.kesi.databinding.ActivityMainBinding
import dh.ae.kesi.ui.adapters.LocationAdapter
import dh.ae.kesi.ui.fragments.ListFragment

class MainActivity : AppCompatActivity(), LocationAdapter.locationClickListener, CameraFragment.CamFragmentInterface, EntryFragment.EntryFragmentListener {
    private lateinit var binding : ActivityMainBinding
    private var fragmentTransaction : FragmentTransaction? = null
    private var objId :String? = null

    /** Helper to ask camera permission.  */
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
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }

        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //request camera permission if not granted
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
        }

        //hide the action bar
        supportActionBar?.hide()

        //initialise camera fragment
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction!!.setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)

        val camFragment = CameraFragment()


        fragmentTransaction?.add(R.id.fragmentFrame, camFragment)
        fragmentTransaction?.commit()

    }
    fun switchToMapFragment() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction!!.setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)

        val mapFragment = MapFragment()
        fragmentTransaction?.replace(R.id.fragmentFrame, mapFragment)
        fragmentTransaction?.commit()
    }
    override fun switchToListFragment() {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction!!.setCustomAnimations(androidx.appcompat.R.anim.abc_slide_in_bottom, androidx.appcompat.R.anim.abc_slide_out_top)

        val listFragment = ListFragment()
        fragmentTransaction?.replace(R.id.fragmentFrame, listFragment)
        fragmentTransaction?.commit()
    }
    override fun detailClick(objectId :String?, username: String?, lat: String?, long: String?, img1: String?, img2: String?, img3: String?, img4: String?, img5: String?) {
        Log.d("test", "menjam fragment")
        fragmentTransaction = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("objectId", objectId)
        bundle.putString("lat", lat)
        bundle.putString("long", long)
        bundle.putString("img1", img1)
        bundle.putString("img2", img2)
        bundle.putString("img3", img3)
        bundle.putString("img4", img4)
        bundle.putString("img5", img5)

        val entryFragment = EntryFragment()
        entryFragment.arguments = bundle
        Log.d("MAIN", objectId.toString())
        Log.d("test", "menjam fragment")
        fragmentTransaction?.setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        fragmentTransaction?.replace(R.id.fragmentFrame, entryFragment)
        fragmentTransaction?.commit()
    }

    override fun goToMapFragment(id: String, mult: Double, lat :String, lng :String) {
        fragmentTransaction = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("objectId", id)
        bundle.putDouble("mult", mult)
        bundle.putDouble("lat", lat.toDouble())
        bundle.putDouble("lng", lng.toDouble())
        val mapFragment = MapFragment()
        //mapFragment.arguments = bundle

        fragmentTransaction?.setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        fragmentTransaction?.replace(R.id.fragmentFrame, mapFragment)
        fragmentTransaction?.commit()
    }

    /*override fun onStart() {
        super.onStart()
        fragmentTransaction = supportFragmentManager.beginTransaction()
        val mapFragment = MapFragment()
        fragmentTransaction?.setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
        fragmentTransaction?.replace(R.id.fragmentFrame, mapFragment)
        fragmentTransaction?.commit()
    }*/
}