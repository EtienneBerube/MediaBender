package com.example.mediabender.helpers

import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView

/**
 * THis class is a view holder for the player quicklinks cards.
 */
class PlayerSettingsCardViewHolder {
    private val CONNECTED_PREFIX = "Connected as: "
    private val NOT_CONNECTED = "Not connected"

    lateinit var connectedTextView: TextView
    lateinit var activeCircle: ImageView
    lateinit var cardView: CardView
}