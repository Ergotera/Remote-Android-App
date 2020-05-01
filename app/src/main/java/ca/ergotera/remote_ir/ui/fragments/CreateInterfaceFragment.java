package ca.ergotera.remote_ir.ui.fragments;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.db.DatabaseManager;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.models.VirtualButton;
import ca.ergotera.remote_ir.models.VirtualInterface;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

/**
 * Fragment (UI and behaviour) allowing users to create a virtual interface.
 * <p>
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class CreateInterfaceFragment extends android.app.Fragment {

    private static final String CLASS_ID = CreateInterfaceFragment.class.getSimpleName();

    private static final String INTERFACE_MODEL = "interfaceModel";
    private static final String UI_CONFIGURED = "uiConfigured";

    private static final String INTERFACE_ID = "ID";

    private View view;

    private LinearLayout scrollLinLayout;
    private EditText interfaceNameEditText;
    private TextView selectButtonsTitle;
    private Button saveInterfaceBtn;
    private Button deleteInterfaceBtn;

    private SelectableButtonListFragment selectableBtnList;

    private VirtualInterface interfaceModel;

    private boolean uiConfigured = false;
    private boolean isNewInterface = true;

    public static android.app.Fragment newInstance(int interfaceId) {
        CreateInterfaceFragment frag = new CreateInterfaceFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(INTERFACE_ID, interfaceId);
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
        view = inflater.inflate(R.layout.frag_create_interface, container, false);
        if (savedInstanceState != null) {
            // Read values from the "savedInstanceState"-object and put them in your textview
            this.interfaceModel = (VirtualInterface) savedInstanceState.getParcelable(INTERFACE_MODEL);
            this.isNewInterface = false;
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int id = VirtualInterface.NO_ID;
            id = getArguments().getInt(INTERFACE_ID);
            if (id != VirtualButton.NO_ID) {
                // If the fragment was instantiated with an id, we must load a button model from
                // the database to modify it instead of creating a new button.
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                this.interfaceModel = dbMgr.getInterface(id);
                dbMgr.close();
                // If we successfully retrieve the button model with the given id from the database,
                // we proceed to change the UI to a 'modification' menu.
                if (this.interfaceModel != null) {
                    this.isNewInterface = false;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_database_get_interface), Toast.LENGTH_SHORT).show();
                    Logger.Error(CLASS_ID, "The interface doesn't exist.");
                }
            }
        } else {
            interfaceModel = new VirtualInterface();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putBoolean(UI_CONFIGURED, uiConfigured);
        outState.putParcelable(INTERFACE_MODEL, this.interfaceModel);
    }

    @Override
    public void onStart() {
        super.onStart();

        initializeComponents();

        if (!this.uiConfigured) {
            android.app.FragmentManager fragMgr = getFragmentManager();
            this.selectableBtnList = new SelectableButtonListFragment().newInstance(this.interfaceModel);
            fragMgr.beginTransaction().add(R.id.frag_create_interface_selectable_btns_scrollable_linlayout, this.selectableBtnList).commit();
            this.uiConfigured = true;
        } else {
            Logger.Debug(CLASS_ID, "Selectable buttons list was already created, not recreating.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getToolbar().setTitle(getResources().getString(R.string.create_interface));
    }

    private void initializeComponents() {
        findComponents();
        setupComponents();
    }

    private void findComponents() {
        this.scrollLinLayout = (LinearLayout) view.findViewById(R.id.frag_create_interface_scrollable_linlayout);
        this.interfaceNameEditText = (EditText) view.findViewById(R.id.frag_create_interface_interface_name_input);
        this.selectButtonsTitle = (TextView) view.findViewById(R.id.frag_create_interface_select_btns_title);
        this.saveInterfaceBtn = (Button) view.findViewById(R.id.frag_create_interface_save_btn);
    }

    private void setupComponents() {
        setupInterfaceNameEditTextChangeListener();

        if (this.isNewInterface) {
            setupSaveButtonCreateClickListener();
        } else {
            setupSaveButtonSaveModifsClickListener();
        }

        setupDeleteInterfaceButtonClickListener();
    }

    private void setupInterfaceNameEditTextChangeListener() {

        if(isNewInterface) {
            interfaceModel.setName(interfaceNameEditText.getText().toString());
        } else {
            this.interfaceNameEditText.setText(interfaceModel.getName());
        }

        this.interfaceNameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                interfaceModel.setName(interfaceNameEditText.getText().toString());

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isNameEmpty()) {
                    interfaceNameEditText.setError(getResources().getString(R.string.error_validation_required_field));
                    saveInterfaceBtn.setEnabled(false);
                } else {
                    saveInterfaceBtn.setEnabled(true);
                }
                interfaceModel.setName(interfaceNameEditText.getText().toString());
            }
        });
    }

    private void setupSaveButtonCreateClickListener() {
        this.saveInterfaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                interfaceModel.setButtonIds(selectableBtnList.getSelectedButtons());
                try {
                    dbMgr.addInterface(interfaceModel);
                    Toast.makeText(getActivity(), getResources().getString(R.string.interface_successfully_created), Toast.LENGTH_SHORT).show();
                    android.app.FragmentManager fragMgr = getFragmentManager();
                    fragMgr.beginTransaction().replace(R.id.content_frame, new InterfaceListFragment()).addToBackStack(null).commit();
                } catch(SQLiteException e) {
                    handleSQLiteDatabaseException(e);
                } finally {
                    dbMgr.close();
                }
            }
        });
    }

    private void setupSaveButtonSaveModifsClickListener() {
        this.saveInterfaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                interfaceModel.setButtonIds(selectableBtnList.getSelectedButtons());
                try {
                    dbMgr.updateInterface(interfaceModel);
                    Toast.makeText(getActivity(), getResources().getString(R.string.modifications_saved), Toast.LENGTH_SHORT).show();
                    android.app.FragmentManager fragMgr = getFragmentManager();
                    fragMgr.beginTransaction().replace(R.id.content_frame, new InterfaceListFragment()).addToBackStack(null).commit();
                } catch (SQLiteException e) {
                    handleSQLiteDatabaseException(e);
                } finally {
                    dbMgr.close();
                }
            }
        });
    }

    /**
     * Handles database errors, informs user on the application
     * status and why his operation failed.
     *
     * @param e exception to be handled.
     */
    private void handleSQLiteDatabaseException(SQLiteException e){
        String errorMsg = e.getMessage().toLowerCase();
        if (errorMsg.contains("unique") && errorMsg.contains("interface_name")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_database_name_not_unique), Toast.LENGTH_SHORT).show();
            this.interfaceNameEditText.setError(getResources().getString(R.string.error_database_name_not_unique));
        }
        if (errorMsg.contains("not null")) {
            if (errorMsg.contains("id")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_add_interface), Toast.LENGTH_SHORT).show();
            }
            if (errorMsg.contains("creation_date")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_add_interface), Toast.LENGTH_SHORT).show();
            }
            if (errorMsg.contains("modification_date")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_add_interface), Toast.LENGTH_SHORT).show();
            }
            if (errorMsg.contains("interface_name")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_name_not_null), Toast.LENGTH_SHORT).show();
                this.interfaceNameEditText.setError(getResources().getString(R.string.error_database_name_not_null));
            }
            if(errorMsg.contains("interface_buttons")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_empty_btn_list), Toast.LENGTH_SHORT).show();
                this.selectButtonsTitle.setError(getResources().getString(R.string.error_database_empty_btn_list));
            }
        }
    }

    /**
     * Configures the style of the delete button (which is created completely programmatically).
     */
    private void setupDeleteInterfaceButton() {
        deleteInterfaceBtn = new Button(getActivity());
        deleteInterfaceBtn.setTextSize(24);
        deleteInterfaceBtn.setTextColor(getResources().getColor(R.color.colorLight));
        deleteInterfaceBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_menu_bin), null, null, null);
        deleteInterfaceBtn.setAllCaps(false);
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            deleteInterfaceBtn.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        }
        deleteInterfaceBtn.setText(getResources().getString(R.string.delete_interface));
    }

    /**
     * Sets up the click listener of the delete button.
     */
    private void setupDeleteInterfaceButtonClickListener() {

        if (!this.isNewInterface && this.deleteInterfaceBtn == null) {
            setupDeleteInterfaceButton();
            this.interfaceNameEditText.setText(interfaceModel.getName());
            // Apply UI transformations to fit update menu instead of creation menu.
            if (deleteInterfaceBtn.getParent() == null) {
                scrollLinLayout.addView(deleteInterfaceBtn);
            }
            this.saveInterfaceBtn.setText(getResources().getString(R.string.save_modifications));

            deleteInterfaceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeletionValidationDialog(view);
                }
            });
        }
    }

    /**
     * Displays a validation dialog for interface deletion.
     *
     * @param view
     */
    public void showDeletionValidationDialog(View view) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.delete_interface));
        builder.setMessage(getResources().getString(R.string.delete_interface_confirmation));

        // add the buttons
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                if (dbMgr.removeInterface(interfaceModel.getId())) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.interface_deleted), Toast.LENGTH_SHORT).show();
                    android.app.FragmentManager fragMgr = getFragmentManager();
                    fragMgr.beginTransaction().replace(R.id.content_frame, new InterfaceListFragment()).addToBackStack(null).commit();
                } else {
                    Logger.Error(CLASS_ID, "An error occured while trying to delete the interface of the database.");
                }
                dbMgr.close();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Returns whether or not the name is empty.
     *
     * @return true if empty, false otherwise.
     */
    private boolean isNameEmpty() {
        return this.interfaceNameEditText.getText().toString().length() == 0;
    }
}
