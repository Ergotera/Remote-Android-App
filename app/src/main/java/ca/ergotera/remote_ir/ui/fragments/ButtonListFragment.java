package ca.ergotera.remote_ir.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.db.DatabaseManager;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

/**
 * Fragment (UI and behaviour) allowing users to see a list of their
 * virtual buttons. From here, they should also be able to access the
 * virtual button's creation menu (Fragment).
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class ButtonListFragment extends android.app.Fragment {

    private static final String CLASS_ID = ButtonListFragment.class.getSimpleName();

    private static final String UI_CONFIGURED = "UI_CONFIGURED";

    private View view;

    private boolean uiConfigured;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            this.uiConfigured = savedInstanceState.getBoolean(UI_CONFIGURED);
        } else {
            Logger.Debug(CLASS_ID, "Saved instance is null.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_button_list, container, false);
        uiConfigured = false;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById((R.id.frag_button_list_new_button));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new CreateButtonFragment()).addToBackStack(null).commit();
            }
        });

        android.app.FragmentManager fragMgr = getFragmentManager();
        LinearLayout linlayout = (LinearLayout) getActivity().findViewById(R.id.frag_button_list_scrollable_linlayout);

        DatabaseManager dbMgr = new DatabaseManager(getActivity());
        Cursor response = dbMgr.getAllDataFromTable(DatabaseManager.BTN_TABLE_NAME);

            if (response == null || response.getCount() == 0) {
                TextView emptyListNotice = new TextView(getActivity());
                emptyListNotice.setText(getResources().getString(R.string.empty_button_list));
                emptyListNotice.setTextSize(24);
                linlayout.addView(emptyListNotice);
            } else {
                if (!this.uiConfigured) {
                while (response.moveToNext()) {
                    int btnId = response.getInt(0);
                    String btnCreationDate = response.getString(1);
                    String btnModificationDate = response.getString(2);
                    String btnName = response.getString(3);
                    String btnStrImgPath = response.getString(4);
                    String btnStrAudioPath = response.getString(5);
                    Uri btnImgPath = null;
                    if (btnStrImgPath != null) {
                        btnImgPath = Uri.parse(btnStrImgPath);
                    }
                    Uri btnAudioPath = null;
                    if (btnStrAudioPath != null) {
                        btnAudioPath = Uri.parse(btnStrAudioPath);
                    }
                    String btnSignal = response.getString(6);
                    ButtonListElementFragment btnListElem = ButtonListElementFragment.newInstance(btnId, btnCreationDate, btnModificationDate, btnName, btnImgPath, btnAudioPath, btnSignal);
                    fragMgr.beginTransaction().add(R.id.frag_button_list_scrollable_linlayout, btnListElem).commit();
                }
            }
            dbMgr.close();
        }
        this.uiConfigured = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getToolbar().setTitle(getResources().getString(R.string.nav_menu_nav_section_button_list));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putBoolean(UI_CONFIGURED, this.uiConfigured);
    }
}
