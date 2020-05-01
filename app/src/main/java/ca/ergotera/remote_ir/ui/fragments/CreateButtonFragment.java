package ca.ergotera.remote_ir.ui.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.db.DatabaseManager;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.models.VirtualButton;
import ca.ergotera.remote_ir.serial_com.CommandListener;
import ca.ergotera.remote_ir.serial_com.CommandManager;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

/**
 * Fragment (UI and behaviour) allowing users to create a virtual button.
 * <p>
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class CreateButtonFragment extends android.app.Fragment implements CommandListener, Observer {

    private static String CLASS_ID = CreateButtonFragment.class.getSimpleName();

    private static final String BTN_MODEL = "BTN_MODEL";
    private static final String BTN_ID = "ID";

    public static final int SELECT_IMAGE = 1;
    public static final int SELECT_SOUND = 2;

    private View view;

    // Media related
    private MediaPlayer mp;

    // Button to be displayed
    private boolean isNewButton = true;
    private VirtualButton btnModel;

    // UI elements
    private LinearLayout scrollLinLayout;
    private EditText buttonNameEditText;
    private Button previewButton;
    private ImageButton previewImgButton;
    private Button selectImageBtn;
    private TextView commandTitle;
    private Button selectSoundBtn;
    private Button recordIRBtn;
    private Button saveButtonBtn;
    private Button deleteButtonBtn;

    public static CreateButtonFragment newInstance(int btnId) {
        CreateButtonFragment frag = new CreateButtonFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(BTN_ID, btnId);
        frag.setArguments(bundle);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_create_button, container, false);
        if (savedInstanceState != null) {
            // Read values from the "savedInstanceState"-object and put them in your textview
            this.btnModel = (VirtualButton) savedInstanceState.getParcelable(BTN_MODEL);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putParcelable(BTN_MODEL, this.btnModel);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            int id = VirtualButton.NO_ID;
            id = getArguments().getInt(BTN_ID);
            if (id != VirtualButton.NO_ID) {
                // If the fragment was instantiated with an id, we must load a button model from
                // the database to modify it instead of creating a new button.
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                this.btnModel = dbMgr.getButton(id);
                dbMgr.close();
                // If we successfully retrieve the button model with the given id from the database,
                // we proceed to change the UI to a 'modification' menu.
                if (this.btnModel != null) {
                    isNewButton = false;
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_database_get_button), Toast.LENGTH_SHORT).show();
                    Logger.Error(CLASS_ID, "The button doesn't exist.");
                }
            }
        } else {
            this.btnModel = new VirtualButton();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        CommandManager cmdMgr = CommandManager.getInstance();
        cmdMgr.addObserver(this);
        if(cmdMgr.isDeviceConnectedAndReady()) {
            cmdMgr.setCommandListener(this);
        }
        initializeComponents();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)getActivity()).getToolbar().setTitle(getResources().getString(R.string.create_button));
    }

    @Override
    public void update(Observable o, Object arg) {
        // The fragment listens to the command manager to be notified of changes
        // to the USB service (ie.: device connected / disconnected).
        boolean isDeviceConnected = (boolean) arg;
        recordIRBtn.setEnabled(isDeviceConnected);
        if(isDeviceConnected) {
            CommandManager.getInstance().setCommandListener(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE && data != null) {
            this.btnModel.setImagePath(data.getData());
            this.switchPreviewBtnToImgBtn();
            Toast.makeText(getActivity(), getResources().getString(R.string.image_selected), Toast.LENGTH_SHORT).show();
        } else if (requestCode == SELECT_SOUND && data != null) {
            this.btnModel.setAudioPath(data.getData());
            Toast.makeText(getActivity(), getResources().getString(R.string.sound_selected), Toast.LENGTH_SHORT).show();
            this.commandTitle.setError(null);
        }
    }

    @Override
    public void external_button_pressed(int button_id) {

    }

    @Override
    public void recorded_IR_code(String code_data) {
        this.btnModel.setSignal(code_data);
        Toast.makeText(getActivity(), getResources().getString(R.string.command_recorded), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handle_error(String error) {
        //TODO: Handle error
        Toast.makeText(getActivity(), "An error occured in Serial COM", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void ping_response(String ping) {

    }

    @Override
    public void state_set_success(String current_state) {
        Logger.Debug(CLASS_ID, "Current state received in button creation menu: " + current_state);
        if (current_state.contains("REC_STATE")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.proceed_to_recording), Toast.LENGTH_SHORT).show();
            this.commandTitle.setError(null);
        }
    }

    @Override
    public void send_IR_success(String sent_ir) {
        Logger.Debug(CLASS_ID, "Successfully sent command '" + sent_ir + "'.");
    }

    /**
     * Template method to retrieve and setup UI components.
     */
    private void initializeComponents() {
        findComponents();
        setupComponents();
    }

    /**
     * Retrieves UI elements from view that needs to be accessed here.
     */
    private void findComponents() {
        this.scrollLinLayout = (LinearLayout) getActivity().findViewById(R.id.frag_create_button_scrollable_linlayout);
        this.buttonNameEditText = (EditText) getActivity().findViewById(R.id.frag_create_button_btn_name_input);
        this.previewButton = (Button) getActivity().findViewById(R.id.frag_create_button_preview_txt_btn);
        this.previewImgButton = (ImageButton) getActivity().findViewById(R.id.frag_create_button_preview_img_btn);
        this.selectImageBtn = (Button) getActivity().findViewById(R.id.frag_create_button_select_img_btn);
        this.commandTitle = (TextView) getActivity().findViewById(R.id.frag_create_button_btn_command_title);
        this.selectSoundBtn = (Button) getActivity().findViewById(R.id.frag_create_button_select_audio_btn);
        this.recordIRBtn = (Button) getActivity().findViewById(R.id.frag_create_button_record_btn);
        this.saveButtonBtn = (Button) getActivity().findViewById(R.id.frag_create_button_save_btn);
    }

    /**
     * Template method which executes functions to configure the UI.
     */
    private void setupComponents() {
        this.setupPreviewBtnClickListener();
        this.setupButtonNameEditTextChangeListener();
        this.setupSelectImgBtnClickListener();
        this.setupSelectSoundBtnClickListener();
        this.setupRecordCommandBtnClickListener();

        //We use a single button for both creating a button or saving modifications to an existing button.
        // Therefore, the action of the button depends on whether we are modifying or creating a button.
        if (this.isNewButton) {
            this.setupSaveBtnCreateClickListener();
        } else {
            setupSaveBtnSaveModifsClickListener();
            if (this.btnModel.getImagePath() != null) {
                this.switchPreviewBtnToImgBtn();
            }
        }

        this.setupDeleteButtonBtnClickListener();
    }

    /**
     * Sets the click listener of the Preview (text) Button.
     */
    private void setupPreviewBtnClickListener() {
        this.previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound();
                sendRecordedInfraredCommand();
            }
        });
    }

    /**
     * Sets the click listener of the Preview (image) Button.
     */
    private void setupPreviewImgBtnClickListener() {
        this.previewImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound();
                sendRecordedInfraredCommand();
            }
        });
    }

    /**
     * Sets the change listener of the button name text edit.
     */
    private void setupButtonNameEditTextChangeListener() {

        if(isNewButton) {
            btnModel.setName(buttonNameEditText.getText().toString());
        } else {
            this.buttonNameEditText.setText(btnModel.getName());
        }

        this.buttonNameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isNameEmpty()) {
                    buttonNameEditText.setError(getResources().getString(R.string.error_validation_required_field));
                    saveButtonBtn.setEnabled(false);
                } else {
                    saveButtonBtn.setEnabled(true);
                }
                previewButton.setText(buttonNameEditText.getText().toString());
                btnModel.setName(buttonNameEditText.getText().toString());
            }
        });
    }

    /**
     * Sets the click listener of the image selection button.
     */
    private void setupSelectImgBtnClickListener() {
        this.selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionReadExternalStorageGranted()){
                    selectImage();
                } else {
                    if(android.os.Build.VERSION.SDK_INT >= 23) {
                        // No explanation needed; request the permission
                        // READ_EXTERNAL_STORAGE_PERMISSION_REQUEST is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                        requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MainActivity.READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
                    }
                }
            }
        });
    }

    /**
     * Checks if permissions for accessing external storage are granted.
     */
    private boolean isPermissionReadExternalStorageGranted() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Logger.Debug(CLASS_ID, "Permission already granted.");
            return true;
        }
        if(android.os.Build.VERSION.SDK_INT < 23) {
            Logger.Error(CLASS_ID, "PERMISSION ISSUE; Read external storage is denied access." +
                    "Those permissions should be granted on installation for devices with SDKs below 23.");
            Toast.makeText(getActivity(), getResources().getString(R.string.permission_warning_cant_display_images), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case MainActivity.READ_EXTERNAL_STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logger.Debug(CLASS_ID, "Permission granted.");
                    selectImage();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.permission_warning_cant_display_images), Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * Select image intent, opens choice to pick which selection tool app to use and allows for
     * image selection.
     */
    private void selectImage() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, getResources().getString(R.string.select_image));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(chooserIntent, SELECT_IMAGE);
    }

    /**
     * Sets up the click listener of the sound selection button.
     */
    private void setupSelectSoundBtnClickListener() {
        this.selectSoundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("audio/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("audio/*");

                Intent chooserIntent = Intent.createChooser(getIntent, getResources().getString(R.string.select_sound));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, SELECT_SOUND);
            }
        });
    }

    /**
     * Sets up the record IR button.
     */
    private void setupRecordCommandBtnClickListener() {
        recordIRBtn.setEnabled(false);
        CommandManager cmdMgr = CommandManager.getInstance();
        if(cmdMgr.isDeviceConnectedAndReady()) {
            recordIRBtn.setEnabled(true);
            cmdMgr.setCommandListener(this);
        }
        this.recordIRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRecordingCommand();
            }
        });
    }

    /**
     * Sets up the save button for button creation.
     */
    private void setupSaveBtnCreateClickListener() {
        this.saveButtonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                try {
                    dbMgr.addButton(btnModel);
                    dbMgr.close();
                    stopSound();
                    Toast.makeText(getActivity(), getResources().getString(R.string.button_successfully_created), Toast.LENGTH_SHORT).show();
                    CommandManager.getInstance().setCommandListener(null);
                    android.app.FragmentManager fragMgr = getFragmentManager();
                    fragMgr.beginTransaction().replace(R.id.content_frame, new ButtonListFragment()).addToBackStack(null).commit();
                } catch (SQLiteException e) {
                    handleSQLiteDatabaseException(e);
                } finally {
                    dbMgr.close();
                }
            }
        });
    }

    /**
     * Sets up the save button for button modification.
     */
    private void setupSaveBtnSaveModifsClickListener() {
        this.saveButtonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                try {
                    dbMgr.updateButton(btnModel);
                    dbMgr.close();
                    stopSound();
                    Toast.makeText(getActivity(), getResources().getString(R.string.modifications_saved), Toast.LENGTH_SHORT).show();
                    CommandManager.getInstance().setCommandListener(null);
                    android.app.FragmentManager fragMgr = getFragmentManager();
                    fragMgr.beginTransaction().replace(R.id.content_frame, new ButtonListFragment()).addToBackStack(null).commit();
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
        if (errorMsg.contains("unique") && errorMsg.contains("button_name")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_database_name_not_unique), Toast.LENGTH_SHORT).show();
            this.buttonNameEditText.setError(getResources().getString(R.string.error_database_name_not_unique));
        }
        if (errorMsg.contains("not null")) {
            if (errorMsg.contains("id")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_add_button), Toast.LENGTH_SHORT).show();
            }
            if (errorMsg.contains("creation_date")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_add_button), Toast.LENGTH_SHORT).show();
            }
            if (errorMsg.contains("modification_date")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_add_button), Toast.LENGTH_SHORT).show();
            }
            if (errorMsg.contains("button_name")) {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_name_not_null), Toast.LENGTH_SHORT).show();
                this.buttonNameEditText.setError(getResources().getString(R.string.error_database_name_not_null));
            }
        }
        if (errorMsg.contains("check")) {
            this.commandTitle.setError("Required field.");
            Toast.makeText(getActivity(), getResources().getString(R.string.error_database_sound_and_signal_null), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Configures the style of the delete button (which is created completely programmatically).
     */
    private void setupDeleteButtonBtn() {
        deleteButtonBtn = new Button(getActivity());
        deleteButtonBtn.setTextSize(24);
        deleteButtonBtn.setTextColor(getResources().getColor(R.color.colorLight));
        deleteButtonBtn.setAllCaps(false);
        deleteButtonBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_menu_bin), null, null, null);
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            deleteButtonBtn.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        }
        deleteButtonBtn.setText(getResources().getString(R.string.delete_button));
    }

    /**
     * Sets up the click listener of the delete button.
     */
    private void setupDeleteButtonBtnClickListener() {

        if (!this.isNewButton && this.deleteButtonBtn == null) {
            setupDeleteButtonBtn();
            this.buttonNameEditText.setText(btnModel.getName());
            // Apply UI transformations to fit update menu instead of creation menu.
            if (deleteButtonBtn.getParent() == null) {
                scrollLinLayout.addView(deleteButtonBtn);
            }
            this.saveButtonBtn.setText(getResources().getString(R.string.save_modifications));

            deleteButtonBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeletionValidationDialog(view);
                }
            });
        }
    }

    /**
     * Displays a validation dialog for button deletion.
     *
     * @param view
     */
    public void showDeletionValidationDialog(View view) {

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.delete_button));
        builder.setMessage(getResources().getString(R.string.delete_button_confirmation));

        // add the buttons
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                if (dbMgr.removeButton(btnModel.getId())) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.button_deleted), Toast.LENGTH_SHORT).show();
                    android.app.FragmentManager fragMgr = getFragmentManager();
                    fragMgr.beginTransaction().replace(R.id.content_frame, new ButtonListFragment()).addToBackStack(null).commit();
                } else {
                    Logger.Error(CLASS_ID, "An error occured while trying to delete the button of the database.");
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
     * Plays the selected button sound.
     */
    private void playSound() {
        if (btnModel.getAudioPath() != null) {

            stopSound();

            mp = new MediaPlayer();
            try {
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setDataSource(getActivity().getApplicationContext(), btnModel.getAudioPath());
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                Logger.Error(CLASS_ID, e.getMessage());
            }
        } else {
            Logger.Error(CLASS_ID, "No sound selected, nothing to play.");
        }
    }

    private void stopSound(){
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
    }

    /**
     * Sends the recorded infrared IR command.
     */
    private boolean sendRecordedInfraredCommand() {
        CommandManager cmdMgr = CommandManager.getInstance();
        if (cmdMgr.getUsbService() != null) {
            if (btnModel.getId() != VirtualButton.NO_ID) {
                if (btnModel.getSignal() != null) {
                    cmdMgr.sendCode(btnModel.getSignal());
                    return true;
                } else {
                    Logger.Error(CLASS_ID, "No recorded infrared signal to send.");
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_serialcom_please_plug_in_device), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Sends the command to record an infrared command.
     */
    private boolean sendRecordingCommand() {
        CommandManager cmdMgr = CommandManager.getInstance();
        if (cmdMgr.getUsbService() != null) {
            cmdMgr.set_recordState();
            return true;
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.error_serialcom_please_plug_in_device), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Replaces the preview (text) button by the preview imagebutton.
     */
    private void switchPreviewBtnToImgBtn() {
        if (previewImgButton != null) {
            setupPreviewImgBtnClickListener();
            setupPreviewImgBtnClickListener();
            this.previewImgButton.setImageURI(btnModel.getImagePath());
            this.previewImgButton.setVisibility(View.VISIBLE);
            this.previewImgButton.setPadding(20, 20, 20, 20);
            this.previewImgButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            this.previewButton.setVisibility(View.GONE);
        } else {
            Logger.Error(CLASS_ID, "Unable to switch between preview button and preview imagebutton; The latter is null.");
        }
    }

    /**
     * Returns whether or not the name is empty.
     *
     * @return true if empty, false otherwise.
     */
    private boolean isNameEmpty() {
        return this.buttonNameEditText.getText().toString().length() == 0;
    }
}
