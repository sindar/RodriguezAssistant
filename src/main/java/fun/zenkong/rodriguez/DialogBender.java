package fun.zenkong.rodriguez;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;

public class DialogBender {

    private static final String ACOUSTIC_MODEL =
            "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static final String DICTIONARY_PATH =
            "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String LANGUAGE_MODEL =
            "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";
    private static final String GRAMMAR_PATH =
            "resource:/dialog/";

    private static final Map<String, String> AUDIO_FILES =
            new HashMap<String, String>();

    private static Map<String, String> parameters =
            new HashMap<String, String>();

    private static final String AUDIO_PATH = "./audio/";;

    private static Timer sleepTimer;
    private static SleepTimerTask sleepTimerTask;

    private static boolean isPlayingAnswer;
    private static boolean isSleeping;

    private static AudioPlayer audioPlayer = new AudioPlayer();

    static {
        AUDIO_FILES.put("shutdown", "with_bjah.wav");
        AUDIO_FILES.put("start", "lets_get_drunk.wav");
        AUDIO_FILES.put("exit", "lets_get_drunk.wav");
        AUDIO_FILES.put("hey bender 0", "bite.wav");
        AUDIO_FILES.put("hey bender 1", "hello.wav");
        AUDIO_FILES.put("hey bender 2", "hello_peasants.wav");
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
        AUDIO_FILES.put("enable", "can_do.wav");
        AUDIO_FILES.put("disable", "can_do.wav");
        AUDIO_FILES.put("set", "can_do.wav");
        AUDIO_FILES.put("how are you", "right_now_i_feel_sorry_for_you.wav");
        AUDIO_FILES.put("unrecognized", "beat_children.wav");
        AUDIO_FILES.put("no audio", "silence.wav");
    }

    static {
        parameters.put("sleep", "disable");
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setAcousticModelPath(ACOUSTIC_MODEL);
        configuration.setDictionaryPath(DICTIONARY_PATH);
        configuration.setGrammarPath(GRAMMAR_PATH);
        configuration.setLanguageModelPath(LANGUAGE_MODEL);

        configuration.setUseGrammar(true);
        configuration.setGrammarName("bender");
        LiveSpeechRecognizer jsgfRecognizer =
                new LiveSpeechRecognizer(configuration);
        jsgfRecognizer.startRecognition(true);

        int fsmState;

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
                    fsmState = processCommand(jsgfRecognizer, fsmState);
                    break;
                case 2:
                    if(!isPlayingAnswer) {
                        if (parameters.get("sleep") == "enable"
                                && sleepTimer == null) {
                            sleepTimer = new Timer();
                            sleepTimerTask = new SleepTimerTask();
                            sleepTimer.schedule(sleepTimerTask, 10000);
                        }
                        fsmState = processCommand(jsgfRecognizer, fsmState);
                    }
                    break;
                case 3:
                    fsmState = processCommand(jsgfRecognizer, fsmState);
                    break;
                case 10:
                    jsgfRecognizer.stopRecognition();
                    return;
            }
        }

    }


    static class SleepTimerTask extends TimerTask {
        @Override
        public void run() {
            isSleeping = true;
        }
    }

    private static int processCommand(LiveSpeechRecognizer jsgfRecognizer, int curfsmState)
            throws NullPointerException
    {
        if(isPlayingAnswer)
            return curfsmState;

        int newfsmState = curfsmState;
        String answer = null;
        String parameter;
        if(curfsmState == 1) {

        }

        String utterance = jsgfRecognizer.getResult().getHypothesis();

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
        } else if (utterance.contains("how are you")) {
            command = "how are you";
        } else if (utterance.contains("where are you from")
                    || utterance.contains("where were you born")) {
            command = "birthplace";
        } else if (utterance.contains("when were you born")
                    || utterance.contains("birth")) {
            command = "birthdate";
        } else if (utterance.endsWith("your favorite animal")) {
            command = "animal";
        } else if (utterance.endsWith("bender") &&
                    (utterance.contains("hi") || utterance.contains("hey")
                     || utterance.contains("hello"))) {
            option = curTime % 3;
            command = "hey bender " + String.valueOf(option);;
        } else if (utterance.contains("magnet")) {
            option = curTime % 2;
            command = "magnet " + String.valueOf(option);
        } else if (utterance.contains("new sweater")) {
            command = "new sweater";
        } else if ((utterance.contains("wake up")
                || utterance.contains("awake"))
                && isSleeping) {
            command = "wake up";
        } else if (utterance.startsWith("enable")
                || utterance.startsWith("disable")) {
            command = utterance;
        } else if (utterance.startsWith("set")) {
            command = utterance;
        }

        if(curfsmState == 1) {
            if (command != null) {
                if (command.contains("hey bender")) {
                    newfsmState = 2;
                    answer = command;
                }
            }
        }

        if(curfsmState == 2) {
            if (parameters.get("sleep") == "enable"
                    && isSleeping) {
                command = null;
                newfsmState = 3;
            }
            if (command != null) {
                answer = command;
                if (command.equals("shutdown")) {
                    answer = "shutdown";
                    newfsmState = 10;
                } else if (command.equals("exit")) {
                    answer = "exit";
                    newfsmState = 0;
                } else if (command.startsWith("enable")
                        || command.startsWith("disable")) {
                    String parValue;
                    if (command.startsWith("enable"))
                        parValue = "enable";
                    else
                        parValue = "disable";
                    parameter = getParameterFromCommand(command);
                    if (parameter != null) {
                        parameters.replace(parameter, parValue);
                        answer = parValue;
                    } else {
                        answer = "unrecognized";
                    }
                } else if (command.startsWith("set")) {
                    //TODO: add logic
                    answer = "set";
                }
            }
        }

        if(curfsmState == 3) {
            playBenderPhrase("kill all humans");
            if (command == "wake up") {
                answer = command;
                newfsmState = 2;
                isSleeping = false;
            }
        }

        if(answer != null)
            playBenderPhrase(answer);

        return newfsmState;
    }

    private static void playBenderPhrase(String command) {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimerTask = null;
            sleepTimer = null;
        }
        try {
            isPlayingAnswer = true;
            audioPlayer.playAudio(AUDIO_PATH + AUDIO_FILES.get(command));
            isPlayingAnswer = false;
        }
        catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static String getParameterFromCommand(String command)
            throws NullPointerException {
        String cur_parameter = null;
        if(command.contains("sleep") || command.contains("sleeping"))
            cur_parameter = "sleep";
        if (parameters.containsKey(cur_parameter))
            return cur_parameter;
        else
            return null;
    }
}