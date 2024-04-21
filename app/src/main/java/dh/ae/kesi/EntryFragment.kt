package dh.ae.kesi

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

import dh.ae.kesi.databinding.FragmentEntryBinding
import java.util.Base64.getDecoder

class EntryFragment : Fragment() {
    private lateinit var binding : FragmentEntryBinding
    private var myBooleanArray:BooleanArray = booleanArrayOf(false, false, false, false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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




        val bitmaps = listOf(img1String, img2String, img3String, img4String, img5String).map { decodeImg(it!!) }

        Log.d("test", objectId.toString())

        binding.imageView1.setImageBitmap(bitmaps[0])

        binding.imageView2.setOnClickListener {
            if (!myBooleanArray[0]) {
                bitmaps[1].apply {
                    //with rounded corners
                    crossfade(binding.imageView2, this)
                    binding.imageView2.setImageBitmap(toRoundedCorners(8F))
                }
                myBooleanArray[0] = true
            }
        }
        binding.imageView3.setOnClickListener {
            if (!myBooleanArray[1]) {
                bitmaps[2].apply {
                    //with rounded corners
                    crossfade(binding.imageView3, this)
                    binding.imageView3.setImageBitmap(toRoundedCorners(8F))
                }
                myBooleanArray[1] = true
            }

        }
        binding.imageView4.setOnClickListener {
            if (!myBooleanArray[2]) {
                bitmaps[3].apply {
                    //with rounded corners
                    crossfade(binding.imageView4, this)
                    binding.imageView4.setImageBitmap(toRoundedCorners(8F))
                }
                myBooleanArray[2] = true
            }

        }
        binding.imageView5.setOnClickListener {
            if (!myBooleanArray[3]) {
                bitmaps[4].apply {
                    //with rounded corners
                    crossfade(binding.imageView5, this)
                    binding.imageView5.setImageBitmap(toRoundedCorners(8F))
                }
                myBooleanArray[3] = true
            }
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

    private fun crossfade(imageView: ImageView,bitmap: Bitmap) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1000
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.duration = 1000

        fadeIn.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                imageView.setImageBitmap(bitmap)
                //imageView.startAnimation(fadeOut)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        imageView.startAnimation(fadeOut)
        imageView.startAnimation(fadeIn)
    }




}