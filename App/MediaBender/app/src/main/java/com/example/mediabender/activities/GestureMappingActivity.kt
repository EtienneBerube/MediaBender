package com.example.mediabender.activities

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.mediabender.R
import com.example.mediabender.helpers.GestureEventDecoder
import com.example.mediabender.helpers.ThemeSharedPreferenceHelper
import com.example.mediabender.models.MediaEventType
import com.example.mediabender.service.Gesture
import kotlinx.android.synthetic.main.activity_gesture_mapping.*

class GestureMappingActivity : AppCompatActivity() {

    private lateinit var gestureEventDecoder: GestureEventDecoder
    private lateinit var standardEventsTextView : TextView
    private lateinit var spinner_play: Spinner
    private lateinit var spinner_pause: Spinner
    private lateinit var spinner_next: Spinner
    private lateinit var spinner_previous: Spinner
    private lateinit var spinner_volUp: Spinner
    private lateinit var spinner_volDown: Spinner
    private lateinit var b_save_events: Button
    private lateinit var gestureView: View
    private lateinit var playTextView: TextView
    private lateinit var pauseTextView: TextView
    private lateinit var nextTextView: TextView
    private lateinit var previousTextView: TextView
    private lateinit var volUpTextView: TextView
    private lateinit var volDownTextView: TextView
    private lateinit var answerTextView: TextView
    private lateinit var declineTextView: TextView
    private lateinit var phoneEventsTextView: TextView

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
        Toast.makeText(applicationContext, "Changes not saved.", Toast.LENGTH_LONG).show()
        super.onBackPressed()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadAppropriateTheme()
        finish()
        startActivity(intent)
    }

    private fun setupUI() {

        playTextView = findViewById(R.id.tv_event_play)
        pauseTextView = findViewById(R.id.tv_event_pause)
        nextTextView = findViewById(R.id.tv_event_next)
        previousTextView = findViewById(R.id.tv_event_previous)
        volUpTextView = findViewById(R.id.tv_event_volUp)
        volDownTextView = findViewById(R.id.tv_event_volDown)
        standardEventsTextView = findViewById(R.id.standardEventsTitleTV)


        b_save_events = findViewById(R.id.b_save_events)

        b_save_events.setOnClickListener {

            // before saving, need to make sure that user entered mapping is valid
            if (gestureEventDecoder.mapIsValid()) {
                saveGestures()
                Toast.makeText(applicationContext, "Saved.", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Each control must have only one associated gesture.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        spinner_play = findViewById(R.id.spinner_play)
        spinner_pause = findViewById(R.id.spinner_pause)
        spinner_next = findViewById(R.id.spinner_next)
        spinner_previous = findViewById(R.id.spinner_previous)
        spinner_volUp = findViewById(R.id.spinner_volUp)
        spinner_volDown = findViewById(R.id.spinner_volDown)
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
            android.R.layout.simple_spinner_item,
            gestures
        ).also { adapter ->
            // specifying the layout for the list when it appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            // applying the adapter to the spinners
            spinner_play.adapter = adapter
            spinner_pause.adapter = adapter
            spinner_next.adapter = adapter
            spinner_previous.adapter = adapter
            spinner_volUp.adapter = adapter
            spinner_volDown.adapter = adapter
        }

        // setting the starting value of the spinner based on user shared preferences
        spinner_play.setSelection(getSpinnerStartingPosition(MediaEventType.PLAY))
        spinner_pause.setSelection(getSpinnerStartingPosition(MediaEventType.PAUSE))
        spinner_next.setSelection(getSpinnerStartingPosition(MediaEventType.SKIP_SONG))
        spinner_previous.setSelection(getSpinnerStartingPosition(MediaEventType.PREVIOUS_SONG))
        spinner_volUp.setSelection(getSpinnerStartingPosition(MediaEventType.RAISE_VOLUME))
        spinner_volDown.setSelection(getSpinnerStartingPosition(MediaEventType.LOWER_VOLUME))

        // defining then assigning the onItemSelectedListener to all spinners
        var myOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // based on which spinner, fetch the media control chosen
                val event: MediaEventType = when (parent?.id) {
                    R.id.spinner_play -> MediaEventType.PLAY
                    R.id.spinner_pause -> MediaEventType.PAUSE
                    R.id.spinner_next -> MediaEventType.SKIP_SONG
                    R.id.spinner_previous -> MediaEventType.PREVIOUS_SONG
                    R.id.spinner_volUp -> MediaEventType.RAISE_VOLUME
                    R.id.spinner_volDown -> MediaEventType.LOWER_VOLUME
                    else -> MediaEventType.NONE    // this case will never happen
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
                gestureEventDecoder.editGestureMap(gesture, event)
                // TODO might be interesting to add feedback to the user saying "hey, this gesture is for more than one control"
            }
        }
        spinner_play.onItemSelectedListener = myOnItemSelectedListener
        spinner_pause.onItemSelectedListener = myOnItemSelectedListener
        spinner_volUp.onItemSelectedListener = myOnItemSelectedListener
        spinner_next.onItemSelectedListener = myOnItemSelectedListener
        spinner_previous.onItemSelectedListener = myOnItemSelectedListener
        spinner_volDown.onItemSelectedListener = myOnItemSelectedListener
    }

    private fun loadAppropriateTheme(){
        val currentMode = gestureView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentMode == Configuration.UI_MODE_NIGHT_YES || darkThemeChosen) loadDarkTheme()
        else loadWhiteTheme()
    }

    private fun loadDarkTheme(){
        playTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        standardEventsTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        pauseTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        nextTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        previousTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        volUpTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        card_events_standard_constraint.setBackgroundColor(getColor(R.color.darkForToolbar))
        volDownTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        gestures_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gestureView.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gestures_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_white)

        answerTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        declineTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        phoneEventsTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        card_events_phone_constraint.setBackgroundColor(getColor(R.color.darkForToolbar))

        window.statusBarColor = getColor(R.color.colorPrimaryDark)
    }

    private fun loadWhiteTheme(){
        standardEventsTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        playTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        pauseTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        nextTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        gestureView.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        previousTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        card_events_standard_constraint.setBackgroundColor(getColor(R.color.whiteForStatusBar))
        volUpTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        volDownTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        gestures_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        gestures_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_black)
        window.statusBarColor = getColor(R.color.whiteForStatusBar)

        answerTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        declineTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        phoneEventsTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        card_events_phone_constraint.setBackgroundColor(getColor(R.color.whiteForStatusBar))
    }

    // save the gesture map to shared preferences
    private fun saveGestures() {
        gestureEventDecoder.saveToSharedPreferences()
    }

    // get the position in the spinner values array for the passed event
    private fun getSpinnerStartingPosition(event: MediaEventType): Int {
        val g: Gesture = gestureEventDecoder.eventToGesture(event)
        return gestures.indexOf(
            when (g) {
                Gesture.UP -> getString(R.string.mapping_spinner_up)
                Gesture.DOWN -> getString(R.string.mapping_spinner_down)
                Gesture.RIGHT -> getString(R.string.mapping_spinner_left)
                Gesture.LEFT -> getString(R.string.mapping_spinner_right)
                Gesture.FAR -> getString(R.string.mapping_spinner_far)
                Gesture.NEAR -> getString(R.string.mapping_spinner_near)
                else -> "NONE"  // this will never occur
            }
        )
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
