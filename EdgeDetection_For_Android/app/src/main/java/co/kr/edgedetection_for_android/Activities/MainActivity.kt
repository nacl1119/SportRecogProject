package co.kr.edgedetection_for_android.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kr.edgedetection_for_android.R
import com.google.android.gms.ads.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGoogleAd()
        initWidgets()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("앱 종료").setMessage("앱을 종료하시겠습니까?")

        builder.setPositiveButton("OK") { dialog, id ->

        }

        builder.setNegativeButton("Cancel") { dialog, id -> }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

    }

    private fun initGoogleAd() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

    }

    private fun initWidgets() {
        activity_main_start_process_btn.setOnClickListener {
            val intent = Intent(this, ProcessActivity::class.java)
            startActivity(intent)
        }
    }
}