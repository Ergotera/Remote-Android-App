package ca.ergotera.remote_ir.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import ca.ergotera.remote_ir.utils.TimestampUtils;

/**
 * Class representing a virtual button used in interfaces. These buttons
 * contains the information needed to create functionnal virtual buttons
 * which will be activated either by touch or by the external (physical)
 * buttons.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class VirtualButton implements Parcelable {

    public static final String BTN_ID = "ID";
    public static final String BTN_CREATION_DATE = "BTN_CREATION_DATE";
    public static final String BTN_MODIFICATION_DATE = "BTN_MODIFICATION_DATE";
    public static final String BTN_NAME = "BTN_NAME";
    public static final String BTN_IMG_PATH = "BTN_IMG_PATH";
    public static final String BTN_AUDIO_PATH = "BTN_AUDIO_PATH";
    public static final String BTN_SIGNAL = "BTN_SIGNAL";

    public static final int NO_ID = -1;

    private int id;
    private String creationDate;
    private String modificationDate;
    private String name;
    private Uri imagePath;
    private Uri audioPath;
    private String signal;

    public VirtualButton(int id, String creationDate, String modificationDate, String name, Uri imgPath, Uri audioPath, String signal) {
        this.id = id;
        this.creationDate = creationDate;
        this.modificationDate = modificationDate;
        this.name = name;
        this.imagePath = imgPath;
        this.audioPath = audioPath;
        this.signal = signal;
    }

    public VirtualButton() {
        this.id = NO_ID;
        this.creationDate = TimestampUtils.getISO8601StringForCurrentDate();
        this.modificationDate = creationDate;
    }

    private VirtualButton(Parcel in) {
        this.id = in.readInt();
        this.creationDate = in.readString();
        this.modificationDate = in.readString();
        this.name = in.readString();
        this.imagePath = in.readParcelable(Uri.class.getClassLoader());
        this.audioPath = in.readParcelable(Uri.class.getClassLoader());
        this.signal = in.readString();
    }

    // Parcelable implementation ===================================================================
    // See https://developer.android.com/reference/android/os/Parcelable for details.
    @Override
    public int describeContents() {
        return 0; // See https://stackoverflow.com/a/4779032 or  for details.
    }

    public static final Parcelable.Creator<VirtualButton> CREATOR = new Parcelable.Creator<VirtualButton>() {

        @Override
        public VirtualButton createFromParcel(Parcel in) {
            return new VirtualButton(in);
        }

        @Override
        public VirtualButton[] newArray(int size) {
            return new VirtualButton[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.id);
        out.writeString(this.creationDate);
        out.writeString(this.modificationDate);
        out.writeString(this.name);
        out.writeParcelable(this.imagePath, flags);
        out.writeParcelable(this.audioPath, flags);
        out.writeString(this.signal);
    }
    // End of: Parcelable implementation ===========================================================

    /**
     * Checks whether or not the button is valid.
     *
     * @return true if valid, false otherwise.
     */
    public boolean isValid() {
        return name != null && (audioPath != null || (signal != null && signal.length() > 0));
    }

    public int getId() {
        return this.id;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public String getName() {
        return this.name;
    }

    public Uri getImagePath() {
        return this.imagePath;
    }

    public Uri getAudioPath() {
        return this.audioPath;
    }

    public String getSignal() {
        return this.signal;
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

    public void setImagePath(Uri imagePath) {
        this.imagePath = imagePath;
    }

    public void setAudioPath(Uri audioPath) {
        this.audioPath = audioPath;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }
}
