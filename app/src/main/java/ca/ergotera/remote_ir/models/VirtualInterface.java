package ca.ergotera.remote_ir.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import ca.ergotera.remote_ir.db.DatabaseManager;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.utils.TimestampUtils;

/**
 * Class representing a virtual interface used to combine buttons
 * and build virtual remotes.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class VirtualInterface implements Parcelable {

    private static final String CLASS_ID = VirtualInterface.class.getSimpleName();

    public static final String INTERFACE_ID = "INTERFACE_ID";
    public static final String INTERFACE_CREATION_DATE = "INTERFACE_CREATION_DATE";
    public static final String INTERFACE_MODIFICATION_DATE = "INTERFACE_MODIFICATION_DATE";
    public static final String INTERFACE_NAME = "INTERFACE_NAME";
    public static final String INTERFACE_BUTTONS = "INTERFACE_BUTTONS";

    public static final int NO_ID = -1;

    private int id;
    private String creationDate;
    private String modificationDate;
    private String name;
    private int[] buttonIds;

    public VirtualInterface(int id, String creationDate, String modificationDate, String name, int[] buttonIds) {
        this.id = id;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.name = name;
        this.buttonIds = buttonIds;
    }

    public VirtualInterface() {
        this.id = NO_ID;
        this.creationDate = TimestampUtils.getISO8601StringForCurrentDate();
        this.modificationDate = creationDate;
        this.buttonIds = new int[0];
    }


    protected VirtualInterface(Parcel in) {
        this.id = in.readInt();
        this.creationDate = in.readString();
        this.modificationDate = in.readString();
        this.name = in.readString();
        this.buttonIds = in.createIntArray();
    }

    // Parcelable implementation ===================================================================
    // See https://developer.android.com/reference/android/os/Parcelable for details.
    @Override
    public int describeContents() {
        return 0; // See https://stackoverflow.com/a/4779032 or  for details.
    }

    public static final Creator<VirtualInterface> CREATOR = new Creator<VirtualInterface>() {

        @Override
        public VirtualInterface createFromParcel(Parcel in) {
            return new VirtualInterface(in);
        }

        @Override
        public VirtualInterface[] newArray(int size) {
            return new VirtualInterface[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(creationDate);
        out.writeString(modificationDate);
        out.writeString(name);
        out.writeIntArray(buttonIds);
    }
    // End of: Parcelable implementation ===========================================================

    /**
     * Returns an array of buttons retrieved from the database using the interface's button IDs.
     *
     * @param context context of execution.
     * @return the array of VirtualButtons contained within the interface.
     */
    public VirtualButton[] getButtons(Context context) {
        if (buttonIds != null && buttonIds.length > 0) {
            VirtualButton[] btns = new VirtualButton[buttonIds.length];
            DatabaseManager dbMgr = new DatabaseManager(context);
            for (int i = 0; i < buttonIds.length; i++) {
                VirtualButton btn = dbMgr.getButton(buttonIds[i]);
                if (btn != null) {
                    btns[i] = btn;
                } else
                    Logger.Error(CLASS_ID, "Unable to find button with id '" + buttonIds[i] + "'.");
            }
            return btns;
        } else {
            return null;
        }
    }

    public int getId() {
        return id;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public String getName() {
        return name;
    }

    public int[] getButtonIds() {
        return buttonIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setButtonIds(int[] buttonIds) {
        this.buttonIds = buttonIds;
    }
}
