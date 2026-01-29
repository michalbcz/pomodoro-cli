package cz.bernhard.pomodoro;

public class PomodoroTimer {

    private final int seconds;
    private final int minutes;
    private final TimerCallback callback;
    private Thread timer;

    public PomodoroTimer(int minutes, int seconds, TimerCallback callback) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.callback = callback;
    }

    public void start() {
        this.timer = new Thread(() -> {
            int totalSeconds = minutes * 60 + seconds;
            try {
                while (totalSeconds >= 0) {
                    int currentMinutes = totalSeconds / 60;
                    int currentSeconds = totalSeconds % 60;
                    callback.onTick(currentMinutes, currentSeconds);
                    Thread.sleep(1000);
                    totalSeconds--;
                }
                callback.onFinish();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timer.start();
    }

    public void stop() {
        if (timer != null && timer.isAlive()) {
            timer.interrupt();
        }
    }

    public void join() {
        try {
            timer.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
