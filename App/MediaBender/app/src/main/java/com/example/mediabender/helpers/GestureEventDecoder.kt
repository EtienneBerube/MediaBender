package com.example.mediabender.helpers

import android.content.Context
import android.content.SharedPreferences
import com.example.mediabender.R
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.models.PhoneEventType
import com.example.mediabender.service.Gesture
import com.google.common.collect.EnumBiMap

class GestureEventDecoder private constructor(private var context: Context) {

    companion object : SingletonHolder<GestureEventDecoder, Context>(::GestureEventDecoder)

    private val SHARED_PREFERENCE_NAME = "gesture_event_map"
    private val sharedPreferences: SharedPreferences

    // map types for gettting from shared preferences
    private val MAP_MEDIA: Int = 0
    private val MAP_PHONE: Int = 1
    private var mediaGestureMap: EnumBiMap<Gesture, MediaEventType>
    private var phoneGestureMap: EnumBiMap<Gesture, PhoneEventType>

    init{
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        // TODO: there is an important weird edge case: if the user manually sets all of their gestures
        // to none, then the app will reinitialize their gestures to the default gestures

        val mediaMap: EnumBiMap<Gesture, MediaEventType>? = getMediaMapFromSharedPreferences()
        val phoneMap: EnumBiMap<Gesture, PhoneEventType>? = getPhoneMapFromSharedPreferences()

        if (mediaMap!!.all {it.value == MediaEventType.NONE}){    // map has never been initialized, initialize a basic map
            mediaGestureMap = EnumBiMap.create( mapOf(
                Gesture.LEFT to MediaEventType.PREVIOUS_SONG,
                Gesture.RIGHT to MediaEventType.SKIP_SONG,
                Gesture.NEAR to MediaEventType.PLAY,
                Gesture.FAR to MediaEventType.PAUSE,
                Gesture.UP to MediaEventType.RAISE_VOLUME,
                Gesture.DOWN to MediaEventType.LOWER_VOLUME,
                Gesture.NONE to MediaEventType.NONE
            ))
            saveToSharedPreferences()
        } else {    // map has been initialized, so take it
            mediaGestureMap = mediaMap
        }

        if (phoneMap!!.all {it.value == PhoneEventType.NONE}){    // map has never been initialized, initialize a basic map
            phoneGestureMap = EnumBiMap.create( mapOf(
                Gesture.LEFT to PhoneEventType.DECLINE_CALL,
                Gesture.RIGHT to PhoneEventType.ACCEPT_CALL,
                Gesture.UP to PhoneEventType.RAISE_VOLUME,
                Gesture.DOWN to PhoneEventType.LOWER_VOLUME,
                Gesture.NONE to PhoneEventType.NONE
            ))
            saveToSharedPreferences()
        } else {    // map has been initialized, so take it
            phoneGestureMap = phoneMap
        }






    }

    fun gestureToMediaEvent(gesture: Gesture): MediaEventType {
        return mediaGestureMap[gesture] ?: MediaEventType.NONE
    }
    fun gestureToPhoneEvent(gesture: Gesture) : PhoneEventType {
        return phoneGestureMap[gesture] ?: PhoneEventType.NONE
    }

    fun mediaEventToGesture(event: MediaEventType): Gesture {
        return mediaGestureMap.inverse()[event] ?: Gesture.NONE
    }
    fun phoneEventToGesture(event: PhoneEventType): Gesture {
        return phoneGestureMap.inverse()[event] ?: Gesture.NONE
    }


