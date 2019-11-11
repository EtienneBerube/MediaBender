package com.example.mediabender.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.mediabender.R
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.Gesture

class GestureMappingActivity : AppCompatActivity() {

    private lateinit var gestureEventDecoder: GestureEventDecoder

    private lateinit var spinner_up: Spinner
    private lateinit var spinner_down: Spinner
    private lateinit var spinner_left: Spinner
    private lateinit var spinner_right: Spinner
    private lateinit var spinner_far: Spinner
    private lateinit var spinner_near: Spinner

    private lateinit var b_save_gestures: Button

    private lateinit var controls_standard: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_mapping)

        gestureEventDecoder = GestureEventDecoder(applicationContext)
        setupUI()
    }

    // need to make sure that when someone tries to go back, they are aware their map wasn't saved
    override fun onBackPressed() {
        Toast.makeText(applicationContext, "Changes not saved.", Toast.LENGTH_LONG).show()
        super.onBackPressed()
    }

    private fun setupUI() {

        b_save_gestures = findViewById(R.id.b_save_gestures)

        b_save_gestures.setOnClickListener {

            // before saving, need to make sure that the mappings are unique
            if (gestureEventDecoder.mapIsValid()) {
                saveGestures()
                Toast.makeText(applicationContext, "Saved.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(applicationContext,
                    "Each control must have only one associated gesture.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        spinner_up = findViewById(R.id.spinner_up)
        spinner_down = findViewById(R.id.spinner_down)
        spinner_left = findViewById(R.id.spinner_left)
        spinner_right = findViewById(R.id.spinner_right)
        spinner_far = findViewById(R.id.spinner_far)
        spinner_near = findViewById(R.id.spinner_near)

        // creating array of standard controls options for spinners
        controls_standard = arrayOf(
            getString(R.string.mapping_spinner_play),
            getString(R.string.mapping_spinner_pause),
            getString(R.string.mapping_spinner_next),
            getString(R.string.mapping_spinner_previous),
            getString(R.string.mapping_spinner_volumeUp),
            getString(R.string.mapping_spinner_volumeDown)
        )

        // creating an array adapter for the spinners
        ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            controls_standard
        ).also { adapter ->
            // specifying the layout for the list when it appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // applying the adapter to the spinners
            spinner_up.adapter = adapter
            spinner_down.adapter = adapter
            spinner_left.adapter = adapter
            spinner_right.adapter = adapter
            spinner_far.adapter = adapter
            spinner_near.adapter = adapter
        }

        // setting the starting value of the spinner based on user shared preferences
        spinner_up.setSelection(getSpinnerStartingPosition(Gesture.UP))
        spinner_down.setSelection(getSpinnerStartingPosition(Gesture.DOWN))
        spinner_left.setSelection(getSpinnerStartingPosition(Gesture.LEFT))
        spinner_right.setSelection(getSpinnerStartingPosition(Gesture.RIGHT))
        spinner_far.setSelection(getSpinnerStartingPosition(Gesture.FAR))
        spinner_near.setSelection(getSpinnerStartingPosition(Gesture.NEAR))

        // defining then assigning the onItemSelectedListener to all spinners
        var myOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // based on the spinner, fetch the media control chosen
                val event: MediaEventType = when (parent?.getItemAtPosition(position).toString()) {
                    getString(R.string.mapping_spinner_play) -> MediaEventType.PLAY
                    getString(R.string.mapping_spinner_pause) -> MediaEventType.PAUSE
                    getString(R.string.mapping_spinner_next) -> MediaEventType.SKIP_SONG
                    getString(R.string.mapping_spinner_previous) -> MediaEventType.PREVIOUS_SONG
                    getString(R.string.mapping_spinner_volumeUp) -> MediaEventType.RAISE_VOLUME
                    getString(R.string.mapping_spinner_volumeDown) -> MediaEventType.LOWER_VOLUME
                    else -> MediaEventType.NONE // this case will never happen
                    }

                // based on which spinner, fetch the gesture for the associated spinner
                val gesture: Gesture = when (parent?.id) {
                    R.id.spinner_up -> Gesture.UP
                    R.id.spinner_down -> Gesture.DOWN
                    R.id.spinner_left -> Gesture.LEFT
                    R.id.spinner_right -> Gesture.RIGHT
                    R.id.spinner_far -> Gesture.FAR
                    R.id.spinner_near -> Gesture.NEAR
                    else -> Gesture.NONE    // this case will never happen
                }
                gestureEventDecoder.editGestureMap(gesture, event)
                // TODO might be interesting to add feedback to the user saying "hey, this gesture is for more than one control"
            }
        }
        findViewById<Spinner>(R.id.spinner_up).onItemSelectedListener = myOnItemSelectedListener
        findViewById<Spinner>(R.id.spinner_down).onItemSelectedListener = myOnItemSelectedListener
        findViewById<Spinner>(R.id.spinner_left).onItemSelectedListener = myOnItemSelectedListener
        findViewById<Spinner>(R.id.spinner_right).onItemSelectedListener = myOnItemSelectedListener
        findViewById<Spinner>(R.id.spinner_far).onItemSelectedListener = myOnItemSelectedListener
        findViewById<Spinner>(R.id.spinner_near).onItemSelectedListener = myOnItemSelectedListener
    }

    // save the gesture map to shared preferences
    fun saveGestures() {
        gestureEventDecoder.saveToSharedPreferences()
    }

    // get the position in the spinner values array (in strings resource) of event
    private fun getSpinnerStartingPosition(gesture: Gesture): Int {
        val e: MediaEventType = gestureEventDecoder.gestureToEvent(gesture)
        val temp = controls_standard.indexOf(
            when (e) {
                MediaEventType.PLAY -> getString(R.string.mapping_spinner_play)
                MediaEventType.PAUSE -> getString(R.string.mapping_spinner_pause)
                MediaEventType.SKIP_SONG -> getString(R.string.mapping_spinner_next)
                MediaEventType.PREVIOUS_SONG -> getString(R.string.mapping_spinner_previous)
                MediaEventType.RAISE_VOLUME -> getString(R.string.mapping_spinner_volumeUp)
                MediaEventType.LOWER_VOLUME -> getString(R.string.mapping_spinner_volumeDown)
                else -> "NONE"  // this will never occur
        })
        return temp
    }

}
