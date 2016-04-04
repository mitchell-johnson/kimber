package com.example.kimber

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kimber.log.DebugTree
import kimber.log.Kimber
import kimber.log.Tree
import java.util.regex.Pattern


class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_activity)
        var testString:String = "Some additional text"
        var testInt:Int = 4;

        Kimber.v("Verbose message test %s", testString);
//        Kimber.v("Verbose message test 2 %d", testInt);
        Kimber.v("Verbose message test 3 = " + testString);
    }
}