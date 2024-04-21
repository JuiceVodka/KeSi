package dh.ae.kesi.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsSpinner
import android.widget.ProgressBar
import androidx.annotation.UiContext
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import dh.ae.kesi.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dh.ae.kesi.data.repositories.LocationRepository
import dh.ae.kesi.ui.adapters.LocationAdapter
import kotlinx.coroutines.withContext

class ListFragment : Fragment() {

    public interface ListFragmentListener{
        fun goToCameraFragment()
    }

    private var recyclerView: RecyclerView? = null
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<*>? = null
    private var spinner :ProgressBar? = null
    private var backButton :AppCompatButton? = null
    private val locationRepository = LocationRepository()
    private lateinit var mainActivity :ListFragmentListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as ListFragmentListener
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
        spinner = view?.findViewById(R.id.progress_loader)
        layoutManager = LinearLayoutManager(view?.context)
        recyclerView?.layoutManager = layoutManager
        backButton = view.findViewById(R.id.backButton)
        setListener()
        locationRepository.getAllLocations { _locations, _ ->
            // Update the UI with the loaded locations
            _locations?.let {
                // Assign the loaded locations to the adapter
                Log.d("test", "v fragmentu")
                Log.d("test", _locations.toString())

                val sharedPreference =  activity?.getSharedPreferences("User_data", Context.MODE_PRIVATE)
                for (i in _locations){
                    val posterId = i.objectId
                    val posterValInPrefs = sharedPreference?.getBoolean(posterId, false)
                    if (posterValInPrefs == true){
                        _locations.removeAt(_locations.indexOf(i))
                    }
                }
                

                adjustVisibility(recyclerView, spinner)
                adapter = LocationAdapter(_locations)
                recyclerView?.adapter = adapter
            }
        }
        return view

        // Inflate the layout for this fragment
    }

    fun adjustVisibility(recyclerView: RecyclerView?, spinner: ProgressBar?){
        recyclerView?.visibility = View.VISIBLE
        spinner?.visibility = View.GONE

    }

    fun setListener(){
        backButton?.setOnClickListener {
            mainActivity.goToCameraFragment()
        }
    }

}