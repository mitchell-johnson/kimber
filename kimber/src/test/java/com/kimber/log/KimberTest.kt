package com.kimber.log

import android.util.Log
import android.widget.TextView
import com.mvlabs.log.BuildConfig
import kimber.log.DebugTree
import kimber.log.Kimber
import kimber.log.Tree
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

import org.fest.assertions.api.Assertions.assertThat
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import org.robolectric.shadows.ShadowLog.LogItem
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class KimberTest {

    @Before @After fun setUpAndTearDown() {
        Kimber.uprootAll()
    }

    // NOTE: This class references the line number. Keep it at the top so it does not change.
    @Test fun debugTreeCanAlterCreatedTag() {
        Kimber.plant(object : DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return super.createStackElementTag(element) + ':' + element.getLineNumber()
            }

        })

        Kimber.d("Test")
        var tagPattern = "KimberTest:[0-9]+"
        var messagePattern = "Test"

        assertLog()
                .hasMessageRegex(Log.DEBUG, tagPattern, messagePattern)
                .hasNoMoreMessages()
    }

    @Test fun recursion() {
        val timber = Kimber.asTree()
        try {
            Kimber.plant(timber)
            fail()
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessage("Cannot plant Kimber into itself.")
        }
    }

    @Test fun treeCount() {
        // inserts trees and checks if the amount of returned trees matches.
        assertThat(Kimber.treeCount()).isEqualTo(0)
        var i = 1
        while (i < 50) {
            Kimber.plant(DebugTree())
            assertThat(Kimber.treeCount()).isEqualTo(i)
            i++
        }
        Kimber.uprootAll()
        assertThat(Kimber.treeCount()).isEqualTo(0)
    }

    @Test fun nullTree() {
        try {
            Kimber.plant(null)
            fail()
        } catch (e: NullPointerException) {
            assertThat(e).hasMessage("tree == null")
        }
    }

    @Test fun forestReturnsAllPlanted() {
        var tree1 = DebugTree()
        var tree2 = DebugTree()
        Kimber.plant(tree1)
        Kimber.plant(tree2)
        assertThat(Kimber.forest()).containsExactly(tree1, tree2)
    }

    @Test fun uprootThrowsIfMissing() {
        try {
            Kimber.uproot(DebugTree())
            fail()
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessageStartingWith("Cannot uproot tree which is not planted: ")
        }
    }

    @Test fun uprootRemovesTree() {
        val tree1 = DebugTree()
        val tree2 = DebugTree()
        Kimber.plant(tree1)
        Kimber.plant(tree2)
        Kimber.d("First")
        Kimber.uproot(tree1)
        Kimber.d("Second")

        assertLog()
                .hasDebugMessage("KimberTest", "First")
                .hasDebugMessage("KimberTest", "First")
                .hasDebugMessage("KimberTest", "Second")
                .hasNoMoreMessages()
    }

    @Test fun uprootAllRemovesAll() {
        val tree1 = DebugTree()
        val tree2 = DebugTree()
        Kimber.plant(tree1)
        Kimber.plant(tree2)
        Kimber.d("First")
        Kimber.uprootAll()
        Kimber.d("Second")

        assertLog()
                .hasDebugMessage("KimberTest", "First")
                .hasDebugMessage("KimberTest", "First")
                .hasNoMoreMessages()
    }

    @Test fun noArgsDoesNotFormat() {
        Kimber.plant(DebugTree())
        Kimber.d("te%st")

        assertLog()
                .hasDebugMessage("KimberTest", "te%st")
                .hasNoMoreMessages()
    }

    @Test fun debugTreeTagGeneration() {
        Kimber.plant(DebugTree())
        Kimber.d("Hello, world!")

        assertLog()
                .hasDebugMessage("KimberTest", "Hello, world!")
                .hasNoMoreMessages()
    }

    @Test fun exceptionFromSpawnedThread() {
        Kimber.plant(DebugTree())
        var datThrowable = NullPointerException()
        var latch = CountDownLatch(1)
        thread() {
            Kimber.e(datThrowable, "OMFG!")
            latch.countDown()
        }
        latch.await()
        assertExceptionLogged("OMFG!", "java.lang.NullPointerException", "KimberTest\$exceptionFromSpawnedThread")
    }


