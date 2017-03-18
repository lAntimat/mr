package ru.lantimat.mradio_new.fragments;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.lantimat.mradio_new.Adapters.GridViewAdapterPodcast;
import ru.lantimat.mradio_new.Adapters.Podcast;
import ru.lantimat.mradio_new.Adapters.PodcastAdapter;
import ru.lantimat.mradio_new.App;
import ru.lantimat.mradio_new.R;
import ru.lantimat.mradio_new.models.PodcastModel;
import ru.lantimat.mradio_new.models.PodcastResultModel;
import ru.lantimat.mradio_new.models.SongModel;

/**
 * Created by Ильназ on 05.01.2017.
 */
public class PodcastFragment extends Fragment {

    View view;

    GridView gvPodcast;
    GridViewAdapterPodcast gridViewAdapterPodcast;
    PodcastAdapter podcastAdapter;
    ArrayList<Podcast> podcastArrayList = new ArrayList<>();
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    Document doc = null;
    List<PodcastResultModel> podcast;
    List<SongModel> podcasts;

    public PodcastFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_podcast, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("Подкасты");

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference();

        /*Slider friendlyMessage = new
                Slider("Молодёжное радио",
                "https://pp.vk.me/c622816/v622816404/5513d/wFz7SB0wTR8.jpg");
        mFirebaseDatabase.child("slider")
                .push().setValue(friendlyMessage);*/

        initListView();
        getPodcast();

        //parseAlbums();

        return view;
    }

    public void initListView() {
        // настраиваем список
        gvPodcast = (GridView) view.findViewById(R.id.gridView);
        gvPodcast.setNumColumns(2);


        gvPodcast.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //imageUrl for PodcastSongFragment
                Bundle bundle = new Bundle();
                //bundle.putString("message", podcastArrayList.get(position).urlImageBig);
                bundle.putInt("id", podcast.get(position).getId());
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                PodcastSongsFragment fragment = new PodcastSongsFragment();
                fragment.setArguments(bundle);
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack("PodcastSongFragment").commit();


            }
        });


    }

    public void getPodcast() {

        App.getApi().getPodcast().enqueue(new Callback<PodcastModel>() {
            @Override
            public void onResponse(Call<PodcastModel> call, Response<PodcastModel> response) {
                PodcastModel result = response.body();
                podcast = result.getResult();
                gridViewAdapterPodcast = new GridViewAdapterPodcast(getActivity().getApplicationContext(), podcast);
                gvPodcast.setAdapter(gridViewAdapterPodcast);
            }

            @Override
            public void onFailure(Call<PodcastModel> call, Throwable t) {
                Toast.makeText(getContext(), "An error occurred during networking", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });

}

    private void fireBase() {
        mFirebaseInstance = FirebaseDatabase.getInstance();

        // get reference to 'users' node
        mFirebaseDatabase = mFirebaseInstance.getReference().child("podcast");

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    podcastArrayList.add(postSnapshot.getValue(Podcast.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void parseAlbums() {



        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl("gs://mradio-2613a.appspot.com").child("albums.txt");

        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(Task<Uri> task) {
                Log.d("Task", task.getResult().toString());
                new ParseJson().execute(task.getResult().toString());

            }
        });
    }



    class ParseJson extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
            //Парсим сайт
            doc = Jsoup.connect(params[0]).ignoreContentType(true).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject;
        String json;
        json = doc.text();
        Log.d("JSON", json);
        //Вытягиваем нужное из json
        JsonParser parser = new JsonParser();
        JsonObject mainObject = parser.parse(json).getAsJsonObject();
        mainObject = mainObject.getAsJsonObject("response");
        Log.d("JSON_responce", mainObject.toString());
        JsonArray pItem = mainObject.getAsJsonArray("items");

        for (JsonElement user : pItem) {
            JsonObject userObject = user.getAsJsonObject();
            Log.d("id", String.valueOf(userObject.get("id")));
            Log.d("title", String.valueOf(userObject.get("title")));
            podcastArrayList.add(new Podcast(userObject.get("title").toString(), userObject.get("id").toString().replace("\"", ""), userObject.get("id").toString()));

        }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            getPodcast();

            super.onPostExecute(aVoid);
        }
    }

}
