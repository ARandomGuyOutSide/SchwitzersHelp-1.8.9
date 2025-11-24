package com.schwitzer.schwitzersHelp.util;

public class TimerUtil {

    public long startedAt = 0;
    public long pausedAt = 0;
    public boolean paused = false;

    public void reset() {
        startedAt = System.currentTimeMillis();
        pausedAt = 0;
        paused = false;
    }

    public long getElapsedTime() {
        if (startedAt == 0) {
            return 0;
        }
        if (paused) {
            return pausedAt - startedAt;
        }
        return System.currentTimeMillis() - startedAt;
    }

    public TimerUtil() {
        reset();
    }

    public boolean hasPassed(long ms) {
        return System.currentTimeMillis() - startedAt > ms;
    }

    public boolean isScheduled() {
        return startedAt != 0;
    }

    public static int milisecondsToTicks(int milliseconds) {
        return (milliseconds * 20) / 1000;
    }

    public static int secondsToTicks(int seconds) {
        // Minecraft runs at 20 ticks per second
        return seconds * 20;
    }
}