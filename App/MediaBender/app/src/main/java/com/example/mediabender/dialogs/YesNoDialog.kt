package com.example.mediabender.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.mediabender.R

// dialog with three elements: a text view, a "yes" button and a "no" button
// promptText -> text to set the prompt to
// onYes      -> what happens when "yes" button clicked
// onNo       -> what happens when "no" button clicked
class YesNoDialog(
    private val prompt: String,
    private val onYes: () -> Unit,
    private val onNo: () -> Unit) : DialogFragment() {

    private lateinit var tv_prompt: TextView
    private lateinit var b_yes: Button
    private lateinit var b_no: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_gesture_mapping_back, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        b_yes = view.findViewById(R.id.b_yes)
        b_no = view.findViewById(R.id.b_no)
        tv_prompt = view.findViewById(R.id.tv_prompt)

        tv_prompt.setText(prompt)
        b_yes.setOnClickListener { yesLambda() }
        b_no.setOnClickListener { noLambda() }
    }

    private fun yesLambda() {
        onYes()
        dismiss()
    }
    private fun noLambda() {
        onNo()
        dismiss()
    }
}