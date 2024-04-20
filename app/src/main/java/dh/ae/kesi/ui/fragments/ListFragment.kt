package dh.ae.kesi.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dh.ae.kesi.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dh.ae.kesi.data.repositories.LocationRepository
import dh.ae.kesi.ui.adapters.LocationAdapter
class ListFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private val locationRepository = LocationRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = view?.findViewById(R.id.achievementsRecyclerView)
        layoutManager = LinearLayoutManager(view?.context)
        recyclerView?.layoutManager = layoutManager
        locationRepository.getAllLocations { _locations, _ ->
            // Update the UI with the loaded locations
            _locations?.let {
                // Assign the loaded locations to the adapter
                Log.d("test", "v fragmentu")
                Log.d("test", _locations.toString())
                adapter = LocationAdapter(_locations)
                recyclerView?.adapter = adapter
            }
        }
        return view

        // Inflate the layout for this fragment
    }

}