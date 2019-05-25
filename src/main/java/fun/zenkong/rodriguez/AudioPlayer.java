package fun.zenkong.rodriguez;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioPlayer {
    public void playAudio(String fileName)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InterruptedException
    {

        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(new File(fileName).getAbsoluteFile());
        Clip clip;
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
        Thread.sleep(clip.getMicrosecondLength()/1000);
        clip.stop();
        clip.close();
    }
}
