package com.example.mediabender.activities

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.example.mediabender.R
import com.example.mediabender.dialogs.YesNoDialog
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.models.PhoneEventType
import com.example.mediabender.service.Gesture
import kotlinx.android.synthetic.main.activity_gesture_mapping.*

class GestureMappingActivity : AppCompatActivity() {

    private lateinit var gestureEventDecoder: GestureEventDecoder
    private lateinit var standardEventsTextView : TextView
    private lateinit var phoneEventsTextView: TextView

    private lateinit var spinner_togglePlaystate: Spinner
    private lateinit var spinner_next: Spinner
    private lateinit var spinner_previous: Spinner
    private lateinit var spinner_volUp: Spinner
    private lateinit var spinner_volDown: Spinner
    private lateinit var spinner_answer: Spinner
    private lateinit var spinner_decline: Spinner
    private lateinit var spinner_phone_volUp: Spinner
    private lateinit var spinner_phone_volDown: Spinner

    private lateinit var b_save_events: Button
    private lateinit var b_default_gestures: Button

    private lateinit var gestureView: View
    private lateinit var togglePlaystateTextView: TextView
    private lateinit var nextTextView: TextView
    private lateinit var previousTextView: TextView
    private lateinit var volUpTextView: TextView
    private lateinit var volDownTextView: TextView
    private lateinit var answerTextView: TextView
    private lateinit var declineTextView: TextView
    private lateinit var phoneVolUpTextView: TextView
    private lateinit var phoneVolDownTextView: TextView

    private var mappingChanged = false
    private var darkThemeChosen = false

