package model;

import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameAudio {
    private Clip bgmClip;
    private long bgmPosition = 0;
    private final List<Clip> effectClips = new ArrayList<>();

    public void playBGM(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            bgmPosition = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
            bgmPosition = 0;
        }
    }

    public void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmPosition = bgmClip.getMicrosecondPosition();
            bgmClip.stop();
        }
    }

    public void resumeBGM() {
        if (bgmClip != null) {
            bgmClip.setMicrosecondPosition(bgmPosition);
            bgmClip.start();
        }
    }

    public void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) return;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            effectClips.add(clip);
            // Remove from list when done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    effectClips.remove(clip);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Stop all sounds (BGM + all effects)
    public void stopAllSounds() {
        stopBGM();
        for (Clip clip : new ArrayList<>(effectClips)) {
            if (clip.isRunning()) clip.stop();
            clip.close();
        }
        effectClips.clear();
    }
}