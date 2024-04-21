package dh.ae.kesi.data.repositories

import android.util.Log
import com.parse.ParseObject
import com.parse.ParseQuery

class LocationRepository {
    fun getAllLocations(callback: (MutableList<ParseObject>?, Exception?) -> Unit) {
        Log.d("test", "querying")
        val query = ParseQuery.getQuery<ParseObject>("Locations")
        query.findInBackground { locations, e ->
            Log.d("test", locations.toString())
            callback(locations, e)
        }
        /*query.getFirstInBackground { player, e ->
            if (e == null) {
                val playerName: String? = player.getString("username")
                if (playerName != null) {
                    Log.d("test", playerName)
                } else {
                    Log.d("test", "Player name is null")
                }
            } else {
                Log.d("test", "e is null")
                // Something is wrong
            }
        }*/
    }
}
