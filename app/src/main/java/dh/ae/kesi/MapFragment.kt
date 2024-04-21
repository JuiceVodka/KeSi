package dh.ae.kesi

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dh.ae.kesi.databinding.FragmentMapBinding
class MapFragment : Fragment() {

    data class LeaderboardEntry(val userName: String, val score: Int)

    val leaderboard: MutableList<LeaderboardEntry> = mutableListOf(
        LeaderboardEntry("User1", 100),
        LeaderboardEntry("User2", 90),
        LeaderboardEntry("User3", 80),
        LeaderboardEntry("User4", 70),
        LeaderboardEntry("User5", 0)
    )



    private lateinit var gMap: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var submitLocation: LatLng = LatLng(0.0, 0.0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMapBinding.inflate(inflater, container, false)
        val root = binding.root
        getPermissions()
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment?
        setGoogleMaps(supportMapFragment)
        return root
    }

    private fun setGoogleMaps(supportMapFragment: SupportMapFragment?) {
        supportMapFragment?.getMapAsync { googleMap ->
            gMap = googleMap
            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.style_json
                )
            )
            // When map is loaded
            googleMap.setOnMapClickListener { latLng ->
                submitLocation = latLng
                googleMap.clear()
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Your guess")
                googleMap.addMarker(markerOptions)
                binding.mapsButton.visibility = View.VISIBLE
                if (googleMap.cameraPosition.zoom < 12f) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                }
            }
            zoomToCurrentLocation()
            addMapsButtonListener()
            addHomeButtonListener()
        }
    }

    private fun addHomeButtonListener() {
        binding.returnHomeButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val cameraFragment = CameraFragment()
            transaction.replace(R.id.fragmentFrame, cameraFragment)
            transaction.commit()
        }
    }

    private fun addMapsButtonListener() {
        binding.mapsButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Location")
            builder.setMessage("Do you want to submit this location?")
            builder.setPositiveButton("Yes") { _, _ ->
//                removeListeners()
                showResults()
            }
            builder.setNegativeButton("No") { _, _ ->
                Snackbar.make(
                    binding.root,
                    "Location not saved",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            builder.show()
        }
    }

    private fun zoomToCurrentLocation() {
        getCurrentLocation { currLocation ->
            if (currLocation != null) {
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 10.0f))
            }
        }
    }

    private fun showResults() {
        getCurrentLocation { currLocation ->
            if (currLocation != null) {
                setFinishMarker(currLocation)
                moveCameraToFitAllMarkers(submitLocation, currLocation)
                toggleVisibility()
            }
        }
    }

    private fun removeListeners() {
        gMap.setOnMapClickListener(null)
        binding.mapsButton.setOnClickListener(null)
    }

    private fun toggleVisibility() {
        binding.mapsButton.visibility = View.GONE
        binding.submitScore.visibility = View.VISIBLE
        val score = 10
        binding.submitScore.text = getString(R.string.submitScore, score)
        binding.returnHomeButton.visibility = View.VISIBLE
        binding.leaderboard.visibility = View.VISIBLE

        // Get references to the TextViews
        val item1: TextView = requireActivity().findViewById(R.id.item1)
        val item2: TextView = requireActivity().findViewById(R.id.item2)
        val item3: TextView = requireActivity().findViewById(R.id.item3)
        val item4: TextView = requireActivity().findViewById(R.id.item4)
        val item5: TextView = requireActivity().findViewById(R.id.item5)

// Create a list of the TextViews
        val textViews = listOf(item1, item2, item3, item4, item5)

// Iterate over the leaderboard and set the text of each TextView
        for (i in leaderboard.indices) {
            textViews[i].text = getString(R.string.leaderboardItem,  leaderboard[i].userName, leaderboard[i].score.toString())
//            textViews[i].background= ContextCompat.getDrawable(requireContext(), R.drawable.border)
            textViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun moveCameraToFitAllMarkers(submitLocation: LatLng, finishLocation: LatLng) {
        // Move camera to finishLocation
        val initialZoomLevel = 15f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(finishLocation, initialZoomLevel)
        gMap.moveCamera(cameraUpdate)

        // Calculate the bounds including both locations
        val builder = LatLngBounds.Builder()
        builder.include(submitLocation)
        builder.include(finishLocation)


        val visibleRegion = gMap.projection.visibleRegion
        var submitVisible = visibleRegion.latLngBounds.contains(submitLocation)

        if (!submitVisible) {
            var zoomLevel = initialZoomLevel

            while (!submitVisible && zoomLevel > 0) {
                zoomLevel -= 1 // Decrease zoom level
                val updatedCameraUpdate = CameraUpdateFactory.newLatLngZoom(finishLocation, zoomLevel)
                gMap.moveCamera(updatedCameraUpdate)

                // Check if submitLocation becomes visible after zooming out
                val newVisibleRegion = gMap.projection.visibleRegion
                submitVisible = newVisibleRegion.latLngBounds.contains(submitLocation)
            }
        }
        // Add dotted line between submitLocation and finishLocation
        addDotedLine(submitLocation, finishLocation)
    }

    private fun addDotedLine(
        location1: LatLng,
        location2: LatLng
    ) {
        val polylineOptions = PolylineOptions()
            .add(location1)
            .add(location2)
            .width(10f)
            .color(Color.BLACK)
            .geodesic(true)
            .pattern(listOf(Dot(), Gap(10f))) // This sets the line to be dotted

        // Add the polyline to the map
        gMap.addPolyline(polylineOptions)
    }

    private fun getPermissions(){
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

    private fun calculateDistance(latlng1: LatLng, latlng2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(latlng1.latitude, latlng1.longitude, latlng2.latitude, latlng2.longitude, results)
        return results[0]
    }

    private fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            callback(null)
        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    if(location != null){
                        val currLocation = LatLng(location.latitude, location.longitude)
                        callback(currLocation)
                    } else {
                        callback(null)
                    }
                }
        }
    }

    private fun setFinishMarker(currLocation: LatLng) {
        val title = "You are here"
        val icon: BitmapDescriptor =
            getIconFromName("flag")
        gMap?.addMarker(MarkerOptions().apply {
            position(currLocation)
            title(title)
            icon(icon)
        })
    }

    private fun getIconFromName(iconName: String): BitmapDescriptor {
        val h = 150
        val w = 150

        val imageBitmap = BitmapFactory.decodeResource(
            resources,
            resources.getIdentifier(iconName, "drawable", requireActivity().packageName)
        )
        val btmp = Bitmap.createScaledBitmap(imageBitmap, h, w, false)

        return BitmapDescriptorFactory.fromBitmap(btmp)
    }
}