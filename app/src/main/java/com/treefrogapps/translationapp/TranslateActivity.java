package com.treefrogapps.translationapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class TranslateActivity extends ActionBarActivity {

    String[] languagesArray;
    String[] languagesArrayLC;
    String[] translationsArray = new String[11];
    ArrayList<TranslateList> translateArrayList = new ArrayList<>();

    TranslateAdapter translateAdapter;
    EditText englishWordsEditText;
    ListView translateListView;
    Button translateButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        languagesArray = getResources().getStringArray(R.array.languagesArray);
        languagesArrayLC = getResources().getStringArray(R.array.languagesArrayLC);

        englishWordsEditText = (EditText) findViewById(R.id.englishWordsEditText);
        translateButton = (Button) findViewById(R.id.translateButton);

        translateArrayList = updateListView(languagesArray, translationsArray);
        translateAdapter = new TranslateAdapter(translateArrayList, getApplicationContext());
        translateListView = (ListView) findViewById(R.id.translateListView);
        translateListView.setAdapter(translateAdapter);


        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkConnection() && !isEmpty(englishWordsEditText)) {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.get_trans), Toast.LENGTH_SHORT).show();

                    new SaveTheFeed().execute();

                } else {

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enter_words), Toast.LENGTH_SHORT).show();
                }

            }
        });

    } // END OF onCreate

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

    protected boolean isEmpty(EditText editText){

        return englishWordsEditText.getText().toString().trim().length() == 0;

    }   //END OF isEmpty

    public ArrayList<TranslateList> updateListView(String[] languagesArray, String[] translationsArray){

        translateArrayList.clear();

        for (int i = 0; i < languagesArray.length; i++){

            TranslateList translateList = new TranslateList();

            translateList.setLanguage(languagesArray[i]);
            translateList.setTranslation(translationsArray[i]);

            translateArrayList.add(translateList);
        }

        return translateArrayList;

    }   // END OF updateListView

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

    class SaveTheFeed extends AsyncTask<Void, Void, Void> {

        String jsonString = "";
        String result = "";


        @Override
        protected Void doInBackground(Void... params) {

            String wordsToTranslate = englishWordsEditText.getText().toString().trim();
            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            BasicHttpParams myParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(myParams, 9000);
            HttpConnectionParams.setSoTimeout(myParams, 9000);

            DefaultHttpClient httpClient = new DefaultHttpClient(myParams);

            HttpPost httpPost = new HttpPost("http://www.treefrogapps.com/language/translateitjson.php?action=translations&english_words=" + wordsToTranslate);
            httpPost.setHeader("Content-Type", "application/json");

            InputStream inputStream = null;

            try {

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                StringBuilder builder = new StringBuilder();
                String line = null;

                while ((line = reader.readLine()) != null){

                    builder.append(line + "\n");

                }

                jsonString = builder.toString();

                JSONObject jsonObject = new JSONObject(jsonString);

                JSONArray jsonArray = jsonObject.getJSONArray("translations");

                outputTranslations(jsonArray);

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            translateArrayList = updateListView(languagesArray, translationsArray);
            translateListView.setAdapter(translateAdapter);

        }

    }



    public void outputTranslations(JSONArray jsonArray){

        try {

            for(int i =0; i < jsonArray.length(); i++){

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                translationsArray[i] = jsonObject.getString(languagesArrayLC[i]);

                Log.v("Returned Tranlsation = ", translationsArray[i]);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    } // END OF outputTranslations


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle persistableBundle) {
        super.onSaveInstanceState(outState, persistableBundle);

        outState.putStringArray("translationArray", translationsArray);
    }
}
