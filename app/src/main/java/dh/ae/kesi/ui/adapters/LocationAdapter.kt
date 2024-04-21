package dh.ae.kesi.ui.adapters
import android.annotation.SuppressLint
import dh.ae.kesi.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.parse.ParseObject
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.Base64
import android.widget.ImageView
import android.graphics.Paint
import android.graphics.Shader


class LocationAdapter(private val locs: MutableList<ParseObject>?) : RecyclerView.Adapter<LocationAdapter.CardViewHolder?>() {
    interface locationClickListener{
        fun detailClick(objectId :String?, username: String?, lat: String?, long: String?, img1: String?, img2: String?, img3: String?)
    }

    var clickListener :locationClickListener? = null
    inner class CardViewHolder (itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var itemTitle: TextView? = null
        var itemImage: ImageView? = null

        init {
            Log.d("mjauu", "helo")
            itemTitle = itemView?.findViewById(R.id.item_title)
            itemImage = itemView?.findViewById(R.id.item_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        // have a CardViewHolder created when needed
        val view = LayoutInflater.from(parent.context).inflate(R.layout.locs_layout, parent, false)
        return CardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return locs?.size ?: 0
    }

    override fun onBindViewHolder(viewHolder: CardViewHolder, @SuppressLint("RecyclerView") i: Int) {
        Log.d("test", locs?.get(i)?.getString("username").toString())
        viewHolder.itemTitle?.text = locs?.get(i)?.getString("username") + " posted a KeSi. Take a guess!"
        // Assuming you have the Base64 encoded string stored in a variable called base64Image

        // Decode the Base64 string into a byte array
        val decodedBytes = Base64.decode(locs?.get(i)?.getString("img1"), Base64.DEFAULT)

        // Convert the byte array into a Bitmap
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        val resizedBitmap = resizeBitmap(bitmap, 200)
        val circularBitmap = getCircularBitmap(resizedBitmap, 200)

        // Set the Bitmap to the ImageView
        viewHolder.itemImage?.setImageBitmap(circularBitmap)


        viewHolder.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View?){
                try{
                    clickListener = v!!.context as locationClickListener
                }catch(e: ClassCastException){
                    throw ClassCastException(v!!.context.toString() + " doesnt implement listListener")
                }
                clickListener?.detailClick(locs?.get(i)?.objectId, locs?.get(i)?.getString("username"), locs?.get(i)?.getString("lat"),locs?.get(i)?.getString("long"),locs?.get(i)?.getString("img1"),locs?.get(i)?.getString("img2"),locs?.get(i)?.getString("img3"))
            }
        })
    }

    fun Bitmap.toRoundedCorners(
        cornerRadius: Float = 1800F
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
                RectF(0f,0f,width.toFloat(),width.toFloat()),
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

    fun getCircularBitmap(bitmap: Bitmap, desiredSize: Int): Bitmap {
        val outputBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val centerX = desiredSize / 2f
        val centerY = desiredSize / 2f
        val radius = desiredSize / 2f

        canvas.drawCircle(centerX, centerY, radius, paint)

        return outputBitmap
    }
    fun resizeBitmap(bitmap: Bitmap, desiredSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate the aspect ratio
        val aspectRatio = width.toFloat() / height.toFloat()

        // Calculate the new dimensions while preserving the aspect ratio
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = desiredSize
            newHeight = (desiredSize / aspectRatio).toInt()
        } else {
            newWidth = (desiredSize * aspectRatio).toInt()
            newHeight = desiredSize
        }

        // Resize the bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

}
