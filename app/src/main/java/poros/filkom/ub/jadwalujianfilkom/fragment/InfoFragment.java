package poros.filkom.ub.jadwalujianfilkom.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import poros.filkom.ub.jadwalujianfilkom.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {
    public static String TAG= InfoFragment.class.getSimpleName();

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    public static InfoFragment newInstance() {
        InfoFragment fragment = new InfoFragment();
        return fragment;
    }
}
