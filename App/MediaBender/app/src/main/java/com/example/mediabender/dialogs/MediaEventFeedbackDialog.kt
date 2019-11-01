package com.example.mediabender.dialogs

import android.content.Context.AUDIO_SERVICE
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.mediabender.R
import com.example.mediabender.models.MediaEventType


class MediaEventFeedbackDialog(private val event: MediaEventType) : DialogFragment() {

    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_media_feedback, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        imageView = view.findViewById(R.id.media_feedback_image_viewer)

        val image = getImageForEvent()
        image?.let{
            it.setColorFilter(BlendModeColorFilter(Color.WHITE, BlendMode.SRC_ATOP))
            imageView.setImageDrawable(it)
        } ?: dismiss()

        Handler().postDelayed( {
            dismiss()
        },1500L)
    }

    private fun getImageForEvent(): Drawable? {

        var image: Drawable?

        if (event == MediaEventType.LOWER_VOLUME || event == MediaEventType.RAISE_VOLUME) {
            val volumeLevel = getVolumeLevel()

            image = when (volumeLevel) {
                in 0.75..1.0001 -> activity!!.getDrawable(R.drawable.ic_volume_high)
                in 0.4..0.75 -> activity!!.getDrawable(R.drawable.ic_volume_medium)
                in 0.1..0.4 -> activity!!.getDrawable(R.drawable.ic_volume_low)
                else -> activity!!.getDrawable(R.drawable.ic_volume_off)
            }
        } else {
            image = when(event){
                MediaEventType.PLAY -> activity!!.getDrawable(R.drawable.ic_play_arrow)
                MediaEventType.PAUSE -> activity!!.getDrawable(R.drawable.ic_pause)
                MediaEventType.PREVIOUS_SONG -> activity!!.getDrawable(R.drawable.ic_backward_track)
                MediaEventType.SKIP_SONG -> activity!!.getDrawable(R.drawable.ic_skip_track)
                else -> null
            }
        }

        return image

    }

    fun getVolumeLevel(): Double {

        activity?.let {
            val am = it.getSystemService(AUDIO_SERVICE) as AudioManager?
            val maxVolume = am?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toDouble() ?: 0.0
            val volumeLevel = am?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toDouble() ?: 0.0

            if (maxVolume == 0.0) {
                //Not max volume, cannot get any sound
                return 0.0
            } else {
                return volumeLevel / maxVolume
            }
        }

        //Cannot get activity
        return 0.0
    }
}