package com.example.myapplication6667666765;


import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity6667666765 extends Activity implements OnInitListener,  OnClickListener {
    Context context=this;
    Locale locale;
    Locale locale2;
    String lang;
    String lang2;
    boolean isHSConnected = false;

    private static final int VR_REQUEST=999;
    private TextView textView;
    //Log для вывода вспомогательной информации
    private final String LOG_TAG="SpeechRepeatActivity";

    //переменная для проверки данных для TTS
    private int MY_DATA_CHECK_CODE=0;

    //Text To Speech интерфейс
    private TextToSpeech repeatTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MusicIntentReceiver myReciever = new MusicIntentReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(myReciever, filter);
        final String[] languges = new String[] {"en","fr","ru"};
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,languges);
        spinner.setAdapter(adapter);
        Spinner spinner2 = (Spinner)findViewById(R.id.spinner2);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,languges);
        spinner2.setAdapter(adapter2);
        Button speechBtn= findViewById(R.id.speech_btn);
        textView = (TextView)findViewById(R.id.tv);
        //проверяем, поддерживается ли распознование речи
        PackageManager packManager= getPackageManager();
        List<ResolveInfo> intActivities= packManager.queryIntentActivities(new
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if(intActivities.size()!=0){
            // распознавание поддерживается, будем отслеживать событие щелчка по кнопке
            speechBtn.setOnClickListener(this);
            Button button = (Button)findViewById(R.id.button2);
            button.setOnClickListener(this);
        }
        else
        {
            // распознавание не работает. Заблокируем
            // кнопку и выведем соответствующее
            // предупреждение.
            speechBtn.setEnabled(false);
            Toast.makeText(this,"Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }
        //подготовка движка TTS для проговаривания слов
        Intent checkTTSIntent=new Intent();
        //проверка наличия TTS
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //запуск checkTTSIntent интента
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    public void onInit(int status) {
        if(status== TextToSpeech.SUCCESS)
            repeatTTS.setLanguage(locale); //Язык
    }

    @Override
    public void onClick(View v) {
        if(v.getId()== R.id.speech_btn){
            AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
            manager.setMode(AudioManager.STREAM_MUSIC);
            manager.setSpeakerphoneOn(true);
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            lang=spinner.getSelectedItem().toString();
            lang2=spinner2.getSelectedItem().toString();
            locale = new Locale(lang);
            locale2 = new Locale(lang);
            listenToSpeech();
        }
        if(v.getId()== R.id.button2){
            if (isHSConnected) {
                AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                manager.setMode(AudioManager.STREAM_MUSIC);
                manager.setSpeakerphoneOn(false);
            }
            else {
                AudioManager manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                manager.setMode(AudioManager.STREAM_MUSIC);
                manager.setSpeakerphoneOn(true);
            }
            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
            lang2=spinner.getSelectedItem().toString();
            lang=spinner2.getSelectedItem().toString();
            locale = new Locale(lang);
            locale2 = new Locale(lang);
            listenToSpeech();
        }
    }

    private void listenToSpeech(){
        //запускаем интент, распознающий речь и передаем ему требуемые данные
        Intent listenIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //указываем пакет
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getClass().getPackage().getName());
        //В процессе распознования выводим сообщение
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say a word!");
        //устанавливаем модель речи
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //указываем число результатов, которые могут быть получены
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        //начинаем прослушивание
        startActivityForResult(listenIntent, VR_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        //проверяем результат распознавания речи
        if(requestCode== VR_REQUEST && resultCode== RESULT_OK)
        {
            //Добавляем распознанные слова в список результатов
            ArrayList<String> suggestedWords=
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //Передаем список возможных слов через ArrayAdapter компоненту ListView
            //  wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.activity_main6667666765, suggestedWords));
            if (suggestedWords.size()!=0){
                Log.d("log",suggestedWords.get(0).toString());
                String languagePair=lang2+"-"+lang; //с КАКОГО на КАКОЙ переводить
                String st = Translate(suggestedWords.get(0),languagePair);
                textView.setText(st);
            }
        }

        //returned from TTS data check
        if(requestCode== MY_DATA_CHECK_CODE)
        {
            //все необходимые приложения установлены, создаем TTS
            if(resultCode== TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
                repeatTTS=new TextToSpeech(this, this);
            //движок не установлен, предположим пользователю установить его
            else
            {
                //интент, перебрасывающий пользователя на страницу TSS в Google Play
                Intent installTTSIntent=new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
        repeatTTS.setLanguage(locale2);
        repeatTTS.speak(textView.getText().toString(), TextToSpeech.QUEUE_FLUSH,null);
        //вызываем метод родительского класса
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String Translate(String textToBeTranslate, String languagePair)
    {
        TranslatorBackgroundTask translatorBackgroundTask = new TranslatorBackgroundTask(context);
        String translationResult = null;
        try {
            translationResult = translatorBackgroundTask.execute(textToBeTranslate,languagePair).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return translationResult;
    }

    private class MusicIntentReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if(isHSConnected) {
                            Log.d("", "Headset is unplugged");
                            isHSConnected=false;
                        }
                        break;
                    case 1:
                        Log.d("", "Headset is plugged");
                        isHSConnected = true;
                        break;
                    default:
                        Log.d("", "I have no idea what the headset state is");
                }
            }
        }
    }

}



