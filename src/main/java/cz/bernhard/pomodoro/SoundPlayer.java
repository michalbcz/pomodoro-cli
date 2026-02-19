package cz.bernhard.pomodoro;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class SoundPlayer {

    static {
        if (System.getProperty("java.home") == null) {
            System.setProperty("java.home", ".");
        }
    }

    public void play(String filePath) {
        // Try Java Sound API first
        try (FileInputStream fis = new FileInputStream(filePath)) {
            if (playWithJavaSound(fis)) {
                return;
            }
        } catch (Exception e) {
            // Fall through to system command
        }
        // Fall back to system command (works in native-image)
        if (!playWithSystemCommand(new File(filePath))) {
            System.err.println("Error playing sound file: no audio playback method available");
        }
    }

    public void play(InputStream stream) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("pomodoro-sound-", ".mp3");
            Files.copy(stream, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Try Java Sound API first
            try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
                if (playWithJavaSound(fis)) {
                    return;
                }
            } catch (Exception e) {
                // Fall through to system command
            }
            // Fall back to system command (works in native-image)
            if (!playWithSystemCommand(tempFile.toFile())) {
                System.err.println("Error playing sound: no audio playback method available");
            }
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {}
            }
        }
    }

    private boolean playWithSystemCommand(File file) {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] command;
        
        if (os.contains("mac")) {
            command = new String[]{"afplay", file.getAbsolutePath()};
        } else if (os.contains("linux")) {
            // Try common Linux audio players
            if (commandExists("aplay")) {
                command = new String[]{"aplay", file.getAbsolutePath()};
            } else if (commandExists("paplay")) {
                command = new String[]{"paplay", file.getAbsolutePath()};
            } else if (commandExists("mpg123")) {
                command = new String[]{"mpg123", "-q", file.getAbsolutePath()};
            } else if (commandExists("ffplay")) {
                command = new String[]{"ffplay", "-nodisp", "-autoexit", "-loglevel", "quiet", file.getAbsolutePath()};
            } else {
                return false;
            }
        } else if (os.contains("windows")) {
            // Windows: use PowerShell to play sound
            command = new String[]{"powershell", "-c", 
                "(New-Object Media.SoundPlayer '" + file.getAbsolutePath() + "').PlaySync()"};
        } else {
            return false;
        }
        
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder("which", command)
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean playWithJavaSound(InputStream stream) {
        try {
            InputStream bufferedIn = new BufferedInputStream(stream);
            AudioInputStream in = AudioSystem.getAudioInputStream(bufferedIn);
            AudioFormat baseFormat = in.getFormat();
            float sampleRate = baseFormat.getSampleRate();
            if (sampleRate <= 0 || sampleRate == AudioSystem.NOT_SPECIFIED) {
                sampleRate = 44100.0f;
            }
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    sampleRate,
                    false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
            DataLine.Info info = new DataLine.Info(Clip.class, din.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);

            // Use a LineListener to wait for playback to complete
            final Object lock = new Object();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            });

            clip.open(din);
            clip.start();

            // Wait for the clip to finish playing
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            clip.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

