package ru.lantimat.mradio_new;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Ильназ on 05.01.2017.
 */
public class StorageUtil {
    private final String STORAGE = "ru.lantimat.mradio_new";
    private final String STORAGE_SLIDER = "ru.lantimat.mradio_new_SLIDER";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<Audio> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Audio> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Audio>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public void storePlayType(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("playType", index);
        editor.apply();
    }

    public int loadPlayType() {
        //return 1 Radio
        //return 2 Podcast
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("playType", -1);//return -1 if no data found;
    }

    public void storeSlider(ArrayList<Slider> arrayList) {
        preferences = context.getSharedPreferences(STORAGE_SLIDER, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("sliderArrayList");
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("sliderArrayList", json);
        editor.apply();
    }

    public ArrayList<Slider> loadSlider() {
        preferences = context.getSharedPreferences(STORAGE_SLIDER, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("sliderArrayList", null);
        Type type = new TypeToken<ArrayList<Slider>>() {
        }.getType();
        if(json!=null)
        return gson.fromJson(json, type);
        else return null;
    }

    public void clearCachedAudioPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    public void clearCachedSlider() {
        preferences = context.getSharedPreferences(STORAGE_SLIDER, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}
