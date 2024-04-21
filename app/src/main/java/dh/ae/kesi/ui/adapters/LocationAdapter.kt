package dh.ae.kesi.ui.adapters
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import dh.ae.kesi.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.parse.ParseObject
import android.util.Log
import android.widget.Toast
import dh.ae.kesi.EntryFragment

class LocationAdapter(private val shrooms: List<ParseObject>?) : RecyclerView.Adapter<LocationAdapter.CardViewHolder?>() {
    interface locationClickListener{
        fun detailClick(objectId :String?, username: String?, lat: String?, long: String?, img1: String?, img2: String?, img3: String?, img4: String?, img5: String?)
    }

    var clickListener :locationClickListener? = null
    inner class CardViewHolder (itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var itemTitle: TextView? = null

        init {
            Log.d("mjauu", "helo")
            itemTitle = itemView?.findViewById(R.id.item_title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        // have a CardViewHolder created when needed
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shrooms_layout, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return shrooms?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: CardViewHolder, @SuppressLint("RecyclerView") i: Int) {
        Log.d("test", shrooms?.get(i)?.getString("username").toString() + "blblblblb")
        viewHolder.itemTitle?.text = shrooms?.get(i)?.getString("username")

        viewHolder.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?){
                try{
                    clickListener = v!!.context as locationClickListener
                }catch(e: ClassCastException){
                    throw ClassCastException(v!!.context.toString() + " doesnt implement listListener")
                }
                clickListener?.detailClick(shrooms?.get(i)?.objectId, shrooms?.get(i)?.getString("username"), shrooms?.get(i)?.getString("lat"),shrooms?.get(i)?.getString("long"),shrooms?.get(i)?.getString("img1"),shrooms?.get(i)?.getString("img2"),shrooms?.get(i)?.getString("img3"),shrooms?.get(i)?.getString("img4"),shrooms?.get(i)?.getString("img5"))
            }
        })
    }
}
