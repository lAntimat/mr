package ru.lantimat.mradio_new.fragments;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.lantimat.mradio_new.R;

/**
 * Created by Ильназ on 05.01.2017.
 */
public class MoreFragment extends Fragment {

    public MoreFragment() {}
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        return view;
    }
}
