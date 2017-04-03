package com.example.android.rxjavademo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hello("John", "Vasja", "Alyona");

        Observable<String> asyncObservable = createCustomObservableAsync();

        asyncObservable.subscribe(createLoggingObserver("1st Observer"));

        delay(537);
        Log.d(LOG_TAG,"adding new subscriber");

        Disposable subscribtion = asyncObservable.subscribe(
                new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        // onNext
                        Log.d("2nd Consumer", "onNext(): " + s);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        // onerror
                        Log.d("2nd Consumer", "onError(): " + throwable.getMessage());
                    }
                },
                new Action() {
                    @Override
                    public void run() throws Exception {
                        // onComplete
                        Log.d("2nd Consumer", "onComplete()");
                    }
                }
        );

        // cancel subscribtion after some period
        delay(321);
        Log.d(LOG_TAG,"disposing 2nd Consumer's subscribtion");
        subscribtion.dispose();

    }

    public static void hello(String... names) {
//        Observable.fromArray(names).subscribe(s -> Log.d(LOG_TAG, "Hello, " + s + "!"));
        Observable.fromArray(names).subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                Log.d(LOG_TAG, "Hello, " + s + "!");
            }
        });
    }

    public static void delay(int ms){
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    public Observer createLoggingObserver(final String tag) {
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable disposable) {
                Log.d(tag, "onSubscribe()");
            }

            @Override
            public void onNext(String s) {
                Log.d(tag, "onNext(): " + s);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(tag, "onError()" + throwable.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(tag, "onComplete()");
            }
        };
    }

    public Observable createCustomObservableBlocking() {
        // create an observable that will emit items
        Observable<String> o = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                try {
                    for (int i = 0; i < 70; i++) {
                        Log.d("(B) Emitter", "Value #" + i);
                        emitter.onNext("Value #" + i);
                        Thread.sleep(10);

                        emitter.onComplete();
                    }
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
        return o;
    }

    public Observable createCustomObservableAsync() {
        Observable<String> o = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> obsEmitter) throws Exception {
                final ObservableEmitter<String> emitter = obsEmitter;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (int i = 0; i < 70; i++) {
                                Log.d("(A) Emitter", "Value #" + i);
                                emitter.onNext("Value #" + i);
                                Thread.sleep(100);
                            }
                            Log.d("(A) Emitter", "onComplete");
                            emitter.onComplete();
                        } catch (Exception e) {
                            Log.d("(A) Emitter", "onError");
                            emitter.onError(e);
                        }
                    }
                }).start();
                ;
            }
        });
        return o;
    }


    private void onOkHttpDownloaded(String content) {
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
                content = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }
    }
}
