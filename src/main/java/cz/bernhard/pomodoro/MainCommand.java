package cz.bernhard.pomodoro;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(name = "pomodoro", description = "Simple Pomodoro timer")
public class MainCommand implements Runnable {

    public static final String RED_COLOR = "\u001B[31m";
    public static final String RESET_COLOR = "\u001B[0m";

    @Option(names = {"-t", "--time"}, description = "Time in minutes or minutes:seconds (e.g., 4 or 4:30)")
    private String time;

    @Option(names = {"--play-on-finish"}, description = "Path to sound file to play when timer is finished")
    private String soundFilePath;

    @Override
    public void run() {

        TimerCallback callback = new TimerCallback() {
            @Override
            public void onTick(int minutes, int seconds) {
                System.out.printf("\r" + RED_COLOR + "%02d:%02d" + RESET_COLOR, minutes, seconds);
                System.out.flush();
            }

            @Override
            public void onFinish() {
                if (soundFilePath != null) {
                    new SoundPlayer().play(soundFilePath);
                } else {
                    new SoundPlayer().play(getClass().getResourceAsStream("/default-finish-sound.mp3"));
                }
            }
        };

        PomodoroTimer timer;
        if (time != null) {
            String[] parts = time.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
            timer = new PomodoroTimer(minutes, seconds, callback);
        } else {
            timer = new PomodoroTimer(25, 0, callback); // Default 25 minutes
        }
        timer.start();
        timer.join();
    }
}
