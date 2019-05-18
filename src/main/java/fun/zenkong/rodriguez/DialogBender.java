package fun.zenkong.rodriguez;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class DialogBender {

    private static final String ACOUSTIC_MODEL =
            "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
            "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String LANGUAGE_MODEL =
            "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
    private static final String GRAMMAR_PATH =
            "resource:/dialog/";

    private static Clip clip;
    private static int fsmState;

    private static final Map<String, String> AUDIO_FILES =
            new HashMap<String, String>();

    private static final String AUDIO_PATH = "./audio/";;

    static {
        AUDIO_FILES.put("shutdown", "with_bjah.wav");
        AUDIO_FILES.put("hey bender", "bite.wav");
        AUDIO_FILES.put("birthplace", "born_in_tijuana.wav");
        AUDIO_FILES.put("birthdate", "birthdate.wav");
        AUDIO_FILES.put("who are you 0", "im_bender.wav");
        AUDIO_FILES.put("who are you 1", "bender_song.wav");
        AUDIO_FILES.put("animal", "turtle.wav");
        AUDIO_FILES.put("sing", "bender_song.wav");
        AUDIO_FILES.put("magnet 0", "roads_song.wav");
        AUDIO_FILES.put("magnet 1", "mountain_song.wav");
        AUDIO_FILES.put("exit", "can_do.wav");
        AUDIO_FILES.put("unrecognized", "beat_children.wav");
    }

    public static void main(String[] args) throws Exception {
        String command;
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setLanguageModelPath(LANGUAGE_MODEL);

        configuration.setUseGrammar(true);
        configuration.setGrammarName("bender");
        LiveSpeechRecognizer jsgfRecognizer =
                new LiveSpeechRecognizer(configuration);

        fsmState = 0;

        while (true) {
            switch (fsmState) {
                case 0:
                    fsmState = 1;
                    System.out.println("Say: \"Hey Bender!\"");
                case 1:
                    command = recognizeCommand(jsgfRecognizer);
                    if (command.equals("hey bender")) {
                        fsmState = 2;
                        try {
                            playAudio(AUDIO_FILES.get(command));
                        }
                        catch (Exception ex) {
                            System.out.println("Error with playing sound.");
                            ex.printStackTrace();
                            fsmState = 10;
                        }
                    }
                    break;
                case 2:
                    command = recognizeCommand(jsgfRecognizer);
                    if (command.equals("shutdown")) {
                        fsmState = 10;
                    } else if (command.equals("exit")) {
                        fsmState = 0;
                    }
                    try {
                        playAudio(AUDIO_FILES.get(command));
                    }
                    catch (Exception ex) {
                        System.out.println("Error with playing sound.");
                        ex.printStackTrace();
                        fsmState = 10;
                    }
                    break;
                case 10:
                    return;
            }
        }
    }

    private static String recognizeCommand(LiveSpeechRecognizer jsgfRecognizer)
            throws NullPointerException
    {
        jsgfRecognizer.startRecognition(true);
        String utterance = jsgfRecognizer.getResult().getHypothesis();
        jsgfRecognizer.stopRecognition();
        System.out.println(utterance);
        long curTime = new Date().getTime();
        long option;

        String command = "unrecognized";
        if (utterance.startsWith("shutdown")) {
            command = "shutdown";
        } else if (utterance.startsWith("exit")
                    || utterance.startsWith("quit")) {
            command = "exit";
        } else if (utterance.startsWith("sing")) {
            command = "sing";
        } else if (utterance.contains("who are you")) {
            option = curTime % 2;
            command = "who are you " + String.valueOf(option);
        } else if (utterance.contains("where are you from")
                    || utterance.contains("where were you born")) {
            command = "birthplace";
        } else if (utterance.contains("when were you born")
                    || utterance.contains("birth")) {
            command = "birthdate";
        } else if (utterance.endsWith("your favorite animal")) {
            command = "animal";
        } else if (utterance.endsWith("bender")) {
            command = "hey bender";
        } else if (utterance.contains("magnet")) {
            option = curTime % 2;
            command = "magnet " + String.valueOf(option);
        }
        return command;
    }

    private static void playAudio(String fileName)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InterruptedException
    {
        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(new File(AUDIO_PATH + fileName).getAbsoluteFile());
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
        Thread.sleep(clip.getMicrosecondLength()/1000);
        clip.stop();
        clip.close();
    }
}