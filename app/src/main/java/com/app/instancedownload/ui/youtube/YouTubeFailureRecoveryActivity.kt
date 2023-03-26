package com.app.instancedownload.ui.youtube

import android.content.Intent
import android.widget.Toast
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.app.instancedownload.BuildConfig

abstract class YouTubeFailureRecoveryActivity : YouTubeBaseActivity(),
    YouTubePlayer.OnInitializedListener {
    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider,
        errorReason: YouTubeInitializationResult
    ) {
        if (errorReason.isUserRecoverableError) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show()
        } else {
            val errorMessage = String.format(BuildConfig.youtubeApi, errorReason.toString())
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubePlayerProvider.initialize(BuildConfig.youtubeApi, this)
        }
    }

    protected abstract val youTubePlayerProvider: YouTubePlayer.Provider

    companion object {
        private const val RECOVERY_DIALOG_REQUEST = 1
    }
}