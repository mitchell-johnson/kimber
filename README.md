# kimber
A port of the Android Timber library to Kotlin. All credit goes to [Jake Wharton](https://github.com/JakeWharton) as this has begun as a line for line port of the Java code.

This is a logger with a small, extensible API which provides utility on top of Android's normal Log class.

Behavior is added through Tree instances. You can install an instance by calling Kimber.plant. Installation of Trees should be done as early as possible. The onCreate of your application is the most logical choice.

The included DebugTree implementation will automatically figure out from which class it's being called and use that class name as its tag. Since the tags vary, it works really well when coupled with a log reader like Pidcat.

There are no Tree implementations installed by default because every time you log in production, a puppy dies.

Usage

Two easy steps:

Install any Tree instances you want in the onCreate of your application class.
Call Kimber's static methods everywhere throughout your app.
Check out the sample app in kimber-sample/ to see it in action.


## Whats missing currently
- Unit Tests
- Lint Rules
- Gradle installation 
