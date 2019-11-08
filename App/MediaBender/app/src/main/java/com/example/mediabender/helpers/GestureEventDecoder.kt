package com.example.mediabender.helpers

import android.content.Context
import android.content.SharedPreferences
import android.provider.MediaStore
import com.example.mediabender.R
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.Gesture
import com.google.gson.Gson
import kotlin.collections.HashMap

class GestureEventDecoder(private val context: Context) {

    companion object{
        private val SHARED_PREFERENCE_NAME = "gesture_event_map"
    }

    private val sharedPreferences: SharedPreferences
    private var gestureMap: Map<Gesture, MediaEventType> = mapOf()
        private set

    init{
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        //val map: Map<Gesture, MediaEventType>? = null
        val map: Map<Gesture, MediaEventType> = getFromSharedPreferences()
        if (map.entries.all { it.value == MediaEventType.NONE }){    // map has never been initialized, initialize a basic map
            gestureMap =  mapOf(
                Gesture.LEFT to MediaEventType.PREVIOUS_SONG,
                Gesture.RIGHT to MediaEventType.SKIP_SONG,
                Gesture.NEAR to MediaEventType.PLAY,
                Gesture.FAR to MediaEventType.PAUSE,
                Gesture.UP to MediaEventType.RAISE_VOLUME,
                Gesture.DOWN to MediaEventType.LOWER_VOLUME,
                Gesture.NONE to MediaEventType.NONE
            )
            saveToSharedPreferences()
        } else {    // map has been initialized, so take it
            gestureMap = map
        }
    }

    fun gestureToEvent(gesture: Gesture): MediaEventType {
        val temp1 = gestureMap[gesture]
        val temp2 = gestureMap.get(gesture)
        val temp3 = gestureMap[gesture] ?: "NONE"

        return gestureMap[gesture] ?: MediaEventType.NONE
    }

    // retrieve the map from shared preference data
    // returns null if can't find map
    private fun getFromSharedPreferences(): Map<Gesture, MediaEventType> {
        return mapOf(
            Gesture.UP to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_up),"NULL")),
            Gesture.DOWN to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_down),"NULL")),
            Gesture.LEFT to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_left),"NULL")),
            Gesture.RIGHT to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_right),"NULL")),
            Gesture.FAR to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_far),"NULL")),
            Gesture.NEAR to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_near),"NULL")),
            Gesture.NONE to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_none),"NULL"))
        )
    }

    // save the mapping to shared preferences
    fun saveToSharedPreferences() {
        with(sharedPreferences.edit()) {
            putString(context.getString(R.string.gesture_up),gestureMap[Gesture.UP].toString())
            putString(context.getString(R.string.gesture_down),gestureMap[Gesture.DOWN].toString())
            putString(context.getString(R.string.gesture_left),gestureMap[Gesture.LEFT].toString())
            putString(context.getString(R.string.gesture_right),gestureMap[Gesture.RIGHT].toString())
            putString(context.getString(R.string.gesture_far),gestureMap[Gesture.FAR].toString())
            putString(context.getString(R.string.gesture_near),gestureMap[Gesture.NEAR].toString())
            apply()
        }
    }

    // edit the gestureMap member
    // NOTE: it is important to note that this function edits the member gestureMap, but DOES NOT
    //       SAVE IT TO SHARED PREFERENCES. We want to save the map only once, once all user changes
    //       have been made
    fun editGestureMap(gesture: Gesture, event: MediaEventType) {
        (gestureMap as HashMap<Gesture, MediaEventType>)[gesture] = event
    }

    // returns true if each MediaEventType is mapped to only one Gesture
    fun mapIsValid(): Boolean {
        return gestureMap.values.count() == gestureMap.values.distinct().count()
    }

    private fun stringToMediaEvent(str: String?): MediaEventType {
        return when(str) {
            "PLAY" -> MediaEventType.PLAY
            "PAUSE" -> MediaEventType.PAUSE
            "SKIP_SONG" -> MediaEventType.SKIP_SONG
            "PREVIOUS_SONG" -> MediaEventType.PREVIOUS_SONG
            "RAISE_VOLUME" -> MediaEventType.RAISE_VOLUME
            "LOWER_VOLUME" -> MediaEventType.LOWER_VOLUME
            "NONE" -> MediaEventType.NONE
            else -> MediaEventType.NONE // will never occur
        }
    }

}