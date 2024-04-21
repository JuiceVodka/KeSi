package dh.ae.kesi.ui.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import dh.ae.kesi.databinding.FragmentEntryBinding
import java.util.Base64.getDecoder

class EntryFragment : Fragment() {

    public interface EntryFragmentListener{
        fun goToMapFragment(id:String, mult:Double, lat :String, lng :String)
    }

    private lateinit var binding : FragmentEntryBinding
    private var myBooleanArray:BooleanArray = booleanArrayOf(false, false, false, false)
    private lateinit var mainActivity : EntryFragmentListener
    private var mult :Double = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as EntryFragmentListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEntryBinding.inflate(layoutInflater)

        val objectId = arguments?.getString("objectId")
        val img1String = arguments?.getString("img1")
        val img2String = arguments?.getString("img2")
        val img3String = arguments?.getString("img3")
        val lat = arguments?.getString("lat")
        val lng = arguments?.getString("long")

        val bitmaps = listOf(img1String, img2String, img3String).map { decodeImg(it!!) }

        binding.imageView1.setImageBitmap(bitmaps[0])

        binding.imageView2.setOnClickListener {
            if (!myBooleanArray[0]) {
                bitmaps[1].apply {
                    //with rounded corners
                    binding.imageView2.setImageBitmap(this)
                    binding.imageView2.setImageBitmap(toRoundedCorners(40F))
                    mult-=0.1
                }
                myBooleanArray[0] = true
            }
        }
        binding.imageView3.setOnClickListener {
            if (!myBooleanArray[1]) {
                bitmaps[2].apply {
                    //with rounded corners
                    binding.imageView3.setImageBitmap(this)
                    binding.imageView3.setImageBitmap(toRoundedCorners(40F))
                    mult-=0.1
                }
                myBooleanArray[1] = true
            }

        }
        binding.imageView4.setOnClickListener {
            if (!myBooleanArray[2]) {
                bitmaps[3].apply {
                    //with rounded corners
                    binding.imageView4.setImageBitmap(this)
                    binding.imageView4.setImageBitmap(toRoundedCorners(40F))
                    mult-=0.1
                }
                myBooleanArray[2] = true
            }

        }
        binding.imageView5.setOnClickListener {
            if (!myBooleanArray[3]) {
                bitmaps[4].apply {
                    //with rounded corners
                    binding.imageView5.setImageBitmap(this)
                    binding.imageView5.setImageBitmap(toRoundedCorners(40F))
                    mult-=0.1
                }
                myBooleanArray[3] = true
            }
        }

        binding.GuessButton.setOnClickListener {
            Log.d("TEST", lat.toString())
            Log.d("TEST", lng.toString())
            Log.d("TEST", objectId.toString())
            mainActivity.goToMapFragment(objectId!!, mult, lat!!, lng!!)
        }

        return binding.root
    }
    private fun decodeImg(img: String): Bitmap{
        val dec = getDecoder().decode(img)
        return BitmapFactory.decodeByteArray(dec, 0, dec!!.size)
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




}