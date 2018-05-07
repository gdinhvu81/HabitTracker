package com.example.gdinh.picker

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
import com.example.gdinh.myapplication.R
import kotlinx.android.synthetic.main.color_picker.*

/**
 * Created by gdinh on 5/7/2018.
 */

/**
 * Created by gdinh on 4/11/2018.
 */
class ColorPicker : AppCompatActivity(){

    var red : Int = 0
    var green : Int = 0
    var blue: Int = 0
    var buttonColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color_picker)

        //Getting ids of seekBar

        val sb_red: SeekBar = findViewById(R.id.sb_red)
        val sb_green: SeekBar = findViewById(R.id.sb_green)
        val sb_blue: SeekBar = findViewById(R.id.sb_blue)

        // Sets the max for colors
        sb_red.max = 255
        sb_green.max = 255
        sb_blue.max = 255

        // Method to change the red colors when seekbar is changed
        sb_red.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(Seekbar: SeekBar?, progress: Int, p2: Boolean) {
                red = progress
                tv_red.text = progress.toString()
                setButtonColor(red,green,blue)
                hex.setText("#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue))
            }
            override fun onStartTrackingTouch(SeekBar: SeekBar?) {}
            override fun onStopTrackingTouch(SeekBar: SeekBar?) {}
        })

        // Method that changed the green color when seekbar is changed
        sb_green.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(SeekBar: SeekBar?, progress: Int, p2: Boolean) {
                green = progress
                tv_green.text = progress.toString()
                hex.setText("#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue))
                setButtonColor(red,green,blue)
            }
            override fun onStartTrackingTouch(SeekBar: SeekBar?) {}
            override fun onStopTrackingTouch(SeekBar: SeekBar?) {}
        })

        // Method that changes the blue color when seekbar is changed
        sb_blue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(SeekBar: SeekBar?, progress: Int, p2: Boolean) {
                blue = progress
                tv_blue.text = progress.toString()
                hex.setText("#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue))
                setButtonColor(red,green,blue)
            }
            override fun onStartTrackingTouch(SeekBar: SeekBar?) {}
            override fun onStopTrackingTouch(SeekBar: SeekBar?) {}
        })

    }

    //Sets button background color to appropriate color
    private fun setButtonColor(r: Int, g: Int, b: Int ){
        buttonColor = Color.rgb(r,g,b)
        button.setBackgroundColor(buttonColor)
    }

    // Sends color to first intent for button
    fun sendColor(view: View){
        var myIntent = Intent()
        myIntent.putExtra("Color", buttonColor)
        setResult(Activity.RESULT_OK, myIntent)
        finish()
    }
}