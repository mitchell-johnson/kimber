package kimber.log

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

/** Logging for lazy people who like kotlin. */
object Kimber {

    /** Add a new logging tree.  */
    fun plant(tree: Tree?) {
        // Both fields guarded by 'FOREST'.

        if (tree == null) {
            throw NullPointerException("tree == null")
        }
        if (tree === TREE_OF_SOULS) {
            throw IllegalArgumentException("Cannot plant Kimber into itself.")
        }
        synchronized (FOREST) {
            FOREST.add(tree)
        }
    }

    /** Log a verbose message with optional throwable */
    fun v(message: String, t: Throwable? = null) {
        TREE_OF_SOULS.v(t, message)
    }

    /** Log a debug message with optional throwable */
    fun d(message: String, t: Throwable? = null) {
        TREE_OF_SOULS.d(t, message)
    }

    /** Log a informational message with optional throwable */
    fun i(message: String, t: Throwable? = null) {
        TREE_OF_SOULS.i(t, message)
    }

    /** Log a warning message with optional throwable  */
    fun w(message: String, t: Throwable? = null) {
        TREE_OF_SOULS.w(t, message)
    }

    /** Log a error message with optional throwable */
    fun e(message: String, t: Throwable? = null) {
        TREE_OF_SOULS.e(t, message)
    }


    /** Log a assert wtf message with optional throwable */
    fun wtf(message: String, t: Throwable? = null) {
        TREE_OF_SOULS.wtf(t, message)
    }
}

val FOREST: MutableList<Tree> = mutableListOf()

/** A {@link Tree} that delegates to all planted trees in the {@linkplain #FOREST forest}. */
object TREE_OF_SOULS : Tree() {

    override fun v(t: Throwable?, message: String) {
        for (tree in FOREST) {
            tree.v(t ,message)
        }
    }

    override fun d(t: Throwable?, message: String) {
        for (tree in FOREST) {
            tree.d(t ,message)
        }
    }

    override fun i(t: Throwable?, message: String) {
        for (tree in FOREST) {
            tree.i(t ,message)
        }
    }

    override fun w(t: Throwable?, message: String) {
        for (tree in FOREST) {
            tree.w(t ,message)
        }
    }

    override fun e(t: Throwable?, message: String) {
        for (tree in FOREST) {
            tree.e(t ,message)
        }
    }

    override fun wtf(t: Throwable?, message: String) {
        for (tree in FOREST) {
            tree.wtf(t ,message)
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        for (tree in FOREST) {
            tree.log(priority, tag, message, t)
        }
    }
}


/** A facade for handling logging calls. Install instances via {@link #plant Timber.plant()}. */
abstract class Tree {
    internal val explicitTag = ThreadLocal<String>()

    /**
     * Returns the current one time tag for this tree.
     * @return the one time tag currently set for this tree. Will return null if no tag
     * has been set. This null should be consumed by implementing child trees
     */
    open fun getTag(): String? {
        val tag: String? = explicitTag.get();
        //ThreadLocal.remove checks for null for us.
        explicitTag.remove()
        return tag
    }

    /** Log an optional verbose  */
    open fun v( t: Throwable? = null, message: String) {
        prepareLog(Log.VERBOSE, t, message)
    }

    /** Log an optional debug  */
    open fun d( t: Throwable? = null, message: String) {
        prepareLog(Log.DEBUG, t, message)
    }

    /** Log an optional info  */
    open fun i( t: Throwable? = null, message: String) {
        prepareLog(Log.INFO, t, message)
    }

    /** Log an optional warning  */
    open fun w( t: Throwable? = null, message: String) {
        prepareLog(Log.WARN, t, message)
    }

    /** Log an optional error  */
    open fun e( t: Throwable? = null, message: String) {
        prepareLog(Log.ERROR, t, message)
    }

    /** Log an optional assert  */
    open fun wtf( t: Throwable? = null, message: String) {
        prepareLog(Log.ASSERT, t, message)
    }

    /** Log at `priority` an  */
    fun log(priority: Int, t: Throwable, message: String) {
        prepareLog(priority, t, message)
    }

    /**
     * Write a log message to its destination. Called for all level-specific methods by default.

     * @param priority Log level. See [Log] for constants.
     * *
     * @param tag Explicit or inferred tag. May be `null`.
     * *
     * @param message Formatted log message. May be `null`, but then `t` will not be.
     * *
     * @param t Accompanying exceptions. May be `null`, but then `message` will not be.
     */
    abstract fun log(priority: Int, tag: String?, message: String, t: Throwable?)

    /** Return whether a message at `priority` should be logged.  */
    protected fun isLoggable(priority: Int): Boolean {
        return true
    }

    fun prepareLog(priority: Int, t: Throwable?, message: String) {
        var outputMessage:String = message;
        if (!isLoggable(priority)) {
            return
        }

        if (outputMessage.length == 0) {
            if (t == null) {
                return  // Swallow message if it's empty and there's no throwable.
            }
            outputMessage = getStackTraceString(t)
        } else {
            if (t != null) {
                outputMessage += "\n" + getStackTraceString(t)
            }
        }

        log(priority, getTag(), outputMessage, t)
    }

    private fun getStackTraceString(t: Throwable): String {
        val sw = StringWriter(256)
        val pw = PrintWriter(sw, false)
        t.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}

/** A {@link Tree Tree} for debug builds. Automatically infers the tag from the calling class. */
object DebugTree : Tree() {
    private val MAX_LOG_LENGTH = 4000
    private val CALL_STACK_INDEX = 5
    private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (message.length < MAX_LOG_LENGTH) {
            if (priority == Log.ASSERT) {
                Log.wtf(tag, message)
            } else {
                Log.println(priority, tag, message)
            }
            return
        }

        // Split by line, then ensure each line can fit into Log's maximum length.
        var i = 0
        val length = message.length
        while (i < length) {
            var newline = message.indexOf('\n', i)
            newline = if (newline != -1) newline else length
            do {
                val end = Math.min(newline, i + MAX_LOG_LENGTH)
                val part = message.substring(i, end)
                if (priority == Log.ASSERT) {
                    Log.wtf(tag, part)
                } else {
                    Log.println(priority, tag, part)
                }
                i = end
            } while (i < newline)
            i++
        }
    }


    override fun getTag(): String {
        val tag = super.getTag()
        if (tag != null) {
            return tag
        }
        // DO NOT switch this to Thread.getCurrentThread().getStackTrace(). The test will pass
        // because Robolectric runs them on the JVM but on Android the elements are different.
        val stackTrace = Throwable().stackTrace
        if (stackTrace.size <= CALL_STACK_INDEX) {
            throw IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?")
        }
        return createStackElementTag(stackTrace[CALL_STACK_INDEX])
    }

    /**
     * Extract the tag which should be used for the message from the `element`. By default
     * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     *
     * Note: This will not be called if a [manual tag][.tag] was specified.
     */
    fun createStackElementTag(element: StackTraceElement): String {
        var tag = element.className
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }
}