package com.example.kimber

import android.app.Application
import android.util.Log
import kimber.log.DebugTree
import kimber.log.Kimber
import kimber.log.Tree

class ExampleApp : Application {
    constructor() : super()

    override fun onCreate() {
        super.onCreate()
        Kimber.plant(DebugTree)
        Kimber.plant(CrashReportingTree)


//        Kimber.v(Throwable(), "Throwable test")

//        Kimber.d("Debug message test");
//        Kimber.d(Throwable(), "Throwable test")
//
//        Kimber.i("Info message test");
//        Kimber.i(Throwable(), "Throwable test")
//
//        Kimber.w("Warning message test");
//        Kimber.w(Throwable(), "Throwable test")
//
//        Kimber.e("Error message test");
//        Kimber.e(Throwable(), "Throwable test")
//
//        Kimber.wtf("Assert message test");
//        Kimber.wtf(Throwable(), "Throwable test")

    }

    object CrashReportingTree : Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            FakeCrashLibrary.log(priority, tag, message)

            if (t != null) {
                if (priority == Log.ERROR) {
                    FakeCrashLibrary.logError(t)
                } else if (priority == Log.WARN) {
                    FakeCrashLibrary.logWarning(t)
                }
            }
        }
    }
}