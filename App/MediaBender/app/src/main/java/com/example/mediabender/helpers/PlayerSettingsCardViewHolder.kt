package com.example.mediabender.helpers

import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.mediabender.models.PlayerAccount

class PlayerSettingsCardViewHolder {
    private val CONNECTED_PREFIX = "Connected as: "
    private val NOT_CONNECTED = "Not connected"

    lateinit var connectedTextView: TextView
    lateinit var activeCircle: ImageView
    lateinit var cardView: CardView

    fun setConnectedTextWithEmail(account: PlayerAccount?){
        if(account != null) {
            connectedTextView.text = CONNECTED_PREFIX + "${account.email}"
        }else{
            connectedTextView.text = NOT_CONNECTED
        }

    }
}