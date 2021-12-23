package com.example.healthcheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import org.jsoup.nodes.Document;
import java.io.IOException;

import org.jsoup.Jsoup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Results extends AppCompatActivity {

    TextView ResultDescription;
    TextView MetricsDescription;
    ImageView Metrics_1;
    ImageView Metrics_2;
    String[] credentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        ResultDescription = findViewById(R.id.result_description);
        MetricsDescription = findViewById(R.id.metrics_description);
        Metrics_1 = findViewById(R.id.metrics_iv);
        Metrics_2 = findViewById(R.id.metrics_smile_iv);
        Intent intent = getIntent();

        credentials = intent.getStringArrayExtra("credentials");
        String first_value = intent.getStringExtra("first_value");
        String second_value = intent.getStringExtra("second_value");

        String data = String.format("day=15&month=12&year=1990&sex=1&m1=%s&m2=%s", first_value, second_value);

        try {
            healthcheck(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void healthcheck(String data) throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(mediaType, data);
        Request request = new Request.Builder()
                .url("http://abashin.ru/cgi-bin/ru/tests/burnout")
                .method("POST", body)
                .addHeader("Host", "abashin.ru")
                .addHeader("Connection", "close")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("DNT", "1")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;" +
                        "q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("Accept-Encoding", "deflate")
                .addHeader("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Content-Length", "43")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String message = response.body().string();

                    Document html_response = Jsoup.parse(message);
                    String format_message = html_response.text();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MetricsDescription.setText(format_message);
                            if (format_message.contains("отсутствию")) {
                                resultAwesome();
                            } else if (format_message.contains("небольшому")) {
                                resultGood();
                            } else if (format_message.contains("высокому")) {
                                resultBad();
                            } else if (format_message.contains("Error")) {
                                resultWorst();
                            }
                        }
                    });;
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void resultAwesome() {
        ResultDescription.setText("Не забывайте о регулярном профессиональном медицинском осмотре!");
        Metrics_1.setImageResource(R.drawable.percent_100);
        Metrics_2.setImageResource(R.drawable.verygood);

    }

    public void resultGood() {
        ResultDescription.setText("Вам необходимо отдохнуть и проконсультироваться со специалистом, если состояние не улучшится!");
        Metrics_1.setImageResource(R.drawable.percent_60);
        Metrics_2.setImageResource(R.drawable.good);
    }

    public void resultBad() {
        ResultDescription.setText("Вам необходимо срочно отдохнуть и обратиться в мед учреждение для осмотра!");
        Metrics_1.setImageResource(R.drawable.percent_40);
        Metrics_2.setImageResource(R.drawable.bad);
    }

    public void resultWorst() {
        ResultDescription.setText("Вам необходима срочная госпитализация и больничный режим!");
        Metrics_1.setImageResource(R.drawable.percent_0);
        Metrics_2.setImageResource(R.drawable.verybad);
    }
}
