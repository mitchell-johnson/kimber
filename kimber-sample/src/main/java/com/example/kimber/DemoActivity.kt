package com.example.kimber

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kimber.log.DebugTree
import kimber.log.Kimber


class DemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_activity)
        Kimber.plant(DebugTree)
        Kimber.v( "Verbose message test" );
        Kimber.v( Throwable(), "Throwable test")

        Kimber.d( "Debug message test" );
        Kimber.d( Throwable(), "Throwable test")

        Kimber.i( "Info message test" );
        Kimber.i( Throwable(), "Throwable test")

        Kimber.w( "Warning message test" );
        Kimber.w( Throwable(), "Throwable test")

        Kimber.e( "Error message test" );
        Kimber.e( Throwable(), "Throwable test")

        Kimber.wtf( "Assert message test" );
        Kimber.wtf( Throwable(), "Throwable test")

    }
}
