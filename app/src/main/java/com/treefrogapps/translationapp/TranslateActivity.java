package com.treefrogapps.translationapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TranslateActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TranslateAdapter translateAdapter;
    private EditText englishWordsEditText;
    private ListView translateListView;
    private Button speakButton;
    private Button translateButton;
    private Translations translationsArrayList;
    private TextToSpeech textToSpeech;

    final static Locale LOCALE_ARABIC = new Locale("pa", "Arab");
    final static Locale LOCALE_CHINESE = new Locale("zh", "Hans");
    final static Locale LOCALE_DANISH = new Locale("da", "DK");
    final static Locale LOCALE_DUTCH = new Locale("nl", "NL");
    final static Locale LOCALE_LATVIAN = new Locale("lv", "LV");
    final static Locale LOCALE_PORTUGUESE = new Locale("pt", "PT");
    final static Locale LOCALE_RUSSIAN = new Locale("ru", "RU");
    final static Locale LOCALE_SPANISH = new Locale("es", "ES");

    final static Locale[] LOCALE_LANGUAGES = {LOCALE_ARABIC, LOCALE_CHINESE, LOCALE_DANISH,LOCALE_DUTCH, Locale.FRENCH,
            Locale.GERMAN, Locale.ITALIAN, LOCALE_LATVIAN, LOCALE_PORTUGUESE, LOCALE_RUSSIAN, LOCALE_SPANISH};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        textToSpeech = new TextToSpeech(this,this);

        englishWordsEditText = (EditText) findViewById(R.id.englishWordsEditText);
        translateListView = (ListView) findViewById(R.id.translateListView);

        speakButton = (Button) findViewById(R.id.speakButton);
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isEmpty(englishWordsEditText)) {

                    textToSpeech.setLanguage(Locale.UK);
                    textToSpeech.speak(englishWordsEditText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });

        translateButton = (Button) findViewById(R.id.translateButton);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkConnection() && !isEmpty(englishWordsEditText)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.get_trans), Toast.LENGTH_SHORT).show();
                    okHttpConnect(englishWordsEditText.getText().toString());

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_words), Toast.LENGTH_SHORT).show();
                }

            }
        });

        translateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position > 1) {
                    String wordsToSpeak = translationsArrayList.getLanguagesArrayList().get(position).getTranslation();
                    Log.v("WORDS TO SPEAK : ", wordsToSpeak);
                    textToSpeech.setLanguage(LOCALE_LANGUAGES[position]);
                    textToSpeech.speak(wordsToSpeak, TextToSpeech.QUEUE_FLUSH, null);

                } else {

                    Toast.makeText(getApplicationContext(), "Language not Supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

    } // END OF onCreate

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS){

            int result = textToSpeech.setLanguage(Locale.UK);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Toast.makeText(getApplicationContext(), "Language not Supported", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Text To Speech Not Available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_translate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean isEmpty(EditText editText) {

        return englishWordsEditText.getText().toString().trim().length() == 0;

    }   //END OF isEmpty

    public boolean checkConnection() {

        // system service connectivity manager
        // include in manifest : <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
        ConnectivityManager checkNetworkStatus = (ConnectivityManager)
                this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);

        // network info will get all the status
        NetworkInfo networkInfo = checkNetworkStatus.getActiveNetworkInfo();

        // check that the state is 'connected' (either wifi or phone network - only 1 connection type
        // can exist at the same time
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;

        } else {

            Toast.makeText(getApplication(), getResources().getString(R.string.no_net),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }   // END OF checkConnection


    public void okHttpConnect(String wordsToTranslate) {

        wordsToTranslate = wordsToTranslate.replace(" ", "+").replace("'", "");
        String url = "http://www.treefrogapps.com/language/translateitjson2.php?action=translations&english_words=" + wordsToTranslate;

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(9000, TimeUnit.MILLISECONDS);
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toast.makeText(getApplicationContext(), "Error while retrieving translations", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (response.code() == 200) {

                    InputStream inputStream = response.body().byteStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                    StringBuilder stringBuilder = new StringBuilder();
                    String line = null;

                    while ((line = reader.readLine()) != null) {

                        stringBuilder.append(line + "\n");
                    }

                    String jsonString = stringBuilder.toString();

                    try {

                        Log.v("JSON STRING", jsonString);
                        outputTranslations(jsonString);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Error : " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void outputTranslations(String jsonString) throws JSONException {

        Gson gson = new Gson();

        translationsArrayList = gson.fromJson(jsonString, Translations.class);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                translateAdapter = new TranslateAdapter(translationsArrayList, getApplicationContext());
                translateListView.setAdapter(translateAdapter);
            }
        });

    } // END OF outputTranslations


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle persistableBundle) {
        super.onSaveInstanceState(outState, persistableBundle);
    }

}
