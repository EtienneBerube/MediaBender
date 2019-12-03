package com.example.mediabender.models

/**
 * Enum for the different currently supported media players and their package name
 */
enum class MediaPlayer(var packageName: String, var prettyName: String) {
    SPOTIFY("com.spotify.music", "Spotify"),
    APPLE_MUSIC("com.apple.android.music", "Apple Music"),
    GOOGLE_PLAY("com.google.android.music", "Google Play Music")
}