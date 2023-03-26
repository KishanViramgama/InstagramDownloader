package com.app.instancedownload.ui.videoplayer.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import com.app.instancedownload.R
import com.app.instancedownload.databinding.ActivityVideoPlayerBinding
import com.app.instancedownload.util.Method
import com.app.instancedownload.util.changeStatusBarColor
import com.app.instancedownload.util.gone
import com.app.instancedownload.util.visible
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class VideoPlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var method: Method

    lateinit var binding: ActivityVideoPlayerBinding

    private lateinit var player: ExoPlayer

    @androidx.annotation.OptIn(BuildCompat.PrereleaseSdkCheck::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_player)

        //Making notification bar transparent
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        if (BuildCompat.isAtLeastT()) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                // Back is pressed... Finishing the activity
                player.playWhenReady = false
                player.stop()
                player.release()
                finish()
            }
        } else {
            onBackPressedDispatcher.addCallback(this /* lifecycle owner */,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Back is pressed... Finishing the activity
                        player.playWhenReady = false
                        player.stop()
                        player.release()
                        finish()
                    }
                })
        }

        changeStatusBarColor(this)

        val intent = intent
        val videoLink = intent.getStringExtra("link")!!

        binding.progressBarVideoPlay.visible()

        val trackSelector = DefaultTrackSelector(this@VideoPlayerActivity)
        player = ExoPlayer.Builder(this@VideoPlayerActivity).setTrackSelector(trackSelector).build()
        binding.playerView.player = player

        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this@VideoPlayerActivity,
            Util.getUserAgent(this@VideoPlayerActivity, resources.getString(R.string.app_name))
        )
        // This is the MediaSource representing the media to be played.
        val mediaItem = MediaItem.fromUri(Uri.fromFile(File(videoLink)))
        val mediaSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
        // Prepare the player with the source.
        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playWhenReady: Boolean) {
                if (playWhenReady) {
                    binding.progressBarVideoPlay.gone()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.d("show_error", error.toString())
                binding.progressBarVideoPlay.gone()
                method.alertBox(this@VideoPlayerActivity, resources.getString(R.string.wrong))
            }
        })

    }

    override fun onPause() {
        player.playWhenReady = false
        super.onPause()
    }

    override fun onDestroy() {
        player.playWhenReady = false
        player.stop()
        player.release()
        super.onDestroy()
    }
}