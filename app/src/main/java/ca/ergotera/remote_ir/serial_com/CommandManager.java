package ca.ergotera.remote_ir.serial_com;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.felhr.services.UsbService;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.ui.activities.MainActivity;

/**
 * Class that handles USB serial communication, parses commands and simplifies
 * communication between the device and the Android device.
 *
 * @author Renaud Varin (renaud.varin.1@ens.etsmtl.ca)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class CommandManager extends Observable {

    private static final String CLASS_ID = CommandManager.class.getSimpleName();
    private static final CommandManager ourInstance = new CommandManager();

    // CONSTANTS ===================================================================================

    private static final String CMD_SET_STATE_IDLE = "SET_STATE {STATE:IDL_STATE}";
    private static final String CMD_SET_STATE_BUTTON = "SET_STATE {STATE:BUT_STATE}";
    private static final String CMD_SET_STATE_RECORD = "SET_STATE {STATE:REC_STATE}";
    private static final String CMD_PING = "PING {}";
    private static final String CMD_SEND_CODE = "SEND_IR ";

    // USB Service =================================================================================
    private boolean deviceConnected = false;
    private UsbService usbService;
    private UsbHandler mHandler;

    // HANDLE TO MainActivity ======================================================================
    private MainActivity mainActivity;

    // SINGLETON ===================================================================================

    // Private constructor: class cannot be instantiated
    private CommandManager() {
    }

    public static CommandManager getInstance() {
        return ourInstance;
    }

    @Override
    public void addObserver(Observer o){
        super.addObserver(o);
        Logger.Info(CLASS_ID, "Added observer: " + o.getClass().getSimpleName() + ".");
    }

    public void setActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mHandler = new UsbHandler();
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            usbService = ((UsbService.UsbBinder) service).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            usbService = null;
        }
    };

    public void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(mainActivity, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            mainActivity.startService(startService);
        }
        Intent bindingIntent = new Intent(mainActivity, service);
        mainActivity.bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        mainActivity.registerReceiver(mUsbReceiver, filter);
    }

    // Broadcast received from device indicating state changes.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Logger.Debug(CLASS_ID, "Broadcast received");

            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, mainActivity.getResources().getString(R.string.usb_ready), Toast.LENGTH_SHORT).show();
                    Logger.Debug(CLASS_ID, "USB Ready");
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, mainActivity.getResources().getString(R.string.usb_permission_not_granted), Toast.LENGTH_SHORT).show();
                    Logger.Debug(CLASS_ID, "USB Permission not granted");
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, mainActivity.getResources().getString(R.string.usb_not_connected), Toast.LENGTH_SHORT).show();
                    Logger.Debug(CLASS_ID, "No USB connected");
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, mainActivity.getResources().getString(R.string.usb_disconnected), Toast.LENGTH_SHORT).show();
                    Logger.Debug(CLASS_ID, "USB disconnected");
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, mainActivity.getResources().getString(R.string.usb_device_not_supported), Toast.LENGTH_SHORT).show();
                    Logger.Debug(CLASS_ID, "USB device not supported");
                    break;
            }

            if(intent.getAction() == UsbService.ACTION_USB_PERMISSION_GRANTED) {
                deviceConnected = true;
            } else {
                deviceConnected = false;
                mHandler.setCommandListener(null);
            }
            Logger.Info(CLASS_ID, "Notifying Command Manager Observers, Device connected: " + deviceConnected + ".");
            notifyDeviceConnectionState(deviceConnected);
        }
    };

    public void set_idleState() {
        usbService.write(CMD_SET_STATE_IDLE.getBytes());
    }

    public void set_buttonState() {
        usbService.write(CMD_SET_STATE_BUTTON.getBytes());
    }

    public void set_recordState() {
        usbService.write(CMD_SET_STATE_RECORD.getBytes());
    }

    public void ping() {
        usbService.write(CMD_PING.getBytes());
    }

    public void sendCode(String data) {
        // data is expected to be : "{codeLen:32,codeType:3,codeValue:16689239}"
        String s = CMD_SEND_CODE + data;
        usbService.write(s.getBytes());
        Logger.Debug(CLASS_ID, "Serial from Android: " + new String(data));
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class UsbHandler extends Handler {

        private static final String CLASS_ID = UsbHandler.class.getSimpleName();

        public CommandListener commandListener;

        private void parseResponse(String response) {
            Logger.Debug(CLASS_ID, "Serial from module: " + response.toString());
            Logger.Debug(CLASS_ID, "Current command listener: " + commandListener + ".");

            // Makes the parser non-case sensitive.
            String lowerCaseResponse = response.toLowerCase();

            if(commandListener != null) {
                if (!lowerCaseResponse.contains("error")) {
                    if (lowerCaseResponse.contains("recorded {")) {
                        commandListener.recorded_IR_code(response.substring(9));
                    } else if (lowerCaseResponse.contains("ping {current_state:")) {
                        commandListener.ping_response(response.substring(5));
                    } else if (lowerCaseResponse.contains("btn_pressed {")) {
                        String s = lowerCaseResponse.substring(13, 14);
                        commandListener.external_button_pressed(Integer.parseInt(s));
                    } else if (lowerCaseResponse.contains("set_state {succ:")) {
                        commandListener.state_set_success(response.substring(10));
                    } else if(lowerCaseResponse.contains("sent ")) {
                        commandListener.send_IR_success(response.substring(5));
                    } else {
                        Logger.Warn(CLASS_ID, "Received unknown response from module: '" + response + "'.");
                    }
                } else {
                    commandListener.handle_error(response);
                }
            } else {
                Logger.Info(CLASS_ID, "No command listener, not parsing response.");
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    parseResponse(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Logger.Debug(CLASS_ID, "CTS_CHANGE");
                    break;
                case UsbService.DSR_CHANGE:
                    Logger.Debug(CLASS_ID, "DSR_CHANGE");
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    break;
            }
        }

        public void setCommandListener(CommandListener commandListener) {
            Logger.Debug(CLASS_ID, "Set Command Listener to " + this.commandListener + ".");
            this.commandListener = commandListener;
        }
    }

    public UsbService getUsbService() {
        return usbService;
    }

    public ServiceConnection getServiceConnection() {
        return usbConnection;
    }

    public BroadcastReceiver getBroadcastReceiver() {
        return mUsbReceiver;
    }

    /**
     * Sets a new value for the command listener.
     *
     * @param listener new command listener.
     */
    public void setCommandListener(CommandListener listener) {
        mHandler.setCommandListener(listener);
    }

    /**
     * Notifies observers of the state of the device's connection.
     *
     * @param isDeviceConnected whether or not a device is connected.
     */
    private void notifyDeviceConnectionState(boolean isDeviceConnected) {
        setChanged();
        notifyObservers(isDeviceConnected);
    }

    public boolean isDeviceConnectedAndReady(){
        return this.deviceConnected;
    }
}
