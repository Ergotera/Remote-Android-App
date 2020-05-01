package ca.ergotera.remote_ir.ui.fragments;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.db.DatabaseManager;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.models.VirtualButton;
import ca.ergotera.remote_ir.models.VirtualInterface;
import ca.ergotera.remote_ir.serial_com.CommandListener;
import ca.ergotera.remote_ir.serial_com.CommandManager;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

import static ca.ergotera.remote_ir.models.VirtualInterface.INTERFACE_ID;

/**
 * Fragment (UI and behaviour) allowing users use their virtual remote
 * with external buttons and / or using the touchscreen.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class ActionModeFragment extends android.app.Fragment implements CommandListener, Observer {

    private static final String CLASS_ID = ActionModeFragment.class.getSimpleName();
    private static final String UI_CONFIGURED = "UI_CONFIGURED";
    private static final String INTERFACE_MODEL = "INTERFACE_MODEL";
    private static final String CURSOR_POSITION = "CURSOR_POSITION";
    private static final String NUMBER_OF_BUTTONS = "NUMBER_OF_BUTTONS";
    private static final String VIEW_BUTTONS = "VIEW_BUTTONS";

    private static final int NO_ID = -1;

    private MediaPlayer mp;

    private View view;

    private LinearLayout container;

    private boolean uiConfigured = false;
    private VirtualInterface interfaceModel;

    private ArrayList<View> buttons;
    private int cursorPos = 0;
    private int numberOfButtons;

    public static android.app.Fragment newInstance(int interfaceId) {
        ActionModeFragment frag = new ActionModeFragment();
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
            this.cursorPos = savedInstanceState.getInt(CURSOR_POSITION);
            this.numberOfButtons = savedInstanceState.getInt(NUMBER_OF_BUTTONS);
        } else {
            Logger.Debug(CLASS_ID, "Saved instance is null.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_action_mode, container, false);
        if (savedInstanceState != null) {
            // Read values from the "savedInstanceState"-object and put them in your textview
            this.interfaceModel = (VirtualInterface) savedInstanceState.getParcelable(INTERFACE_MODEL);
            ArrayList<View> UIButtons = createInterfaceButtons(this.interfaceModel);
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int id = NO_ID;
            id = getArguments().getInt(INTERFACE_ID);
            if (id != NO_ID) {
                DatabaseManager dbMgr = new DatabaseManager(getActivity());
                this.interfaceModel = dbMgr.getInterface(id);
                dbMgr.close();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_database_get_button), Toast.LENGTH_SHORT).show();
                Logger.Error(CLASS_ID, "The button doesn't exist.");
            }
        } else {
            Logger.Error(CLASS_ID, "Cannot instantiate ActionModeFragment without an" +
                    " instance of a virtual interface, returning to interfaces list.");
            android.app.FragmentManager fragMgr = getFragmentManager();
            fragMgr.popBackStack();
            fragMgr.beginTransaction().replace(R.id.content_frame, new InterfaceListFragment()).addToBackStack(null).commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        CommandManager cmdMgr = CommandManager.getInstance();
        cmdMgr.addObserver(this);
        if(cmdMgr.isDeviceConnectedAndReady()) {
            Logger.Debug(CLASS_ID, "Device is connected and ready, setting fragment as command listener and setting device to button state.");
            cmdMgr.setCommandListener(this);
            cmdMgr.set_buttonState();
        } else {
            Logger.Debug(CLASS_ID, "Device is not connected.");
        }
        initializeComponents();
        moveCursorToNext(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        // The fragment listens to the command manager to be notified of changes
        // to the USB service (ie.: device connected / disconnected).
        boolean isDeviceConnected = (boolean) arg;
        CommandManager cmdMgr = CommandManager.getInstance();
        if(isDeviceConnected) {
            Logger.Debug(CLASS_ID, "Device is connected and ready, setting fragment as command listener and setting device to button state.");
            cmdMgr.set_buttonState();
            CommandManager.getInstance().setCommandListener(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getToolbar().setTitle(getResources().getString(R.string.action_mode));
    }

    @Override
    public void external_button_pressed(int button_id) {
        Toast.makeText(getActivity(), "Received button: " + button_id,Toast.LENGTH_SHORT).show();
        Logger.Info(CLASS_ID, "Received 'External button pressed': " + button_id);
        if(button_id == 1) {
            moveCursorToNext(false);
        } else {
            buttons.get(cursorPos).callOnClick();
        }
    }

    @Override
    public void recorded_IR_code(String code_data) {}

    @Override
    public void handle_error(String error) {}

    @Override
    public void ping_response(String ping) {

    }

    @Override
    public void state_set_success(String current_state) {

    }

    @Override
    public void send_IR_success(String sent_ir) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save the values you need from your textview into "outState"-object
        super.onSaveInstanceState(outState);
        outState.putBoolean(UI_CONFIGURED, uiConfigured);
        outState.putParcelable(INTERFACE_MODEL, this.interfaceModel);
        outState.putInt(CURSOR_POSITION, cursorPos);
        outState.putInt(NUMBER_OF_BUTTONS, numberOfButtons);
    }


    private void initializeComponents() {
        findComponents();
        setupComponents();
    }

    private void findComponents() {
        container = (LinearLayout) view.findViewById(R.id.frag_action_mode_container);
    }

    private void setupComponents() {
        this.buttons = createInterfaceButtons(this.interfaceModel);
        // Build the layout
        float screenRatio = getScreenRatio();
        double numberOfRowNotRounded = Math.sqrt(this.buttons.size());
        int numberOfRowRounded = (int) Math.floor(numberOfRowNotRounded);
        int maxBtnPerRow = this.buttons.size() / numberOfRowRounded;
        float positionningRatio;
        if(numberOfRowNotRounded > numberOfRowRounded) {
            positionningRatio = (maxBtnPerRow + 1) / numberOfRowRounded;
        } else {
            positionningRatio = maxBtnPerRow / numberOfRowRounded;
        }
        int curBtn = 0;
        int btnSize;
        if (positionningRatio > screenRatio) {
            // Button layout ratio is wider than screen ratio, limiting by width.
            if(numberOfRowNotRounded > numberOfRowRounded) {
                btnSize = getScreenWidth() / (maxBtnPerRow + 1);
            } else {
                btnSize = getScreenWidth() / maxBtnPerRow;
            }
        } else {
            btnSize = getScreenHeight() / numberOfRowRounded - getActivity().findViewById(R.id.toolbar).getHeight();
        }
        for (int r = 0; r < numberOfRowRounded; r++) {
            LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0,0,0,0);

            for (int rb = 0; rb < maxBtnPerRow; rb++) {
                if (curBtn > this.buttons.size() - 1) {
                    break;
                } else {
                    this.buttons.get(curBtn).setLayoutParams(new LinearLayout.LayoutParams(btnSize, btnSize));
                    row.addView(this.buttons.get(curBtn));
                }
                curBtn++;
            }
            // Add remaining
            if(r == numberOfRowRounded - 1) {
                for(curBtn = curBtn; curBtn < this.buttons.size(); curBtn++) {
                    this.buttons.get(curBtn).setLayoutParams(new LinearLayout.LayoutParams(btnSize, btnSize));
                    row.addView(this.buttons.get(curBtn));
                }
            }
            container.addView(row);
        }
    }

    /**
     * Creates a list of UI buttons from the button IDs of the given interface Model.
     *
     * @return a list of Views containing both Buttons and ImageButtons.
     */
    private ArrayList<View> createInterfaceButtons(VirtualInterface interfaceModel) {
        ArrayList<View> viButtons = new ArrayList<>();
        int[] viBtnIds = interfaceModel.getButtonIds();
        DatabaseManager dbMgr = new DatabaseManager(getActivity());

        for (int b = 0; b < viBtnIds.length; b++) {
            VirtualButton btnModel = dbMgr.getButton(viBtnIds[b]);
            if (btnModel.getImagePath() == null) {
                viButtons.add(this.createButtonWithModel(btnModel));
            } else {
                viButtons.add(this.createImageButtonWithModel(btnModel));
            }
        }
        dbMgr.close();

        return viButtons;
    }

    private ImageButton createImageButtonWithModel(final VirtualButton btnModel) {
        ImageButton btn = new ImageButton(getActivity());
        btn.setImageURI(btnModel.getImagePath());
        btn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        btn.setAdjustViewBounds(true);
        btn.setBackgroundColor(getResources().getColor(R.color.white));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(btnModel);
                sendRecordedInfraredCommand(btnModel);
            }
        });
        return btn;
    }

    private Button createButtonWithModel(final VirtualButton btnModel) {
        Button btn = new Button(getActivity());
        btn.setText(btnModel.getName());
        btn.setBackgroundColor(getResources().getColor(R.color.white));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(btnModel);
                sendRecordedInfraredCommand(btnModel);
            }
        });
        return btn;
    }

    /**
     * Plays the selected button sound.
     */
    private void playSound(VirtualButton btnModel) {
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

    private void stopSound() {
        if (mp != null && mp.isPlaying()) {
            mp.stop();
        }
    }

    /**
     * Sends the recorded infrared IR command.
     */
    private boolean sendRecordedInfraredCommand(VirtualButton btnModel) {
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
     * Moves the cursor to the next button.
     */
    private void moveCursorToNext(boolean isInitialization){
        if(!isInitialization) {
            // Sets old current style back to normal.
            buttons.get(cursorPos).setBackgroundColor(getResources().getColor(R.color.white));
            if(android.os.Build.VERSION.SDK_INT >= 21 && buttons.get(cursorPos) instanceof ImageButton) {
                ((ImageButton)buttons.get(cursorPos)).setImageTintMode(PorterDuff.Mode.MULTIPLY);
                ((ImageButton)buttons.get(cursorPos)).setImageTintList(getResources().getColorStateList(R.color.white));
            }

            // Moves cursor to next location.
            cursorPos++;
            if(cursorPos > buttons.size() - 1) {
                cursorPos = 0;
            }
        }

        // Highlights the new selection.
        buttons.get(cursorPos).setBackgroundColor(getResources().getColor(R.color.colorSecondary));
        if(android.os.Build.VERSION.SDK_INT >= 21 && buttons.get(cursorPos) instanceof ImageButton) {
            ((ImageButton)buttons.get(cursorPos)).setImageTintMode(PorterDuff.Mode.MULTIPLY);
            ((ImageButton)buttons.get(cursorPos)).setImageTintList(getResources().getColorStateList(R.color.colorSecondary));
        }
    }

    /**
     * Returns the screen ratio given by screen width divided by screen height.
     *
     * @return screen ratio.
     */
    private float getScreenRatio() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return (float)width / ((float)height - getActivity().findViewById(R.id.toolbar).getHeight());
    }

    /**
     * Returns the width of the screen.
     *
     * @return width of the screen.
     */
    private int getScreenWidth() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    /**
     * Returns the width of the screen.
     *
     * @return width of the screen.
     */
    private int getScreenHeight() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}
