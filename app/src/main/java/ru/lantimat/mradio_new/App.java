package ru.lantimat.mradio_new;

/**
 * Created by Ильназ on 16.03.2017.
 */

import android.app.Application;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class App extends Application {

    private static RecordApi recordApi;
    private Retrofit retrofit;

    @Override
    public void onCreate() {
        super.onCreate();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://www.radiorecord.ru/") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        recordApi = retrofit.create(RecordApi.class); //Создаем объект, при помощи которого будем выполнять запросы
    }

    public static RecordApi getApi() {
        return recordApi;
    }
}
