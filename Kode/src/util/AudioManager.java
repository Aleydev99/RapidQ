package util;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AudioManager {
    
    private static AudioManager instance;
    private Clip bgmClip;
    private FloatControl bgmVolumeControl;
    private float bgmVolume = 0.25f;
    private float sfxVolume = 0.25f;
    private boolean isBgmPlaying = false;
    
    private Thread bgmThread;
    private volatile boolean shouldStopBgm = false;
    
    private AudioManager() {}
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    public void playBGM(String filePath) {
        stopBGM();
        
        shouldStopBgm = false;
        
        bgmThread = new Thread(() -> {
            try {
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    System.err.println("Audio file not found: " + filePath);
                    return;
                }
                
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioStream);
                
                if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    bgmVolumeControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                    setBGMVolume(bgmVolume);
                }
                
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
                isBgmPlaying = true;
                
                System.out.println("BGM started in dedicated thread: " + filePath);
                
                while (!shouldStopBgm && bgmClip != null && bgmClip.isRunning()) {
                    Thread.sleep(100);
                }
                
            } catch (UnsupportedAudioFileException e) {
                System.err.println("Unsupported audio format: " + filePath);
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Error reading audio file: " + filePath);
                e.printStackTrace();
            } catch (LineUnavailableException e) {
                System.err.println("Audio line unavailable");
                e.printStackTrace();
            } catch (InterruptedException e) {
                System.out.println("BGM thread interrupted");
            }
        }, "BGM-Thread");
        
        bgmThread.start();
    }

    public void stopBGM() {
        shouldStopBgm = true;
        
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            isBgmPlaying = false;
            System.out.println("BGM stopped");
        }
        
        if (bgmThread != null && bgmThread.isAlive()) {
            try {
                bgmThread.join(500);
            } catch (InterruptedException e) {
                bgmThread.interrupt();
            }
        }
    }

    public void pauseBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            isBgmPlaying = false;
        }
    }

    public void resumeBGM() {
        if (bgmClip != null && !bgmClip.isRunning()) {
            bgmClip.start();
            isBgmPlaying = true;
        }
    }

    public void setBGMVolume(float volume) {
        this.bgmVolume = Math.max(0.0f, Math.min(1.0f, volume));
        
        if (bgmVolumeControl != null) {
            float gain = convertLinearToDecibel(this.bgmVolume, bgmVolumeControl);
            bgmVolumeControl.setValue(gain);
        }
    }
    
    public void setBGMVolumeFromSlider(int sliderValue) {
        float volume = sliderValue / 100.0f;
        setBGMVolume(volume);
    }

    public float getBGMVolume() {
        return bgmVolume;
    }
    
    public int getBGMVolumeSliderValue() {
        return (int) (bgmVolume * 100);
    }

    public void playSFX(String filePath) {
        Thread sfxThread = new Thread(() -> {
            try {
                File audioFile = new File(filePath);
                if (!audioFile.exists()) {
                    System.err.println("SFX file not found: " + filePath);
                    return;
                }
                
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                Clip sfxClip = AudioSystem.getClip();
                sfxClip.open(audioStream);
                
                if (sfxClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volumeControl = (FloatControl) sfxClip.getControl(FloatControl.Type.MASTER_GAIN);
                    float gain = convertLinearToDecibel(sfxVolume, volumeControl);
                    volumeControl.setValue(gain);
                }
                
                sfxClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        sfxClip.close();
                    }
                });
                
                sfxClip.start();
                
                while (sfxClip.isRunning()) {
                    Thread.sleep(10);
                }
                
            } catch (Exception e) {
                System.err.println("Error playing SFX: " + filePath);
                e.printStackTrace();
            }
        }, "SFX-Thread");
        
        sfxThread.start();
    }

    public void setSFXVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    public void setSFXVolumeFromSlider(int sliderValue) {
        float volume = sliderValue / 100.0f;
        setSFXVolume(volume);
    }

    public float getSFXVolume() {
        return sfxVolume;
    }

    public int getSFXVolumeSliderValue() {
        return (int) (sfxVolume * 100);
    }

    public boolean isBgmPlaying() {
        return isBgmPlaying && bgmClip != null && bgmClip.isRunning();
    }

    public void cleanup() {
        shouldStopBgm = true;
        
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
        }
        
        if (bgmThread != null && bgmThread.isAlive()) {
            bgmThread.interrupt();
        }
    }
    
    private float convertLinearToDecibel(float volume, FloatControl control) {
        if (control == null) {
            return 0f;
        }
        if (volume <= 0.0001f) {
            return control.getMinimum();
        }
        float gain = (float) (20.0 * Math.log10(volume));
        return Math.max(control.getMinimum(), Math.min(gain, control.getMaximum()));
    }
}
