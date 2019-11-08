package com.example.mediabender.models

import com.google.gson.annotations.SerializedName

enum class MediaEventType {
    RAISE_VOLUME,
    LOWER_VOLUME,
    SKIP_SONG,
    PREVIOUS_SONG,
    PLAY,
    PAUSE,
    NONE,
    TOGGLE
}