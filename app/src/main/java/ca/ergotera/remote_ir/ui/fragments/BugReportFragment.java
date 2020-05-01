package ca.ergotera.remote_ir.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

/**
 * Fragment (UI and behaviour) allowing users to report bugs directly within
 * the application.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class BugReportFragment extends android.app.Fragment {

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_bug_report, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getToolbar().setTitle(getResources().getString(R.string.nav_menu_extras_section_report_bug));
    }
}