//    @Test fun debugTreeTagGenerationStripsAnonymousClassMarker() {
//        Kimber.plant(DebugTree())
//        thread {
//            Kimber.d("Hello, world!")
//
//            thread {
//                Kimber.d("Hello, world!")
//            }
//
//        }
//
//        assertLog()
//                .hasDebugMessage("KimberTest", "Hello, world!")
//                .hasDebugMessage("KimberTest", "Hello, world!")
//                .hasNoMoreMessages()
//    }

    @Test fun debugTreeCustomTag() {
        Kimber.plant(DebugTree())
        Kimber.tag("Custom").d(null, "Hello, world!")

        assertLog()
                .hasDebugMessage("Custom", "Hello, world!")
                .hasNoMoreMessages()
    }

    @Test fun messageWithException() {
        Kimber.plant(DebugTree())
        var datThrowable = NullPointerException()
        Kimber.e(datThrowable, "OMFG!")
        assertExceptionLogged("OMFG!", "java.lang.NullPointerException")
    }

    @Test fun logMessageCallback() {
        var logs = mutableListOf<String>()
        Kimber.plant(object : DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                logs.add("$priority $tag $message")
            }
        })

        Kimber.v("Verbose")
        Kimber.tag("Custom").v(null, "Verbose")
        Kimber.d("Debug")
        Kimber.tag("Custom").d(null, "Debug")
        Kimber.i("Info")
        Kimber.tag("Custom").i(null, "Info")
        Kimber.w("Warn")
        Kimber.tag("Custom").w(null, "Warn")
        Kimber.e("Error")
        Kimber.tag("Custom").e(null, "Error")
        Kimber.wtf("Assert")
        Kimber.tag("Custom").wtf(null, "Assert")

        assertThat(logs).containsExactly(//
                "2 KimberTest Verbose", //
                "2 Custom Verbose", //
                "3 KimberTest Debug", //
                "3 Custom Debug", //
                "4 KimberTest Info", //
                "4 Custom Info", //
                "5 KimberTest Warn", //
                "5 Custom Warn", //
                "6 KimberTest Error", //
                "6 Custom Error", //
                "7 KimberTest Assert", //
                "7 Custom Assert" //
        )
    }

    @Test fun logAtSpecifiedPriority() {
        Kimber.plant(DebugTree())

        Kimber.log(Log.VERBOSE, "Hello, World!")
        Kimber.log(Log.DEBUG, "Hello, World!")
        Kimber.log(Log.INFO, "Hello, World!")
        Kimber.log(Log.WARN, "Hello, World!")
        Kimber.log(Log.ERROR, "Hello, World!")
        Kimber.log(Log.ASSERT, "Hello, World!")

        assertLog()
                .hasVerboseMessage("KimberTest", "Hello, World!")
                .hasDebugMessage("KimberTest", "Hello, World!")
                .hasInfoMessage("KimberTest", "Hello, World!")
                .hasWarnMessage("KimberTest", "Hello, World!")
                .hasErrorMessage("KimberTest", "Hello, World!")
                .hasAssertMessage("KimberTest", "Hello, World!")
                .hasNoMoreMessages()
    }

    @Test fun formatting() {
        Kimber.plant(DebugTree())
        Kimber.v("Hello, %s!", "World")
        Kimber.d("Hello, %s!", "World")
        Kimber.i("Hello, %s!", "World")
        Kimber.w("Hello, %s!", "World")
        Kimber.e("Hello, %s!", "World")
        Kimber.wtf("Hello, %s!", "World")

        assertLog()
                .hasVerboseMessage("KimberTest", "Hello, World!")
                .hasDebugMessage("KimberTest", "Hello, World!")
                .hasInfoMessage("KimberTest", "Hello, World!")
                .hasWarnMessage("KimberTest", "Hello, World!")
                .hasErrorMessage("KimberTest", "Hello, World!")
                .hasAssertMessage("KimberTest", "Hello, World!")
                .hasNoMoreMessages()
    }

    @Test fun isLoggableControlsLogging() {
        Kimber.plant(object : DebugTree() {
            override fun isLoggable(priority: Int): Boolean {
                return priority == Log.INFO
            }
        })
        Kimber.v("Hello, World!")
        Kimber.d("Hello, World!")
        Kimber.i("Hello, World!")
        Kimber.w("Hello, World!")
        Kimber.e("Hello, World!")
        Kimber.wtf("Hello, World!")

        assertLog()
                .hasInfoMessage("KimberTest", "Hello, World!")
                .hasNoMoreMessages()
    }

    @Test fun logsUnknownHostExceptions() {
        Kimber.plant(DebugTree())
        Kimber.e(UnknownHostException(), "")
        assertExceptionLogged("", "UnknownHostException")
    }

    //Utility functions for checking log output calls

    fun assertExceptionLogged(message: String, exceptionClassname: String, tag: String? = null) {
        var logs: List<LogItem> = ShadowLog.getLogs()
        //if the log we posted here was longer than the max log length then this will fail. So we want to re-combine
        //all logs into one.
        var testLogs: String = "";
        for(item in logs) {
            testLogs = testLogs.plus(item.msg)
        }
//        assertThat(logs).hasSize(1)
        var log = logs[0]
        assertThat(log.type).isEqualTo(Log.ERROR)
        var tagString: String = tag ?: "KimberTest"
        assertThat(log.tag).isEqualTo(tagString)
        assertThat(testLogs).startsWith(message)
        assertThat(testLogs).contains(exceptionClassname)
        // We use a low-level primitive that Robolectric doesn't populate.
        assertThat(log.throwable).isNull()
    }

    fun assertLog(): LogAssert {
        return LogAssert(ShadowLog.getLogs())
    }

    class LogAssert(logItems: MutableList<LogItem>) {
        val items: MutableList<LogItem> = logItems
        var index = 0

        fun hasVerboseMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.VERBOSE, tag, message)
        }

        fun hasDebugMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.DEBUG, tag, message)
        }

        fun hasInfoMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.INFO, tag, message)
        }

        fun hasWarnMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.WARN, tag, message)
        }

        fun hasErrorMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.ERROR, tag, message)
        }

        fun hasAssertMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.ASSERT, tag, message)
        }

        fun hasMessage(priority: Int, tag: String, message: String): LogAssert {
            val item = items[index++]
            assertThat(item.type).isEqualTo(priority)
            assertThat(item.tag).isEqualTo(tag)
            assertThat(item.msg).isEqualTo(message)
            return this
        }

        fun hasMessageRegex(priority: Int, tagRegex: String, messageRegex: String): LogAssert {
            val item = items.get(index++)
            assertThat(item.type).isEqualTo(priority)
            assert(item.tag.matches(tagRegex.toRegex()))
            assert(item.msg.matches(messageRegex.toRegex()))
            return this
        }

        fun hasNoMoreMessages() {
            assertThat(items).hasSize(index)
        }
    }
}