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
        Kimber.plant(DebugTree())
        Kimber.plant(CrashReportingTree)

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