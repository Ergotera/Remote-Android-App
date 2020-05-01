package ca.ergotera.remote_ir.ui.fragments;

import android.database.Cursor;
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
import ca.ergotera.remote_ir.utils.Utils;

/**
 * Fragment (UI and behaviour) allowing users to see a list of their
 * virtual interfaces. From here, they should also be able to access the
 * virtual interfaces's creation menu (Fragment).
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class InterfaceListFragment extends android.app.Fragment {

    private static final String CLASS_ID = InterfaceListFragment.class.getSimpleName();

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
        view = inflater.inflate(R.layout.frag_interface_list, container, false);
        uiConfigured = false;
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FloatingActionButton addButton = (FloatingActionButton) getView().findViewById((R.id.frag_interface_list_new_interface));
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.content_frame, new CreateInterfaceFragment()).addToBackStack(null).commit();
            }
        });


        android.app.FragmentManager fragMgr = getFragmentManager();
        LinearLayout linlayout = (LinearLayout) getActivity().findViewById(R.id.frag_interface_list_scrollable_linlayout);

        DatabaseManager dbMgr = new DatabaseManager(getActivity());
        Cursor response = dbMgr.getAllDataFromTable(DatabaseManager.INTERFACE_TABLE_NAME);

        if (response == null || response.getCount() == 0) {
            TextView emptyListNotice = new TextView(getActivity());
            emptyListNotice.setText(getResources().getString(R.string.empty_interface_list));
            emptyListNotice.setTextSize(24);
            linlayout.addView(emptyListNotice);
        } else {
            if (!this.uiConfigured) {
                while (response.moveToNext()) {
                    int interfaceId = response.getInt(0);
                    String interfaceCreationDate = response.getString(1);
                    String interfaceModificationDate = response.getString(2);
                    String interfaceName = response.getString(3);
                    String strBtnIds = response.getString(4);
                    int[] interfaceBtnIds = null;
                    if (strBtnIds != null && !strBtnIds.equals(""))
                        interfaceBtnIds = Utils.convertStrArrayToIntArray(strBtnIds);
                    InterfaceListElementFragment btnListElem = InterfaceListElementFragment.newInstance(interfaceId, interfaceCreationDate, interfaceModificationDate, interfaceName, interfaceBtnIds);
                    fragMgr.beginTransaction().add(R.id.frag_interface_list_scrollable_linlayout, btnListElem).commit();
                }
            } else {
                Logger.Debug(CLASS_ID, "List was already created, not recreating.");
            }
            dbMgr.close();
            this.uiConfigured = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getToolbar().setTitle(getResources().getString(R.string.nav_menu_nav_section_interface_list));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putBoolean(UI_CONFIGURED, this.uiConfigured);
    }
}
