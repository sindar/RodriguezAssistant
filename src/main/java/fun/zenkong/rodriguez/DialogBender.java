package fun.zenkong.rodriguez;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

    private static int fsmState;

    private static final Map<String, String> AUDIO_FILES =
            new HashMap<String, String>();

    private static final String AUDIO_PATH = "./audio/";;

    private static Timer sleepTimer;
    private static SleepTimerTask sleepTimerTask;

    private static boolean isPlayingAnswer;
    private static boolean isSleeping;

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
        AUDIO_FILES.put("new sweater", "new_sweater.wav");
        AUDIO_FILES.put("kill all humans", "kill_all_humans.wav");
        AUDIO_FILES.put("wake up", "most_wonderful_dream.wav");
        AUDIO_FILES.put("exit", "can_do.wav");
        AUDIO_FILES.put("unrecognized", "beat_children.wav");
        AUDIO_FILES.put("no audio", "silence.wav");
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
        sleepTimer = null;
        isPlayingAnswer = false;
        isSleeping = false;
        while (true) {
            switch (fsmState) {
                case 0:
                    fsmState = 1;
                    System.out.println("Say: \"Hey Bender!\"");
                case 1:
                    command = recognizeCommand(jsgfRecognizer);
                    if(command != null) {
                        if (command.equals("hey bender")) {
                            fsmState = 2;
                            playBenderAnswer(command);
                        }
                    }
                    break;
                case 2:
                    if(!isPlayingAnswer) {
                        if (sleepTimer == null) {
                            sleepTimer = new Timer();
                            sleepTimerTask = new SleepTimerTask();
                            sleepTimer.schedule(sleepTimerTask, 10000);
                        }

                        command = recognizeCommand(jsgfRecognizer);
                        if(isSleeping) {
                            command = null;
                            fsmState = 3;
                        }
                        if(command != null) {
                            if (command.equals("shutdown")) {
                                fsmState = 10;
                            } else if (command.equals("exit")) {
                                fsmState = 0;
                            }
                            playBenderAnswer(command);
                        }
                    }
                    break;
                case 3:
                    playBenderAnswer("kill all humans");
                    command = recognizeCommand(jsgfRecognizer);
                    if(command == "wake up") {
                        playBenderAnswer(command);
                        fsmState = 2;
                        isSleeping = false;
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
        if(isPlayingAnswer)
            return null;

        System.out.println(utterance);
        long curTime = new Date().getTime();
        long option;

        String command = "unrecognized";
        if (utterance.startsWith("shutdown")) {
            command = "shutdown";
        } else if (utterance.startsWith("exit")
                    || utterance.startsWith("quit")) {
            command = "exit";
        } else if (utterance.startsWith("sing")
                    && utterance.contains("song")) {
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
        } else if (utterance.contains("new sweater")) {
            command = "new sweater";
        } else if ((utterance.contains("wake up")
                || utterance.contains("awake"))
                && isSleeping) {
            command = "wake up";
        }
        return command;
    }

    private static void playBenderAnswer(String command) {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimerTask = null;
            sleepTimer = null;
        }
        try {
            playAudio(AUDIO_FILES.get(command));
        }
        catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void playAudio(String fileName)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InterruptedException
    {

        AudioInputStream audioInputStream =
                AudioSystem.getAudioInputStream(new File(AUDIO_PATH + fileName).getAbsoluteFile());
        Clip clip;
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
        isPlayingAnswer = true;
        Thread.sleep(clip.getMicrosecondLength()/1000);
        isPlayingAnswer = false;
        clip.stop();
        clip.close();
    }

    static class SleepTimerTask extends TimerTask {
        @Override
        public void run() {
            isSleeping = true;
        }
    }
}