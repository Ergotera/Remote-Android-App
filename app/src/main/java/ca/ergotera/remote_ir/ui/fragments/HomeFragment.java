package ca.ergotera.remote_ir.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

/**
 * Fragment (UI and behaviour) welcoming users to the application. It could
 * be used to display the latest features / update details, give them a shortcut
 * to their most used interfaces (allowing them to quickly launch their system).
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class HomeFragment extends android.app.Fragment {

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_home, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getToolbar().setTitle(getResources().getString(R.string.nav_menu_nav_section_home));
    }
}
