package com.example.mediabender.models

/**
 * Enum for the different event types
 */
enum class MediaEventType {
    RAISE_VOLUME,
    LOWER_VOLUME,
    SKIP_SONG,
    PREVIOUS_SONG,
    TOGGLE_PLAYSTATE,
    NONE
}