package com.example.mediabender.helpers

import android.content.Context
import android.content.SharedPreferences
import com.example.mediabender.R
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.models.PhoneEventType
import com.example.mediabender.service.Gesture
import com.google.common.collect.EnumBiMap

/**
 * This class define which gesture maps to which media event.
 */
class GestureEventDecoder constructor(private var context: Context) {

    companion object : SingletonHolder<GestureEventDecoder, Context>(::GestureEventDecoder)

    private val SHARED_PREFERENCE_NAME = "gesture_event_map"
    private val sharedPreferences: SharedPreferences

    private var lastModifiedMediaEvent: MediaEventType = MediaEventType.NONE
    private var lastBootedMediaEvent: MediaEventType = MediaEventType.NONE
    private var lastModifiedPhoneEvent: PhoneEventType = PhoneEventType.NONE
    private var lastBootedPhoneEvent: PhoneEventType = PhoneEventType.NONE
    private var mediaGestureMap: EnumBiMap<Gesture, MediaEventType>
    private var phoneGestureMap: EnumBiMap<Gesture, PhoneEventType>

    init{
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)

        val mediaMap: EnumBiMap<Gesture, MediaEventType>? = getMediaMapFromSharedPreferences()
        val phoneMap: EnumBiMap<Gesture, PhoneEventType>? = getPhoneMapFromSharedPreferences()

        if (mediaMap!!.all {it.value == MediaEventType.NONE}){    // map has never been initialized, initialize a basic map
            mediaGestureMap = EnumBiMap.create( mapOf(
                Gesture.LEFT to MediaEventType.PREVIOUS_SONG,
                Gesture.RIGHT to MediaEventType.SKIP_SONG,
                Gesture.NEAR to MediaEventType.TOGGLE_PLAYSTATE,
                Gesture.UP to MediaEventType.RAISE_VOLUME,
                Gesture.DOWN to MediaEventType.LOWER_VOLUME,
                Gesture.NONE to MediaEventType.NONE
            ))
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
        } else {    // map has been initialized, so take it
            phoneGestureMap = phoneMap
        }

        saveToSharedPreferences()
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
        var enumBiMap: EnumBiMap<Gesture,MediaEventType> = EnumBiMap.create( mapOf(
            Gesture.UP to stringToMediaEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_up_media), "NULL"))
        ))
        with(enumBiMap) {
            forcePut(Gesture.DOWN, stringToMediaEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_down_media),"NULL")))
            forcePut(Gesture.LEFT, stringToMediaEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_left_media),"NULL")))
            forcePut(Gesture.RIGHT, stringToMediaEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_right_media),"NULL")))
            forcePut(Gesture.FAR, stringToMediaEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_far_media),"NULL")))
            forcePut(Gesture.NEAR, stringToMediaEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_near_media),"NULL")))
            forcePut(Gesture.NONE, MediaEventType.NONE)
        }
        return enumBiMap
    }

    private fun getPhoneMapFromSharedPreferences(): EnumBiMap<Gesture, PhoneEventType>? {
        var enumBiMap: EnumBiMap<Gesture,PhoneEventType> = EnumBiMap.create( mapOf(
            Gesture.UP to stringToPhoneEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_up_phone), "NULL"))
        ))
        with(enumBiMap) {
            forcePut(Gesture.DOWN, stringToPhoneEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_down_phone),"NULL")))
            forcePut(Gesture.LEFT, stringToPhoneEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_left_phone ),"NULL")))
            forcePut(Gesture.RIGHT, stringToPhoneEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_right_phone),"NULL")))
            forcePut(Gesture.FAR, stringToPhoneEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_far_phone),"NULL")))
            forcePut(Gesture.NEAR, stringToPhoneEvent(sharedPreferences.getString(context.getString(
                R.string.gesture_near_phone),"NULL")))
            forcePut(Gesture.NONE, PhoneEventType.NONE)
        }
        return enumBiMap
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
    fun editMap(gesture: Gesture, event: MediaEventType) {
        if (lastModifiedMediaEvent == event) { // if we are changing the same event twice in a row
            mediaGestureMap.forcePut( // re-map the last booted event with it's previous gesture
                mediaGestureMap.inverse()[event], // booted events gesture is current gesture of double modified
                lastBootedMediaEvent
            )
        }

        // if the event at the given gesture doesnt exist, its because we are not booting anything
        if (mediaGestureMap[gesture] == null) {
            lastBootedMediaEvent = event
        } else { // otherwise we are booting something, and its the event at whatever gesture we are changing to
            lastBootedMediaEvent = mediaGestureMap[gesture]!!
        }

        lastModifiedMediaEvent = event // last modified is now the desired event we wanted to modify

        mediaGestureMap.forcePut(gesture, event) // performing the desired change
    }
    fun editMap(gesture: Gesture, event: PhoneEventType) {
        if (lastModifiedPhoneEvent == event) { // if we are changing the same event twice in a row
            phoneGestureMap.forcePut( // re-map the last booted event with it's previous gesture
                phoneGestureMap.inverse()[event], // booted events gesture is current gesture of double modified
                lastBootedPhoneEvent
            )
        }

        // if the event at the given gesture doesnt exist, its because we are not booting anything
        if (phoneGestureMap[gesture] == null) {
            lastBootedPhoneEvent = event
        } else { // otherwise we are booting something, and its the event at whatever gesture we are changing to
            lastBootedPhoneEvent = phoneGestureMap[gesture]!!
        }

        lastModifiedPhoneEvent = event // last modified is now the desired event we wanted to modify

        phoneGestureMap.forcePut(gesture, event) // performing the desired change
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
        // 6 from media controls map: 5 for the gestures, 1 for none
        // 3 from phone map         : 2 for the gestures, 1 for none
        return mediaGestureMap.keys.count() == 6 && phoneGestureMap.keys.count() == 5
    }

    private fun stringToMediaEvent(str: String?): MediaEventType {
        return when(str) {
            "TOGGLE_PLAYSTATE" -> MediaEventType.TOGGLE_PLAYSTATE
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