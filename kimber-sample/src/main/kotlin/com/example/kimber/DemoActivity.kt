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

        Kimber.v("Verbose message test ${testString}");
        Kimber.v("Verbose message test 3 = " + testString, Throwable("Some Throwable message"));

        Kimber.d("Debug message test with number %d", 2.0);
        Kimber.d("Debug Throwable test", Throwable())

        Kimber.i("Info message test with String %s", testString);
        Kimber.i("Info Throwable test", Throwable())

        Kimber.w("Warning message test");
        Kimber.w("Warning Throwable test", Throwable())

        Kimber.e("Error message test");
        Kimber.e("Error Throwable test", Throwable())

        Kimber.wtf("Assert message test");
        Kimber.wtf("Assert Throwable test", Throwable())
    }
}