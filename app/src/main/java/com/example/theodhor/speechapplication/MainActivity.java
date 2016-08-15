package com.example.theodhor.speechapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @see <a href="https://www.sitepoint.com/using-android-text-to-speech-to-create-a-smart-assistant/"></a>
 * @see <a href="https://github.com/PhongHuynh93/SpeechApplication"></a>
 */
public class MainActivity extends AppCompatActivity {


    private TextToSpeech tts;
    private ArrayList<String> questions;
    private String name, surname, age, asName;

    /**
     * To make the app more interesting, in this step you are going to make the application able learn simple things, like your name.
     * To make this possible, you need to use local storage.
     */
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS = "prefs";
    private static final String NEW = "new";
    private static final String NAME = "name";
    private static final String AGE = "age";
    private static final String AS_NAME = "as_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        preferences = getSharedPreferences(PREFS, 0);
        editor = preferences.edit();

        /**
         * todo 4 - To make the application listen, you will use the microphone button. Add this code to onCreate:
         */
        findViewById(R.id.microphoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });
        loadQuestions();

        // TODO: 8/15/16 1 -  Declare a TextToSpeech variable, This starts the TextToSpeech service.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
//                    The speak() method takes a String parameter, which is the text you want Android to speak.
                    speak("Hello");

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

    }

    private void loadQuestions() {
        questions = new ArrayList<>();
        questions.clear();
        questions.add("Hello, what is your name?");
        questions.add("What is your surname?");
        questions.add("How old are you?");
        questions.add("That's all I had, thank you ");
    }

    /** TODO: 8/15/16 5 when click the listen button
     * This method starts the listening activity which displays as a dialog with a text prompt.
     * The language of the speech is taken from the device, via the Locale.getDefault() method.
     *
     *
     *
     */
    private void listen() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            // The startActivityForResult(i, 100) method waits for the current activity to return a result.
            // 100 is a random code attached to the started activity, and can be any number that suits your use case.
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * todo 3 After speak() create another method to stop the TextToSpeech service when a user closes the app:
     * trái ngược với ondestroy là phải tắt nó
     */
    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // TODO: 8/15/16 2 -  The speak() method takes a String parameter, which is the text you want Android to speak.
    // fixme - note - when use different verson in code, put in a different method
    private void speak(String text) {
        // tts.speak(param,param,param) is deprecated for API levels over 5.1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    /** TODO: 8/15/16 6 - When a result returns from the started activity, it contains this code and uses it to differentiate multiple results from each other.
     * trong TH nhiều intent thì ta phải lọc ra đúng
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }

    private void recognition(String text) {
        Log.e("Speech", "" + text);
        String[] speech = text.split(" ");

        // TODO: 8/15/16 7 nếu như cái chữ của mình  khớp với chữ nào đó, thì device nói lại
        if (text.contains("hello")) {
            speak(questions.get(0));
        }
        //
        if (text.contains("my name is")) {
            name = speech[speech.length - 1];
            Log.e("THIS", "" + name);
            editor.putString(NAME, name).apply();
            speak(questions.get(2));
        }
        //This must be the age
        if (text.contains("years") && text.contains("old")) {
            String age = speech[speech.length - 3];
            Log.e("THIS", "" + age);
            editor.putString(AGE, age).apply();
        }

        if (text.contains("what time is it")) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
            Date now = new Date();
            String[] strDate = sdfDate.format(now).split(":");
            if (strDate[1].contains("00"))
                strDate[1] = "o'clock";
            speak("The time is " + sdfDate.format(now));

        }

        if (text.contains("wake me up at")) {
            speak(speech[speech.length - 1]);
            String[] time = speech[speech.length - 1].split(":");
            String hour = time[0];
            String minutes = time[1];
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_HOUR, Integer.valueOf(hour));
            i.putExtra(AlarmClock.EXTRA_MINUTES, Integer.valueOf(minutes));
            startActivity(i);
            speak("Setting alarm to ring at " + hour + ":" + minutes);
        }

        if (text.contains("thank you")) {
            speak("Thank you too " + preferences.getString(NAME, null));
        }

        if (text.contains("how old am I")) {
            speak("You are " + preferences.getString(AGE, null) + " years old.");
        }

        if (text.contains("what is your name")) {
            String as_name = preferences.getString(AS_NAME, "");
            if (as_name.equals(""))
                speak("How do you want to call me?");
            else
                speak("My name is " + as_name);
        }

        if (text.contains("call you")) {
            String name = speech[speech.length - 1];
            editor.putString(AS_NAME, name).apply();
            speak("I like it, thank you " + preferences.getString(NAME, null));
        }

        if (text.contains("what is my name")) {
            speak("Your name is " + preferences.getString(NAME, null));
        }
    }
}
