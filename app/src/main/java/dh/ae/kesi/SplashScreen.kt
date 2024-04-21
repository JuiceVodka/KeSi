package dh.ae.kesi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Typeface
import android.opengl.Visibility
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import dh.ae.kesi.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private lateinit var binding : ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val sharedPreference =  this.getSharedPreferences("User_data", Context.MODE_PRIVATE)
        Log.d("SPLASH", sharedPreference.getString("uname", "UserNotDefinedYet").toString())
        var uname = sharedPreference.getString("uname", "UserNotDefinedYet")

        if (uname == "UserNotDefinedYet" || uname == null){
            binding.unameView.visibility = View.VISIBLE
            binding.splashView.visibility = View.GONE
        }else{
            startTimer(2000L)
        }

        binding.buttonConfirm.setOnClickListener {
            if (binding.unameField.text != null && binding.unameField.text.toString() != "Enter your username") {
                with(sharedPreference.edit()) {
                    putString("uname", binding.unameField.text.toString())
                    apply()
                }
            }
            startTimer(100L)
        }

    }

    fun startTimer(timer:Long){
        Handler().postDelayed({
            //replace SplashActivity with MainActivity after timer
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, timer)
    }
}