    private lateinit var gestures: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_mapping)

        gestureEventDecoder = GestureEventDecoder.getInstance(applicationContext)
        setChosenTheme()
        setUpToolbar()
        setupUI()
        setUpSpinners()
        loadAppropriateTheme()
    }

    override fun onRestart() {
        super.onRestart()
        setChosenTheme()
        if( (gestureView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) loadDarkTheme()
        else loadWhiteTheme()

        loadAppropriateTheme()
    }

    // need to make sure that when someone tries to go back, they are aware their map wasn't saved
    override fun onBackPressed() {
        if (mappingChanged) {
            YesNoDialog(
                "You are about to exit with unsaved changes. Are you sure?",
                { finish() }, // on "yes" press, want to leave without doing anything
                {}          // on "no" press, want to return to the activity
            ).show(supportFragmentManager, "GestureMappingActivity: onBackPressed")
        } else {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadAppropriateTheme()
    }

    private fun setupUI() {

        togglePlaystateTextView = findViewById(R.id.tv_event_togglePlaystate)
        nextTextView = findViewById(R.id.tv_event_next)
        previousTextView = findViewById(R.id.tv_event_previous)
        volUpTextView = findViewById(R.id.tv_event_volUp)
        volDownTextView = findViewById(R.id.tv_event_volDown)
        standardEventsTextView = findViewById(R.id.standardEventsTitleTV)

        phoneVolUpTextView = findViewById(R.id.tv_phone_event_volUp)
        phoneVolDownTextView = findViewById(R.id.tv_phone_event_volDown)
        answerTextView = findViewById(R.id.tv_event_answer)
        declineTextView = findViewById(R.id.tv_event_decline)
        phoneEventsTextView = findViewById(R.id.phoneEventsTitleTV)

        b_save_events = findViewById(R.id.b_save_events)
        b_save_events.setOnClickListener {

            // before saving, need to make sure that user entered mapping is valid
            if (gestureEventDecoder.mapsAreValid()) {
                saveGestures()
                Toast.makeText(applicationContext, "Saved.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Each gesture must have only one associated control.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        b_default_gestures = findViewById(R.id.b_default_gestures)
        b_default_gestures.setOnClickListener {
            with(gestureEventDecoder) {
                editMap(Gesture.UP, MediaEventType.RAISE_VOLUME)
                editMap(Gesture.DOWN, MediaEventType.LOWER_VOLUME)
                editMap(Gesture.RIGHT, MediaEventType.SKIP_SONG)
                editMap(Gesture.LEFT, MediaEventType.PREVIOUS_SONG)
                editMap(Gesture.NEAR, MediaEventType.TOGGLE_PLAYSTATE)

                editMap(Gesture.UP, PhoneEventType.RAISE_VOLUME)
                editMap(Gesture.DOWN, PhoneEventType.LOWER_VOLUME)
                editMap(Gesture.RIGHT, PhoneEventType.ACCEPT_CALL)
                editMap(Gesture.LEFT, PhoneEventType.DECLINE_CALL)
            }
            mappingChanged = true
            refreshSpinners()
        }

        spinner_togglePlaystate = findViewById(R.id.spinner_togglePlaystate)
        spinner_next = findViewById(R.id.spinner_next)
        spinner_previous = findViewById(R.id.spinner_previous)
        spinner_volUp = findViewById(R.id.spinner_volUp)
        spinner_volDown = findViewById(R.id.spinner_volDown)

        spinner_answer = findViewById(R.id.spinner_answer)
        spinner_decline = findViewById(R.id.spinner_decline)
        spinner_phone_volUp = findViewById(R.id.spinner_phone_volUp)
        spinner_phone_volDown = findViewById(R.id.spinner_phone_volDown)

        gestureView = findViewById(R.id.scroll_gestures_constraint)

    }

    private fun setUpSpinners(){
        // creating array of gesture options for spinners
        gestures = arrayOf(
            getString(R.string.mapping_spinner_up),
            getString(R.string.mapping_spinner_down),
            getString(R.string.mapping_spinner_right),
            getString(R.string.mapping_spinner_left),
            getString(R.string.mapping_spinner_far),
            getString(R.string.mapping_spinner_near)
        )

        // creating an array adapter for the spinners
        ArrayAdapter<String>(
            this,
            R.layout.my_spinner_item,
            gestures
        ).also { adapter ->
            // specifying the layout for the list when it appears
            adapter.setDropDownViewResource(R.layout.my_spinner_drop_down)

            // applying the adapter to the spinners
            spinner_togglePlaystate.adapter = adapter
            spinner_next.adapter = adapter
            spinner_previous.adapter = adapter
            spinner_volUp.adapter = adapter
            spinner_volDown.adapter = adapter

            spinner_answer.adapter = adapter
            spinner_decline.adapter = adapter
            spinner_phone_volUp.adapter = adapter
            spinner_phone_volDown.adapter = adapter
        }

        // setting the starting value of the spinner based on user shared preferences
        refreshSpinners()

        // defining then assigning the onItemSelectedListener to all spinners
        var myOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (parent?.getChildAt(0) as TextView).setTextColor( when(darkThemeChosen) {
                    true -> getColor(R.color.colorPrimaryWhite)
                    false -> getColor(R.color.colorPrimaryDark)
                })

                // based on which spinner, fetch the media control chosen
                val event: MediaEventType? = when (parent?.id) {
                    R.id.spinner_togglePlaystate -> MediaEventType.TOGGLE_PLAYSTATE
                    R.id.spinner_next -> MediaEventType.SKIP_SONG
                    R.id.spinner_previous -> MediaEventType.PREVIOUS_SONG
                    R.id.spinner_volUp -> MediaEventType.RAISE_VOLUME
                    R.id.spinner_volDown -> MediaEventType.LOWER_VOLUME
                    else -> null    // this case will only occur if phone gesture
                }

                // based on the spinner, fetch the gesture for the associated spinner
                val gesture: Gesture = when (parent?.getItemAtPosition(position).toString()) {
                    getString(R.string.mapping_spinner_up) -> Gesture.UP
                    getString(R.string.mapping_spinner_down) -> Gesture.DOWN
                    getString(R.string.mapping_spinner_right) -> Gesture.RIGHT
                    getString(R.string.mapping_spinner_left) -> Gesture.LEFT
                    getString(R.string.mapping_spinner_far) -> Gesture.FAR
                    getString(R.string.mapping_spinner_near) -> Gesture.NEAR
                    else -> Gesture.NONE // this case will never happen
                }

                if (event != null) { // media gesture
                    if (gestureEventDecoder.mediaEventToGesture(event) != gesture) { // if no change, don't need to map
                        gestureEventDecoder.editMap(gesture, event)
                        mappingChanged = true
                    }
                } else { // phone gesture
                    // based on which spinner, fetch the phone control chosen
                    val event2: PhoneEventType = when (parent?.id) {
                        R.id.spinner_phone_volDown -> PhoneEventType.LOWER_VOLUME
                        R.id.spinner_phone_volUp -> PhoneEventType.RAISE_VOLUME
                        R.id.spinner_answer -> PhoneEventType.ACCEPT_CALL
                        R.id.spinner_decline -> PhoneEventType.DECLINE_CALL
                        else -> PhoneEventType.NONE    // this case will never happen
                    }
                    if (gestureEventDecoder.phoneEventToGesture(event2) != gesture) { // if no change, don't need to map
                        gestureEventDecoder.editMap(gesture, event2)
                        mappingChanged = true
                    }
                }

            }
        }
        spinner_togglePlaystate.onItemSelectedListener = myOnItemSelectedListener
        spinner_volUp.onItemSelectedListener = myOnItemSelectedListener
        spinner_next.onItemSelectedListener = myOnItemSelectedListener
        spinner_previous.onItemSelectedListener = myOnItemSelectedListener
        spinner_volDown.onItemSelectedListener = myOnItemSelectedListener

        spinner_answer.onItemSelectedListener = myOnItemSelectedListener
        spinner_decline.onItemSelectedListener = myOnItemSelectedListener
        spinner_phone_volUp.onItemSelectedListener = myOnItemSelectedListener
        spinner_phone_volDown.onItemSelectedListener = myOnItemSelectedListener
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (mappingChanged) {
                    YesNoDialog(
                        "You are about to exit with unsaved changes. Are you sure?",
                        { finish() }, // on "yes" press, want to leave without doing anything
                        {}          // on "no" press, want to return to the activity
                    ).show(supportFragmentManager, "GestureMappingActivity: onBackPressed")
                } else {
                    finish()
                }
            }
        }
        return true
    }

    private fun loadAppropriateTheme(){
        val currentMode = gestureView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentMode == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) loadDarkTheme()
        else loadWhiteTheme()
    }

    private fun loadDarkTheme(){
        togglePlaystateTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        standardEventsTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        nextTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        previousTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        volUpTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        volDownTextView.setTextColor(getColor(R.color.colorPrimaryWhite))

        card_events_standard_constraint.setBackgroundColor(getColor(R.color.darkForToolbar))
        gestures_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gestureView.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gestures_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_white)

        answerTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        declineTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        phoneVolUpTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        phoneVolDownTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        phoneEventsTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        card_events_phone_constraint.setBackgroundColor(getColor(R.color.darkForToolbar))

        window.statusBarColor = getColor(R.color.colorPrimaryDark)
    }

    private fun loadWhiteTheme(){
        standardEventsTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        togglePlaystateTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        nextTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        gestureView.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        previousTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        volUpTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        volDownTextView.setTextColor(getColor(R.color.colorPrimaryDark))

        card_events_standard_constraint.setBackgroundColor(getColor(R.color.whiteForStatusBar))
        gestures_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        gestures_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_black)
        window.statusBarColor = getColor(R.color.whiteForStatusBar)

        answerTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        declineTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        phoneVolUpTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        phoneVolDownTextView.setTextColor(getColor(R.color.colorPrimaryDark))

        phoneEventsTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        card_events_phone_constraint.setBackgroundColor(getColor(R.color.whiteForStatusBar))
    }

    // save the gesture map to shared preferences
    private fun saveGestures() {
        gestureEventDecoder.saveToSharedPreferences()
    }

    // get the position in the spinner values array for the passed event
    private fun getSpinnerStartingPosition(event: MediaEventType): Int {
        val g: Gesture = gestureEventDecoder.mediaEventToGesture(event)
        return gestures.indexOf(
            when (g) {
                Gesture.UP -> getString(R.string.mapping_spinner_up)
                Gesture.DOWN -> getString(R.string.mapping_spinner_down)
                Gesture.RIGHT -> getString(R.string.mapping_spinner_right)
                Gesture.LEFT -> getString(R.string.mapping_spinner_left)
                Gesture.FAR -> getString(R.string.mapping_spinner_far)
                Gesture.NEAR -> getString(R.string.mapping_spinner_near)
                else -> "NONE"  // this will never occur
            }
        )
    }
    private fun getSpinnerStartingPosition(event: PhoneEventType): Int {
        val g: Gesture = gestureEventDecoder.phoneEventToGesture(event)
        return gestures.indexOf(
            when (g) {
                Gesture.UP -> getString(R.string.mapping_spinner_up)
                Gesture.DOWN -> getString(R.string.mapping_spinner_down)
                Gesture.RIGHT -> getString(R.string.mapping_spinner_right)
                Gesture.LEFT -> getString(R.string.mapping_spinner_left)
                Gesture.FAR -> getString(R.string.mapping_spinner_far)
                Gesture.NEAR -> getString(R.string.mapping_spinner_near)
                else -> "NONE"  // this will never occur
            }
        )
    }

    // refresh the spinner views with the current gesture map
    private fun refreshSpinners() {
        spinner_togglePlaystate.setSelection(getSpinnerStartingPosition(MediaEventType.TOGGLE_PLAYSTATE))
        spinner_next.setSelection(getSpinnerStartingPosition(MediaEventType.SKIP_SONG))
        spinner_previous.setSelection(getSpinnerStartingPosition(MediaEventType.PREVIOUS_SONG))
        spinner_volUp.setSelection(getSpinnerStartingPosition(MediaEventType.RAISE_VOLUME))
        spinner_volDown.setSelection(getSpinnerStartingPosition(MediaEventType.LOWER_VOLUME))

        spinner_answer.setSelection(getSpinnerStartingPosition(PhoneEventType.ACCEPT_CALL))
        spinner_decline.setSelection(getSpinnerStartingPosition(PhoneEventType.DECLINE_CALL))
        spinner_phone_volUp.setSelection(getSpinnerStartingPosition(PhoneEventType.RAISE_VOLUME))
        spinner_phone_volDown.setSelection(getSpinnerStartingPosition(PhoneEventType.LOWER_VOLUME))
    }

    private fun setUpToolbar(){
        setSupportActionBar(findViewById(R.id.gestures_toolbar))
        supportActionBar?.elevation = 0f
        val actionbar = supportActionBar
        actionbar?.title = ""
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setChosenTheme(){
        val themeHelper = ThemeSharedPreferenceHelper(getSharedPreferences("Theme", Context.MODE_PRIVATE))

        darkThemeChosen = when (themeHelper.getTheme()){
            "Dark" -> true
            else -> false
        }
    }
}