    // retrieve the desired map from shared preference data
    // for MAP_TYPE use one of the two constants defined at top of class, either:
    //      MAP_MEDIA to retrieve the map of media controls
    //      MAP_PHONE to retrieve the map of phone controls
    private fun getMediaMapFromSharedPreferences(): EnumBiMap<Gesture, MediaEventType>? {
        return EnumBiMap.create( mapOf(
            Gesture.UP to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_up_media),"NULL")),
            Gesture.DOWN to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_down_media),"NULL")),
            Gesture.LEFT to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_left_media),"NULL")),
            Gesture.RIGHT to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_right_media),"NULL")),
            Gesture.FAR to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_far_media),"NULL")),
            Gesture.NEAR to stringToMediaEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_near_media),"NULL")),
            Gesture.NONE to MediaEventType.NONE
        ))
    }

    private fun getPhoneMapFromSharedPreferences(): EnumBiMap<Gesture, PhoneEventType>? {
        return EnumBiMap.create( mapOf(
            Gesture.UP to stringToPhoneEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_up_phone),"NULL")),
            Gesture.DOWN to stringToPhoneEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_down_phone),"NULL")),
            Gesture.LEFT to stringToPhoneEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_left_phone),"NULL")),
            Gesture.RIGHT to stringToPhoneEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_right_phone),"NULL")),
            Gesture.FAR to stringToPhoneEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_far_phone),"NULL")),
            Gesture.NEAR to stringToPhoneEvent(
                sharedPreferences.getString(context.getString(R.string.gesture_near_phone),"NULL")),
            Gesture.NONE to PhoneEventType.NONE
        ))
    }

    // save the mapping to shared preferences
    fun saveToSharedPreferences() {
        with(sharedPreferences.edit()) {
            // saving the media map
            putString(context.getString(R.string.gesture_up_media),mediaGestureMap[Gesture.UP].toString())
            putString(context.getString(R.string.gesture_down_media),mediaGestureMap[Gesture.DOWN].toString())
            putString(context.getString(R.string.gesture_left_media),mediaGestureMap[Gesture.LEFT].toString())
            putString(context.getString(R.string.gesture_right_media),mediaGestureMap[Gesture.RIGHT].toString())
            putString(context.getString(R.string.gesture_far_media),mediaGestureMap[Gesture.FAR].toString())
            putString(context.getString(R.string.gesture_near_media),mediaGestureMap[Gesture.NEAR].toString())

            // saving the phone map
            putString(context.getString(R.string.gesture_up_phone),phoneGestureMap[Gesture.UP].toString())
            putString(context.getString(R.string.gesture_down_phone),phoneGestureMap[Gesture.DOWN].toString())
            putString(context.getString(R.string.gesture_left_phone),phoneGestureMap[Gesture.LEFT].toString())
            putString(context.getString(R.string.gesture_right_phone),phoneGestureMap[Gesture.RIGHT].toString())
            putString(context.getString(R.string.gesture_far_phone),phoneGestureMap[Gesture.FAR].toString())
            putString(context.getString(R.string.gesture_near_phone),phoneGestureMap[Gesture.NEAR].toString())

            apply()
        }
    }

    // edit the mediaGestureMap member
    // NOTE: this function uses forcePut because EnumBiMap does not support multiple keys mapping to
    //       the same value. The method forcePut removes any other key-values pairs in the map that
    //       also have the same value as the one that is being put into the map. Thus, the size of
    //       the map can:
    //          -> decrease (if there are 2 more entries with the same value already in map)
    //          -> stay same (if there is exactly 1 entry with the same value already in map)
    //          -> increase (if there are no entries with the same value already in the map)
    // NOTE: it is important to note that this function edits the member mediaGestureMap, but DOES NOT
    //       SAVE IT TO SHARED PREFERENCES. We want to save the map only once, once all user changes
    //       have been made
    fun editGestureMap(gesture: Gesture, event: MediaEventType) {
        mediaGestureMap.forcePut(gesture, event)
    }
    fun editPhoneMap(gesture: Gesture, event: PhoneEventType) {
        phoneGestureMap.forcePut(gesture, event)
    }

    fun updateGestupMap(newMap: EnumBiMap<Gesture, MediaEventType>){
        this.mediaGestureMap = newMap
        saveToSharedPreferences()
    }

    // returns true if each MediaEventType is mapped to only one Gesture
    // since we are using forcePut to enter key-value pairs, if ever the user has chosen to override
    // one of the mapping, then the mediaGestureMap will have a length of less than 7, and the
    // phoneGestureMap will have a length of less than 3
    fun mapsAreValid(): Boolean {
        // 7 from media controls map: 6 for the gestures, 1 for none
        // 3 from phone map         : 2 for the gestures, 1 for none
        return mediaGestureMap.keys.count() == 7 && phoneGestureMap.keys.count() == 3
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
    private fun stringToPhoneEvent(str: String?): PhoneEventType {
        return when(str) {
            "RAISE_VOLUME" -> PhoneEventType.RAISE_VOLUME
            "LOWER_VOLUME" -> PhoneEventType.LOWER_VOLUME
            "ACCEPT_CALL" -> PhoneEventType.ACCEPT_CALL
            "DECLINE_CALL" -> PhoneEventType.DECLINE_CALL
            else -> PhoneEventType.NONE // will never occur
        }
    }

}