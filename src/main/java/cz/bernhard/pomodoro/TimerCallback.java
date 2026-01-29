package cz.bernhard.pomodoro;

public interface TimerCallback {

    void onTick(int minutes, int seconds);

    void onFinish();
}
