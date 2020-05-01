package ca.ergotera.remote_ir.ui.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.db.DatabaseManager;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.models.VirtualInterface;

/**
 * Fragment (UI and behaviour) allowing users to report bugs directly within
 * the application.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class SelectableButtonListFragment extends android.app.Fragment {

    private static final String CLASS_ID = SelectableButtonListFragment.class.getSimpleName();

    private static final String UI_CONFIGURED = "UI_CONFIGURED";

    private static final String INTERFACE_BTN_IDS = "INTERFACE_BUTTON_IDS";

    private View view;

    private boolean uiConfigured;
    private ArrayList<ButtonListSelectableElementFragment> selectedBtnsList = new ArrayList<>();

    private int[] buttonids;

    /**
     * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
     * to use this static method bundling up the parameters we need. We will then be able to
     * retrieve this data by using the getArguments().getTYPE(BUNDLE_IDENTIFIER) methods.
     *
     * @param virtualInterface containing the list of selected button ids.
     * @return an instance of the fragment with the configured bundle.
     */
    public static SelectableButtonListFragment newInstance(VirtualInterface virtualInterface) {
        SelectableButtonListFragment frag = new SelectableButtonListFragment();
        Bundle bundle = new Bundle(1);
        bundle.putIntArray(INTERFACE_BTN_IDS, virtualInterface.getButtonIds());
        frag.setArguments(bundle);
        return frag;
    }

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
        view = inflater.inflate(R.layout.frag_selectable_button_list, container, false);

        uiConfigured = false;

        if (getArguments() != null) {
            this.buttonids = getArguments().getIntArray(INTERFACE_BTN_IDS);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!this.uiConfigured) {
            android.app.FragmentManager fragMgr = getFragmentManager();
            LinearLayout linlayout = (LinearLayout) getActivity().findViewById(R.id.frag_create_interface_selectable_btns_scrollable_linlayout);

            DatabaseManager dbMgr = new DatabaseManager(getActivity());
            Cursor response = dbMgr.getAllDataFromTable(DatabaseManager.BTN_TABLE_NAME);

            if (response.getCount() == 0) {
                TextView emptyListNotice = new TextView(getActivity());
                emptyListNotice.setText(getResources().getString(R.string.empty_button_list));
                emptyListNotice.setTextSize(24);
                emptyListNotice.setPadding(20, 20, 20, 20);
                linlayout.addView(emptyListNotice);
            } else {
                while (response.moveToNext()) {
                    int btnId = response.getInt(0);
                    String btnCreationDate = response.getString(1);
                    String btnModificationDate = response.getString(2);
                    String btnName = response.getString(3);
                    String btnStrImgPath = response.getString(4);
                    String btnStrAudioPath = response.getString(5);
                    String btnSignal = response.getString(6);
                    Uri btnImgPath = null;
                    if (btnStrImgPath != null) {
                        btnImgPath = Uri.parse(btnStrImgPath);
                    }
                    Uri btnAudioPath = null;
                    if (btnStrAudioPath != null) {
                        btnAudioPath = Uri.parse(btnStrAudioPath);
                    }
                    ButtonListSelectableElementFragment btnListElem;
                    if (getArguments() != null) {
                        btnListElem = ButtonListSelectableElementFragment.newInstance(btnId, btnCreationDate, btnModificationDate, btnName, btnImgPath, btnAudioPath, btnSignal, idContainedInInterfaceBtnIds(btnId));
                    } else {
                        btnListElem = ButtonListSelectableElementFragment.newInstance(btnId, btnCreationDate, btnModificationDate, btnName, btnImgPath, btnAudioPath, btnSignal);
                    }
                    selectedBtnsList.add(btnListElem);
                    fragMgr.beginTransaction().add(R.id.frag_create_interface_selectable_btns_scrollable_linlayout, btnListElem).commit();
                }
            }
            dbMgr.close();
            this.uiConfigured = true;
        } else {
            Logger.Debug(CLASS_ID, "List was already created, not recreating.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putBoolean(UI_CONFIGURED, uiConfigured);
    }

    public int[] getSelectedButtons() {
        ArrayList<Integer> selectedBtnIds = new ArrayList<>();
        for (int i = 0; i < this.selectedBtnsList.size(); i++) {
            if (this.selectedBtnsList.get(i).isSelected()) {
                selectedBtnIds.add(this.selectedBtnsList.get(i).getBtnModelId());
            }
        }
        int[] selectedBtnIdsArray = new int[selectedBtnIds.size()];
        for (int i = 0; i < selectedBtnIds.size(); i++) {
            selectedBtnIdsArray[i] = selectedBtnIds.get(i);
        }
        return selectedBtnIdsArray;
    }

    private boolean idContainedInInterfaceBtnIds(int id) {
        for (int i = 0; i < buttonids.length; i++) {
            if (id == buttonids[i]) {
                return true;
            }
        }
        return false;
    }
}
