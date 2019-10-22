package com.example.mediabender.dialogs

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.mediabender.R
import java.lang.ClassCastException


//TODO add connection service
class PlayerConnectionDialog(val player: MediaPlayerType) : DialogFragment() {

    enum class MediaPlayerType(val playerName: String) {
        SPOTIFY("Spotify"),
        APPLE_MUSIC("Apple Music"),
        GOOGLE_PLAY("Google Play Music");
    }

    interface ConnectionDialogListener{
        fun acceptConnectionOnDismiss(name: MediaPlayerType)
    }

    lateinit var title: TextView
    lateinit var connectButton: Button
    lateinit var cancelButton: Button
    lateinit var email: TextView
    lateinit var password: TextView

    lateinit var listener: ConnectionDialogListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_settings_player_connection, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = view.findViewById(R.id.settings_dialog_player_title)
        connectButton = view.findViewById(R.id.settings_dialog_connect_button)
        cancelButton = view.findViewById(R.id.settings_dialog_cancel_button)
        email = view.findViewById(R.id.settings_dialog_email_textview)
        password = view.findViewById(R.id.settings_dialog_password_textview)

        setupTheme()

        cancelButton.setOnClickListener { dismiss() }
        connectButton.setOnClickListener { listener.acceptConnectionOnDismiss(player) }
    }

    private fun setupTheme(){
        title.text = player.playerName

            when(player){
            MediaPlayerType.SPOTIFY -> {
                title.setTextColor(context!!.getColor(R.color.spotify_primary))
                connectButton.background = context!!.getDrawable(R.drawable.spotify_connection_button)
            }

            MediaPlayerType.APPLE_MUSIC -> {
                title.setTextColor(context!!.getColor(R.color.apple_music_primary))
                connectButton.background = context!!.getDrawable(R.drawable.apple_music_connection_button)
            }

            MediaPlayerType.GOOGLE_PLAY -> {
                title.setTextColor(context!!.getColor(R.color.google_play_primary))
                connectButton.background = context!!.getDrawable(R.drawable.google_play_connection_button)
            }

        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            listener = context as ConnectionDialogListener
        }catch (e: ClassCastException){
            Log.d("Connection Dialog","Cannot cast context as [ConnectionDialogListener] for $player")
        }
    }
}