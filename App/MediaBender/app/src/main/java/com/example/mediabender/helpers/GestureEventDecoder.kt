package com.example.mediabender.helpers

import android.content.Context
import android.content.SharedPreferences
import com.example.mediabender.R
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.Gesture

class GestureEventDecoder(private val context: Context) {

    companion object{
        private val SHARED_PREFERENCE_NAME = "gesture_event_map"
    }

    private val sharedPreferences: SharedPreferences
    private var gestureMap: Map<Gesture, MediaEventType> = mapOf()
        private set

    init{
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val map = getFromSharedPreferences()
        if(map.entries.all { it.value == MediaEventType.NONE }){
            //Is not a custom gesture
            gestureMap =  mapOf(
                Gesture.LEFT to MediaEventType.PREVIOUS_SONG,
                Gesture.RIGHT to MediaEventType.SKIP_SONG,
                Gesture.NEAR to MediaEventType.PLAY,
                Gesture.FAR to MediaEventType.PAUSE,
                Gesture.UP to MediaEventType.RAISE_VOLUME,
                Gesture.DOWN to MediaEventType.LOWER_VOLUME,
                Gesture.NONE to MediaEventType.NONE
            )
        }else{
            gestureMap = map
        }
    }

    fun gestureToEvent(gesture: Gesture): MediaEventType{
        return gestureMap[gesture] ?: MediaEventType.NONE
    }

    fun updatedMap(newMap: Map<Gesture, MediaEventType>){
        gestureMap = newMap
        saveToSharedPreferences(newMap)
    }

    private fun getFromSharedPreferences():Map<Gesture, MediaEventType>{
        val map = HashMap<Gesture, MediaEventType>()
        map.put(Gesture.LEFT, MediaEventType.valueOf(sharedPreferences.getString(context.getString(R.string.gesture_event_map_left), "NONE") ?: "NONE"))
        map.put(Gesture.RIGHT, MediaEventType.valueOf(sharedPreferences.getString(context.getString(R.string.gesture_event_map_right), "NONE") ?: "NONE"))
        map.put(Gesture.UP, MediaEventType.valueOf(sharedPreferences.getString(context.getString(R.string.gesture_event_map_up), "NONE") ?: "NONE"))
        map.put(Gesture.DOWN, MediaEventType.valueOf(sharedPreferences.getString(context.getString(R.string.gesture_event_map_down), "NONE") ?: "NONE"))
        map.put(Gesture.NEAR, MediaEventType.valueOf(sharedPreferences.getString(context.getString(R.string.gesture_event_map_near), "NONE") ?: "NONE"))
        map.put(Gesture.FAR, MediaEventType.valueOf(sharedPreferences.getString(context.getString(R.string.gesture_event_map_far), "NONE") ?: "NONE"))

        return map
    }

    private fun saveToSharedPreferences(newMap: Map<Gesture, MediaEventType>){
        val editor = sharedPreferences.edit()

        editor.putString(context.getString(R.string.gesture_event_map_left), newMap[Gesture.LEFT]?.name ?: "NONE")
        editor.putString(context.getString(R.string.gesture_event_map_right), newMap[Gesture.RIGHT]?.name ?: "NONE")
        editor.putString(context.getString(R.string.gesture_event_map_up), newMap[Gesture.UP]?.name ?: "NONE")
        editor.putString(context.getString(R.string.gesture_event_map_down), newMap[Gesture.DOWN]?.name ?: "NONE")
        editor.putString(context.getString(R.string.gesture_event_map_near), newMap[Gesture.NEAR]?.name ?: "NONE")
        editor.putString(context.getString(R.string.gesture_event_map_far), newMap[Gesture.FAR]?.name ?: "NONE")
    }
}