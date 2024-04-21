package dh.ae.kesi.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.parse.ParseObject
import com.parse.ParseQuery
import dh.ae.kesi.databinding.FragmentMapBinding
import dh.ae.kesi.R
import kotlin.math.pow

class MapFragment : Fragment() {



    data class LeaderboardEntry(val userName: String, val score: Int?, val lon: Double?, val lat: Double?)
    private var challengeId: String = "test-id"
    private var scoreMultiplier: Double = 0.8
    private var answerLatLng = LatLng(0.0, 0.0)
    private var submitLatLng: LatLng = LatLng(0.0, 0.0)
    private lateinit var leaderboardCurr: List<LeaderboardEntry>



    private lateinit var gMap: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        challengeId = arguments?.getString("objectId") ?: "test-id"
        scoreMultiplier = arguments?.getDouble("mult") ?: 1.0

        answerLatLng = LatLng(arguments?.getDouble("lat") ?: 0.0, arguments?.getDouble("lng") ?: 0.0)
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
                submitLatLng = latLng
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
                submitResults()
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

    private fun submitResults() {
        val sharedPreference =  requireActivity().getSharedPreferences("User_data", Context.MODE_PRIVATE)
        val distance = calculateDistance(submitLatLng, answerLatLng)
        val score = calculateScore(distance, scoreMultiplier)
        val uname = sharedPreference.getString("username", "Anonymous") ?: "Anonymous"
        postToDb(score, uname)
        removeListeners()
        val editor = sharedPreference.edit()
        editor.putBoolean(challengeId, true)
        editor.apply()
        setFinishMarker(answerLatLng)
        moveCameraToFitAllMarkers(submitLatLng, answerLatLng)
    }

    private fun removeListeners() {
        gMap.setOnMapClickListener(null)
        binding.mapsButton.setOnClickListener(null)
    }

    private fun toggleVisibility(score: Int) {
        binding.mapsButton.visibility = View.GONE
        binding.submitScore.visibility = View.VISIBLE


        binding.submitScore.text = getString(R.string.submitScore, score)
        binding.returnHomeButton.visibility = View.VISIBLE
        binding.leaderboard.visibility = View.VISIBLE
        binding.closeButton.visibility = View.VISIBLE
        binding.closeButton.setOnClickListener {
            binding.leaderboard.visibility = View.GONE
            binding.closeButton.visibility = View.GONE
        }


        getAllLeaderboards { _leaderboards, _ ->
            // Update the UI with the loaded locations
            _leaderboards?.let {
                // Assign the loaded locations to the adapter
                Log.d("test", "v fragmentu")
                Log.d("test", _leaderboards.toString())
                leaderboardCurr = convertParseObjectsToLeaderboardEntries(_leaderboards)
                fillLeaderboard(leaderboardCurr)

            }
        }
        // Get references to the TextViews





// Create a list of the TextViews

// Iterate over the leaderboard and set the text of each TextView

    }

    private fun fillLeaderboard(leaderboardCurr: List<LeaderboardEntry>) {
        val item1: TextView = requireActivity().findViewById(R.id.item1)
        val item2: TextView = requireActivity().findViewById(R.id.item2)
        val item3: TextView = requireActivity().findViewById(R.id.item3)
        val item4: TextView = requireActivity().findViewById(R.id.item4)
        val item5: TextView = requireActivity().findViewById(R.id.item5)

        val textViews = listOf(item1, item2, item3, item4, item5)
        val sortedLeaderboardCurr = leaderboardCurr.sortedByDescending { it.score }
        var textSize = 32.0f
        for (i in 0..sortedLeaderboardCurr.size) {
            if (i >= 5) {
                break
            }
            if (i >= leaderboardCurr.size) {
                textViews[i].visibility = View.GONE
                continue
            }
            if ( leaderboardCurr[i].userName == null) {
                textViews[i].visibility = View.GONE
            }
            else {
            textViews[i].text = getString(R.string.leaderboardItem,  leaderboardCurr[i].userName, leaderboardCurr[i].score.toString())
            textViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            textViews[i].textSize = textSize

            }

            textSize -= 2
        }


    }

    fun getAllLeaderboards(callback: (List<ParseObject>?, Exception?) -> Unit) {
        Log.d("test", "querying")
        val query = ParseQuery.getQuery<ParseObject>("Leaderboard")
        query.findInBackground { leaderboards, e ->
            Log.d("test", leaderboards.toString())
            callback(leaderboards, e)
        }
    }

    fun convertParseObjectsToLeaderboardEntries(parseObjects: List<ParseObject>): List<LeaderboardEntry> {
        return parseObjects.filter { it.getString("locationId") == challengeId }.map { parseObject ->
            LeaderboardEntry(
                userName = parseObject.getString("username") ?: "",
                score = parseObject.getString("score")?.toInt(),
                lon = parseObject.getString("long")?.toDouble(),
                lat = parseObject.getString("lat")?.toDouble()
            )
        }
    }

    private fun moveCameraToFitAllMarkers(submitLocation: LatLng, finishLocation: LatLng) {
        // Move camera to finishLocation
        val initialZoomLevel = 15f
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(finishLocation, initialZoomLevel)
        gMap.moveCamera(cameraUpdate)


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

    private fun postToDb(score: Int, uname: String = "Anonymous") {
        val toBase = ParseObject("Leaderboard")
        toBase.put("username", uname)
        toBase.put("locationId", challengeId)
        toBase.put("score", score.toString())



        toBase.put("lat", submitLatLng.latitude.toString())
        toBase.put("long", submitLatLng.longitude.toString())
        toBase.saveInBackground {
            if (it != null) {
                it.localizedMessage?.let { message -> Log.e(CameraFragment.TAG, message) }
            } else {
                Log.d(CameraFragment.TAG, toBase.toString())
                Log.d(CameraFragment.TAG, "Object saved.")
            }
            toggleVisibility(score)
        }
        Toast.makeText(activity, "Location submitted!", Toast.LENGTH_LONG)
            .show()
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
        return if (results[0] < 1000) {
            results[0]
        } else {
            results[0] / 1000
        }
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
    private fun calculateScore(distance: Float, scoreMultiplier: Double): Int {
        return 10000 / (distance).toInt()
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