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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout

import dh.ae.kesi.databinding.FragmentEntryBinding
import java.util.Base64.getDecoder
import kotlin.math.round

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
        val img4String = arguments?.getString("img4")
        val img5String = arguments?.getString("img5")
        val lat = arguments?.getString("lat")
        val lng = arguments?.getString("long")

        val bitmaps = listOf(img1String, img2String, img3String, img4String, img5String).map { decodeImg(it!!) }
        val displayMetrics = resources.displayMetrics.xdpi
        binding.imageView1.setImageBitmap(bitmaps[0].toRoundedCorners(40F))
        val calc = displayMetrics/2 - binding.imageView1.width/2
        val layout = binding.imageView1.layoutParams as LinearLayout.LayoutParams
        layout.setMargins(calc.toInt(), 0, 0, 0)
        binding.imageView1.layoutParams = layout

        binding.imageView2.setOnClickListener {
            if (!myBooleanArray[0]) {
                bitmaps[1].apply {
                    crossfade(binding.imageView2, this)
                }
                myBooleanArray[0] = true
            }
        }
        binding.imageView3.setOnClickListener {
            if (!myBooleanArray[1]) {
                bitmaps[2].apply {
                    //with rounded corners
                    crossfade(binding.imageView3, this)
                    val calc = displayMetrics/2 - binding.imageView3.width/2
                    val layout = binding.imageView3.layoutParams as LinearLayout.LayoutParams
                    layout.setMargins(calc.toInt(), 0, 0, 0)
                    binding.imageView3.layoutParams = layout
                }
                myBooleanArray[1] = true
            }

        }
        /*binding.imageView4.setOnClickListener {
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
        }*/

        binding.problem.setOnClickListener {
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
    fun crossfade(imageView: ImageView, bitmap: Bitmap) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000

        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 1000

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                mult -= 0.1
                "Deduction: ${round(100*(1-mult)).toInt()}%".also { binding.deduction.text = it }
                imageView.setImageBitmap(bitmap)
                bitmap.toRoundedCorners(40F)
                imageView.startAnimation(fadeIn) // Start fadeIn animation here
                imageView.setImageBitmap(bitmap.toRoundedCorners(40F))
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // No need to set the image bitmap or start fadeOut animation here
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        imageView.startAnimation(fadeOut) // Start fadeOut animation first
    }




}