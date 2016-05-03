package com.rkhandavilli.mygarage;

import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GarageActivity extends AppCompatActivity {

    private static final String LOG_TAG = GarageActivity.class.getSimpleName();
    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage);
        displaySpeechRecognizer();

        // testing http post
        // new SendCommandtoPi().execute("open");


    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Which garage do you want to operate?");
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.i(LOG_TAG, spokenText);

            TextView textView = (TextView) findViewById(R.id.textDisplay);

            SendCommandtoPi sendCommandtoPi = new SendCommandtoPi();

            switch(spokenText) {
                case "open garage 1" :
                    textView.setText("opening garage one");
                    sendCommandtoPi.execute("open");

                    // to send to different garages
                    // sendCommandtoPi.execute("open", "one");
                    break;
                case "close garage 1" :
                    textView.setText("closing garage one");
                    sendCommandtoPi.execute("close");
                    break;
                default:
                    textView.setText("unrecognized command: " + spokenText);
                    // repeat the process again?
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class SendCommandtoPi extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String command = params[0];

            // Assign garage number to second parameter
            // String garage = params[1];

            final String GARAGE_URL = "http://<ip-address>/GPIO/<pin-number>/value/";
            String piCommandUrl;

            if (command.equals("open")) {
                piCommandUrl = GARAGE_URL + "1";
            } else if (command.equals("close")) {
                piCommandUrl =  GARAGE_URL + "0";
            } else {
                return null;
            }

            // String testUrl = "http://10.94.20.102:8529/success";
            try {
                URL url = new URL(piCommandUrl);
                // URL url = new URL(testUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

//                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//                byte[] data = new byte[0];
//                wr.write(data);
//                wr.flush();
//                wr.close();

                Log.i(LOG_TAG, "Status code: "+ conn.getResponseCode());

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Log.i(LOG_TAG, "Http response: " + response);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
