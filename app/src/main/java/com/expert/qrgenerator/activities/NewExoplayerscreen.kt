package com.expert.qrgenerator.activities

import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.expert.qrgenerator.R
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.*


class NewExoplayerscreen : AppCompatActivity() {
    var vdo_ContentVideo: VideoView? = null
    var pDialog: ProgressDialog? = null
    var progressBar: ProgressBar? = null
    var playerView: PlayerView? = null
    var player: SimpleExoPlayer? = null
    var arrayList = ArrayList<UrlData>()
    var firstUrl = ""
    var youTubePlayerView: YouTubePlayerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_exoplayerscreen)

        progressBar = findViewById(R.id.grid_progress)
       // changeScreenOrientation()
//        val rootRef = FirebaseDatabase.getInstance().reference
//        val moviesRef = rootRef.child("ruvideos")
//        Log.wtf("ROOT_FOUND_THE_VALUE", "VALUE$moviesRef")
        val VideoUrl = intent.getStringExtra("key")
        Log.wtf("NewURll", "data")
        Log.wtf("LoadUrl", "url$VideoUrl")
        if (VideoUrl != null) {

            //Log.wtf("LoadUrlExpoplayer", "url$VideoUrl")
            youTubePlayerView = findViewById(R.id.youtube_player_view)
            lifecycle.addObserver(youTubePlayerView!!)


            val videolink = VideoPlayerScreen.getVideoId(VideoUrl)
            Log.wtf("URL_VIDEO_LIST", "list" + videolink);

            youTubePlayerView!!.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    val videoId = videolink
                    youTubePlayer.loadVideo(videoId, 0f)
                }
            })


            // andExoPlayerView.setSource(VideoUrl);
        }


    }

    fun getVideoId(watchLink: String): String? {
        return watchLink.substring(watchLink.length - 11)
    }


    override fun onPause() {
        super.onPause()
        youTubePlayerView!!.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.wtf("Destroymethod", "methodCalled")
        youTubePlayerView!!.release()
    }
    private fun changeScreenOrientation() {
        val orientation: Int = this.getResources().getConfiguration().orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
          //  showMediaDescription()
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
         //   hideMediaDescription()
        }
//        if (Settings.System.getInt(
//                contentResolver,
//                Settings.System.ACCELEROMETER_ROTATION, 0
//            ) === 1
//        )
        {
            val handler = Handler()
            handler.postDelayed(Runnable {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }, 4000)
        }
    }
}