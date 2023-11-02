package com.compose.keyboarddemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.blankj.utilcode.util.KeyboardUtils
import com.compose.keyboard.KeyBoardUtil

class MainActivity : AppCompatActivity() {

    val et_body:EditText by lazy { findViewById(R.id.et_body) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KeyBoardUtil(this).apply {
            attachTo(et_body,true, KeyBoardUtil.INPUT_CAR_NUMBER_TYPE)
            setDefaultKeyBoardListener {
                KeyboardUtils.showSoftInput(et_body)
            }
        }
    }
}