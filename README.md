# kotlin-efficiency-cores 🔋
![](https://img.shields.io/badge/stability-prototype-blue) ![](https://img.shields.io/badge/platform-jvm-red) ![](https://img.shields.io/badge/os-macOS-green)

Constrain JVM threads and coroutines to the efficiency cores available on M-series Macs (as well as other supported QoS). Do heavy calculations in Kotlin code without the fans ever coming on! ❄

<p align="center">
<img src="images/ecores.png">
<i>All <a href="https://developer.apple.com/news/?id=vk3m204o">e-cores, no p-cores</a>, thanks to the Background QoS on macOS! (Graphic via <a href="https://github.com/exelban/stats">Stats</a>)</i>
</p>

## Usage

_tbd -- so far, this repo contains my implementations that haven't been packaged up into a nice library yet._

- If you're using Java 22, you can copy-paste the `FFMQosSetter.kt` and `QosSetter.kt` files into your project. Start your program with the `--enable-native-access=ALL-UNNAMED` VM argument.
- If you're targeting pre-Java 22, copy-paste the `JNAQosSetter.kt` and `QosSetter.kt` files into your project, and add `implementation("net.java.dev.jna:jna:5.14.0")` to your build file.
- If you're using coroutines, refer to the `BackgroundQosCoroutineDispatcher.kt`.

### With threads
Call `QosSetter.setQosClass(QosClass.QOS_CLASS_BACKGROUND)` to change the current thread's QoS class. Refer to the [Mac Apps documentation](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/power_efficiency_guidelines_osx/PrioritizeWorkAtTheTaskLevel.html#//apple_ref/doc/uid/TP40013929-CH35-SW5) for a detailed overview of available QoS classes.

### With coroutines
Use the `BackgroundQosCoroutineDispatcher` to start your coroutines or switch to this dispatcher using `withContext`.

`BackgroundQosCoroutineDispatcher` uses a pool of threads that all have the Background QoS class, constraining their execution to the efficiency cores.

## How?

There's a native API for it! It's called `pthread_set_qos_class_self_np`. This is part of the [macOS APIs for energy efficiency](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/power_efficiency_guidelines_osx/PrioritizeWorkAtTheTaskLevel.html#//apple_ref/doc/uid/TP40013929-CH35-SW46).

The API specifies a number of QoS classes (defined in [`qos.h`](https://opensource.apple.com/source/libpthread/libpthread-218.30.1/sys/qos.h.auto.html)) that can be set.

I originally prototyped this library using JNI ([see here](https://github.com/SebastianAigner/ec-jni) if you're curious) just to validate whether calling these APIs worked at all with JVM threads. After that quick prototype phase, I decided to rewrite it using JNA after [Alex](https://github.com/alllex) mentioned to me that [JNI might be slowly phased out](https://openjdk.org/jeps/472). He also reminded me of the new [Foreign Function & Memory API](https://openjdk.org/jeps/454) in Java 22, so I decided to build a version using that way of interop as well.

## About the performance
You're giving up a lot of performance (which is kind of the point). Running `ffmpeg` under background affinity, I observed the following:
- Default QoS: `ffmpeg -i "splash.mov" -vf hflip output.mp4  119.22s user 7.98s system 1056% cpu 12.039 total (speed=8.22x)`
- Background QoS: `taskpolicy -c background ffmpeg -i "splash.mov" -vf hflip output.mp4  476.48s user 21.29s system 282% cpu 2:55.99 total
(speed=0.535x)`

So on my M2 Max, switching to the background QoS was 16x slower. However, in the entire conversion process, the fans didn't come on at all. And that's what this is all about!

## Did you know?

If you just want to run a program on the efficiency cores, you can do so via `taskpolicy -c background mycommand` (thanks, [SO](https://apple.stackexchange.com/questions/419758/how-to-execute-terminal-command-on-energy-efficient-cores-on-m1-chip)!). So, if you ever want to convert a video file or run a Gradle build without your fans coming on, you can use that!
