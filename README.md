# Kimber
A port of the Android Timber library to Kotlin. All credit goes to [Jake Wharton](https://github.com/JakeWharton) as this has begun as a line for line port of the Java code.

This is a logger with a small, extensible API which provides utility on top of Android's normal Log class.

Behavior is added through Tree instances. You can install an instance by calling Kimber.plant. Installation of Trees should be done as early as possible. The onCreate of your application is the most logical choice.

The included DebugTree implementation will automatically figure out from which class it's being called and use that class name as its tag. Since the tags vary, it works really well when coupled with a log reader like Pidcat.

There are no Tree implementations installed by default because every time you log in production, a kotlin flavored puppy dies.

Usage

Two easy steps:

1. Install any `Tree` instances you want in the `onCreate` of your application class.
2. Call Kimber's static methods everywhere throughout your app.

Check out the sample app in `kimber-sample/` to see it in action.

## Main Differences With [Timber](https://github.com/JakeWharton/timber)
Trees are created as "Objects" not Classes using the standard Kotlin format.
```
object ReleaseTree : Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Handle Log calls here
        }
}
```
When planting new Trees there is no need to create new instances of the tree, you simply call `Kimber.plant(ReleaseTree)`

## Installation
Kimber is currently hosted on jCenter. Add the following to your gradle:
`compile 'com.mvlabs:kimber:0.5'`

Please make sure that you have added jCenter to your list of repositories eg:
```
repositories {
    mavenCentral()
    jcenter()
}
```


## Whats missing currently
- Lint Rules
- Maven Central installation
- Any Kotlin black magic that I dont know about
