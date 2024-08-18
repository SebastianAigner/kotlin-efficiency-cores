# kotlin-efficiency-cores ⚡️
Constrain JVM threads and coroutines to the efficiency cores available on M-series Macs (as well as other supported QoS). Do heavy calculations in Kotlin code without the fans ever coming on! ❄

<p align="center">
<img src="images/ecores.png">
<i>All <a href="https://developer.apple.com/news/?id=vk3m204o">e-cores, no p-cores</a>, thanks to the Background QoS on macOS! (Graphic via <a href="https://github.com/exelban/stats">Stats</a>)</i>
</p>

## Usage

_tbd -- so far, this repo contains my implementations that haven't been packaged up into a nice library yet._

- If you're using Java 22, you can copy-paste the `FFMQosSetter.kt` and `QosSetter.kt` files into your project. Start your program with the `--enable-native-access=ALL-UNNAMED` VM argument.
- If you're targeting pre-Java 22, copy-paste the `JNAQosSetter.kt` and `QosSetter.kt` files into your project.

Call `setQosClass(QosClass.QOS_CLASS_BACKGROUND)` to change the current thread's QoS class.

## How?

There's a native API for it! It's called `pthread_set_qos_class_self_np`. This is part of the [macOS APIs for energy efficiency](https://developer.apple.com/library/archive/documentation/Performance/Conceptual/power_efficiency_guidelines_osx/PrioritizeWorkAtTheTaskLevel.html#//apple_ref/doc/uid/TP40013929-CH35-SW46).

The API specifies a number of QoS classes (defined in [`qos.h`](https://opensource.apple.com/source/libpthread/libpthread-218.30.1/sys/qos.h.auto.html)) that can be set.

## About the performance
You're giving up a lot of performance (which is kind of the point). Running `ffmpeg` under background affinity, I observed the following:
- Default QoS: `ffmpeg -i "skip-splash.mov" -vf hflip output.mp4  119.22s user 7.98s system 1056% cpu 12.039 total (speed=8.22x)`
- Background QoS: `taskpolicy -c background ffmpeg -i "skip-splash.mov" -vf hflip output.mp4  476.48s user 21.29s system 282% cpu 2:55.99 total
(speed=0.535x)`

So on my M2 Max, switching to the background QoS was 16x slower. However, in the entire conversion process, the fans didn't come on at all. And that's what this is all about!

## Did you know?

If you just want to run a program on the efficiency cores, you can do so via `taskpolicy -c background mycommand` (thanks, [SO](https://apple.stackexchange.com/questions/419758/how-to-execute-terminal-command-on-energy-efficient-cores-on-m1-chip)!).