package com.example.android.rxjavademo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG=MainActivity.class.getSimpleName();

    OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d(LOG_TAG,message);
            }
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        new OkHttpHandler(mOkHttpClient).execute("https://github.com/ReactiveX/RxJava/wiki/How-To-Use-RxJava");
    }

    private void onOkHttpDownloaded(String content){
        content.length();
    }

    private class OkHttpHandler extends AsyncTask<String, Void, String> {

        OkHttpClient client;

        public OkHttpHandler(OkHttpClient client) {
            this.client = client;
        }

        @Override
        protected void onPostExecute(String s) {
            onOkHttpDownloaded(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();
            Response response = null;
            String content = null;
            try {
                response = client.newCall(request).execute();
                content =response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
    }
}
