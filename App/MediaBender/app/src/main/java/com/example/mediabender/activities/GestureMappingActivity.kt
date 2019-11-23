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
import com.example.mediabender.service.Gesture
import kotlinx.android.synthetic.main.activity_gesture_mapping.*

class GestureMappingActivity : AppCompatActivity() {

    private lateinit var gestureEventDecoder: GestureEventDecoder
    private lateinit var titleLabel : TextView
    private lateinit var spinner_up: Spinner
    private lateinit var spinner_down: Spinner
    private lateinit var spinner_left: Spinner
    private lateinit var spinner_right: Spinner
    private lateinit var spinner_far: Spinner
    private lateinit var spinner_near: Spinner
    private lateinit var b_save_gestures: Button
    private lateinit var b_default_gestures: Button
    private lateinit var gestureView: View
    private lateinit var upTextView: TextView
    private lateinit var downTextView: TextView
    private lateinit var leftTextView: TextView
    private lateinit var rightTextView: TextView
    private lateinit var farTextView: TextView
    private lateinit var nearTextView: TextView
    private var darkThemeChosen = false
    private lateinit var controls_standard: Array<String>

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
        YesNoDialog(
            "You are about to exit with unsaved changes. Are you sure?",
            {finish()}, // on "yes" press, want to leave without doing anything
            {}          // on "no" press, want to return to the activity
        ).show(supportFragmentManager,"GestureMappingActivity: onBackPressed")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        loadAppropriateTheme()
        finish()
        startActivity(intent)
    }

    private fun setupUI() {

        upTextView = findViewById(R.id.tv_gesture_up)
        downTextView = findViewById(R.id.tv_gesture_down)
        leftTextView = findViewById(R.id.tv_gesture_left)
        rightTextView = findViewById(R.id.tv_gesture_right)
        farTextView = findViewById(R.id.tv_gesture_far)
        nearTextView = findViewById(R.id.tv_gesture_near)
        titleLabel = findViewById(R.id.gesturesTitleTV)

        b_save_gestures = findViewById(R.id.b_save_gestures)
        b_default_gestures = findViewById(R.id.b_default_gestures)

        b_save_gestures.setOnClickListener {

            // before saving, need to make sure that the mappings are unique
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

        b_default_gestures.setOnClickListener {
            with(gestureEventDecoder) {
                editGestureMap(Gesture.UP,MediaEventType.RAISE_VOLUME)
                editGestureMap(Gesture.DOWN,MediaEventType.LOWER_VOLUME)
                editGestureMap(Gesture.RIGHT,MediaEventType.SKIP_SONG)
                editGestureMap(Gesture.LEFT,MediaEventType.PREVIOUS_SONG)
                editGestureMap(Gesture.FAR,MediaEventType.PAUSE)
                editGestureMap(Gesture.NEAR,MediaEventType.PLAY)
            }
            refreshSpinners()
        }

        spinner_up = findViewById(R.id.spinner_up)
        spinner_down = findViewById(R.id.spinner_down)
        spinner_left = findViewById(R.id.spinner_left)
        spinner_right = findViewById(R.id.spinner_right)
        spinner_far = findViewById(R.id.spinner_far)
        spinner_near = findViewById(R.id.spinner_near)
        gestureView = findViewById(R.id.scroll_gestures_constraint)

    }

    private fun setUpSpinners(){
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
        spinner_up.onItemSelectedListener = myOnItemSelectedListener
        spinner_down.onItemSelectedListener = myOnItemSelectedListener
        spinner_far.onItemSelectedListener = myOnItemSelectedListener
        spinner_left.onItemSelectedListener = myOnItemSelectedListener
        spinner_right.onItemSelectedListener = myOnItemSelectedListener
        spinner_near.onItemSelectedListener = myOnItemSelectedListener
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                YesNoDialog(
                    "You are about to exit with unsaved changes. Are you sure?",
                    {finish()}, // on "yes" press, want to leave without doing anything
                    {}          // on "no" press, want to return to the activity
                ).show(supportFragmentManager,"GestureMappingActivity: onBackPressed")
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
        upTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        titleLabel.setTextColor(getColor(R.color.colorPrimaryWhite))
        downTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        leftTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        rightTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        farTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        card_gestures_standard_constraint.setBackgroundColor(getColor(R.color.darkForToolbar))
        nearTextView.setTextColor(getColor(R.color.colorPrimaryWhite))
        gestures_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gestureView.setBackgroundColor(getColor(R.color.colorPrimaryDark))
        gestures_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_white)

        window.statusBarColor = getColor(R.color.colorPrimaryDark)
    }

    private fun loadWhiteTheme(){
        titleLabel.setTextColor(getColor(R.color.colorPrimaryDark))
        upTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        downTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        leftTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        gestureView.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        rightTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        card_gestures_standard_constraint.setBackgroundColor(getColor(R.color.whiteForStatusBar))
        farTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        nearTextView.setTextColor(getColor(R.color.colorPrimaryDark))
        gestures_toolbar.setBackgroundColor(getColor(R.color.colorPrimaryWhite))
        gestures_toolbar.navigationIcon = getDrawable(R.drawable.arrow_back_black)
        window.statusBarColor = getColor(R.color.whiteForStatusBar)
    }
    // save the gesture map to shared preferences
    private fun saveGestures() {
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
            }
        )
        return temp
    }

    // refresh the spinner views with the current gesture map
    private fun refreshSpinners() {
        spinner_up.setSelection(getSpinnerStartingPosition(Gesture.UP))
        spinner_down.setSelection(getSpinnerStartingPosition(Gesture.DOWN))
        spinner_left.setSelection(getSpinnerStartingPosition(Gesture.LEFT))
        spinner_right.setSelection(getSpinnerStartingPosition(Gesture.RIGHT))
        spinner_far.setSelection(getSpinnerStartingPosition(Gesture.FAR))
        spinner_near.setSelection(getSpinnerStartingPosition(Gesture.NEAR))
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
        val  chosenTheme = themeHelper.getTheme()

        when (chosenTheme){
            "Dark" -> darkThemeChosen = true
            else -> darkThemeChosen = false
        }
    }
}
