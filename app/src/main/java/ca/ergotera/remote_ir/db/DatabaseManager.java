package ca.ergotera.remote_ir.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.widget.Toast;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.utils.TimestampUtils;
import ca.ergotera.remote_ir.utils.Utils;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.models.VirtualButton;
import ca.ergotera.remote_ir.models.VirtualInterface;

/**
 * Class containing functions to store, access, modify and delete database
 * entries in the different SQLite tables.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class DatabaseManager extends SQLiteOpenHelper {

    private static final String CLASS_ID = DatabaseManager.class.getSimpleName();

    public static final String DB_NAME = "REMOTE_IR.db";
    public static final String COL_ID = "ID";
    public static final String COL_CREATION_DATE = "CREATION_DATE";
    public static final String COL_MODIF_DATE = "MODIFICATION_DATE";

    // Buttons table
    public static final String BTN_TABLE_NAME = "BUTTONS_TABLE";
    // First column is an autoincrement ID column.
    public static final String COL_BTN_NAME = "BUTTON_NAME";
    public static final String COL_IMAGE_PATH = "IMAGE_PATH";
    public static final String COL_AUDIO_PATH = "AUDIO_PATH";
    public static final String COL_IR_SIGNAL = "IR_SIGNAL";

    // Interfaces table
    public static final String INTERFACE_TABLE_NAME = "INTERFACES_TABLE";
    // First column is an autoincrement ID column.
    public static final String COL_INTERFACE_NAME = "INTERFACE_NAME";
    public static final String COL_INTERFACE_BUTTONS = "INTERFACE_BUTTONS";

    private SQLiteDatabase db;

    private Activity activityContext;

    public DatabaseManager(Context context) {
        super(context, DB_NAME, null, 1);
        activityContext = (Activity) context;
        this.open();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        SQLiteStatement createBtnTableStmt = db.compileStatement("CREATE TABLE "
                + BTN_TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_CREATION_DATE + " TEXT NOT NULL,"
                + COL_MODIF_DATE + " TEXT NOT NULL,"
                + COL_BTN_NAME + " TEXT UNIQUE NOT NULL,"
                + COL_IMAGE_PATH + " TEXT,"
                + COL_AUDIO_PATH + " TEXT CHECK (" + COL_IR_SIGNAL + " IS NOT NULL OR " + COL_AUDIO_PATH + " IS NOT NULL),"
                + COL_IR_SIGNAL + " TEXT CHECK (" + COL_AUDIO_PATH + " IS NOT NULL OR " + COL_IR_SIGNAL + " IS NOT NULL)"
                + ")");
        createBtnTableStmt.execute();

        SQLiteStatement createInterfaceTableStmt = db.compileStatement("CREATE TABLE "
                + INTERFACE_TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_CREATION_DATE + " TEXT NOT NULL,"
                + COL_MODIF_DATE + " TEXT NOT NULL,"
                + COL_INTERFACE_NAME + " TEXT UNIQUE NOT NULL,"
                + COL_INTERFACE_BUTTONS + " TEXT NOT NULL"
                + ")");
        createInterfaceTableStmt.execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BTN_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + INTERFACE_TABLE_NAME);
        onCreate(db);
    }

    /**
     * Opens database stream.
     */
    public void open() {
        db = this.getWritableDatabase();
    }

    /**
     * Closes database stream.
     */
    public void close() {
        if (db != null) {
            db.close();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Virtual buttons's CRUD ----------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    /**
     * Runs the SQL command to add the button to the buttons's table in the database.
     *
     * @param btn virtual button model to add to the buttons database.
     * @throws SQLiteException
     */
    public void addButton(VirtualButton btn) throws SQLiteException {
        try {
            ContentValues cv = new ContentValues();
            String dateTime = TimestampUtils.getISO8601StringForCurrentDate();
            cv.put(COL_CREATION_DATE, dateTime);
            cv.put(COL_MODIF_DATE, dateTime);
            if (btn.getName() != null) {
                if(!btn.getName().equals("")) cv.put(COL_BTN_NAME, btn.getName());
            }
            if (btn.getImagePath() != null) cv.put(COL_IMAGE_PATH, btn.getImagePath().toString());
            if (btn.getAudioPath() != null) cv.put(COL_AUDIO_PATH, btn.getAudioPath().toString());
            if (btn.getSignal() != null) cv.put(COL_IR_SIGNAL, btn.getSignal());
            db.insertOrThrow(BTN_TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            logButtonSQLiteExceptionAndThrow(e);
        }
    }

    /**
     * Runs the SQL command to retrieve the button with the given ID from the buttons's table.
     *
     * @param id of the button to retrieve from the database.
     * @return the retrieved VirtualButton object or null if no button was found.
     */
    public VirtualButton getButton(int id) {
        try {
            Cursor dbButton = this.getEntryWithId(BTN_TABLE_NAME, id);
            if (dbButton == null)
                return null;
            if (!dbButton.moveToFirst())
                return null;
            String creationDate = dbButton.getString(dbButton.getColumnIndexOrThrow(COL_CREATION_DATE));
            String modificationDate = dbButton.getString(dbButton.getColumnIndexOrThrow(COL_MODIF_DATE));
            String name = dbButton.getString(dbButton.getColumnIndexOrThrow(COL_BTN_NAME));
            String imgPath = dbButton.getString(dbButton.getColumnIndexOrThrow(COL_IMAGE_PATH));
            Uri parsedImgPath = null;
            if(imgPath != null)  parsedImgPath = Uri.parse(imgPath);
            String audioPath = dbButton.getString(dbButton.getColumnIndexOrThrow(COL_AUDIO_PATH));
            Uri parsedAudioPath = null;
            if(audioPath != null) parsedAudioPath = Uri.parse(audioPath);
            String signal = dbButton.getString(dbButton.getColumnIndexOrThrow(COL_IR_SIGNAL));
            return new VirtualButton(id, creationDate, modificationDate, name, parsedImgPath, parsedAudioPath, signal);
        } catch (Exception e) {
            Logger.Error(CLASS_ID, e.getMessage());
            Toast.makeText(activityContext, activityContext.getString(R.string.error_database_get_button), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Runs the SQL command to update the button with the given id in the database.
     *
     * @param btn virtual button model to add to the database.
     * @throws SQLiteException
     */
    public void updateButton(VirtualButton btn) throws SQLiteException {
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_MODIF_DATE, TimestampUtils.getISO8601StringForCurrentDate());
            if (btn.getName() != null) {
                if(!btn.getName().equals("")) cv.put(COL_BTN_NAME, btn.getName());
            }
            if (btn.getImagePath() != null) cv.put(COL_IMAGE_PATH, btn.getImagePath().toString());
            if (btn.getAudioPath() != null) cv.put(COL_AUDIO_PATH, btn.getAudioPath().toString());
            if (btn.getSignal() != null) cv.put(COL_IR_SIGNAL, btn.getSignal());
            db.update(BTN_TABLE_NAME, cv, "ID = ?", new String[]{Integer.toString(btn.getId())});
        } catch (SQLiteException e) {
            logButtonSQLiteExceptionAndThrow(e);
        }
    }

    /**
     * Runs the SQL command to delete the button with the given ID from the buttons table.
     *
     * @param id of the button to delete.
     * @return true if succeeded, false otherwise.
     */
    public boolean removeButton(int id) {
        boolean success = this.removeEntryWithId(BTN_TABLE_NAME, id);
        if (success) {
            return true;
        } else {
            Toast.makeText(activityContext, activityContext.getString(R.string.error_database_remove_button), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void logButtonSQLiteExceptionAndThrow(SQLiteException e){
        String errorMsg = e.getMessage().toLowerCase();
        if (errorMsg.contains("unique") && errorMsg.contains("button_name")) {
            Logger.Info(CLASS_ID, "Couldn't add button to database, name is not unique.");
        }
        if (errorMsg.contains("not null")) {
            if (errorMsg.contains("id")) {
                Logger.Info(CLASS_ID, "Couldn't add button to database, id is null.");
            }
            if (errorMsg.contains("creation_date")) {
                Logger.Info(CLASS_ID, "Couldn't add button to database, creation date is null.");
            }
            if (errorMsg.contains("modification_date")) {
                Logger.Info(CLASS_ID, "Couldn't add button to database, modification date is null.");
            }
            if (errorMsg.contains("button_name")) {
                Logger.Info(CLASS_ID, "Couldn't add button to database, name is null.");
            }
        }
        if (errorMsg.contains("check")) {
            Logger.Info(CLASS_ID, "Couldn't add button to database, the button is missing a sound or a signal.");
        }
        // We throw the exception after logging it for additionnal handling.
        throw e;
    }

    // ---------------------------------------------------------------------------------------------
    // Virtual interfaces's CRUD -------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    /**
     * Runs the SQL command to add the interface to the interfaces's table in the database.
     *
     * @param vinterface virtual interface model to add to the database.
     */
    public void addInterface(VirtualInterface vinterface) throws SQLiteException{
        try {
            ContentValues cv = new ContentValues();
            String dateTime = TimestampUtils.getISO8601StringForCurrentDate();
            cv.put(COL_CREATION_DATE, dateTime);
            cv.put(COL_MODIF_DATE, dateTime);
            if (vinterface.getName() != null) {
                if(!vinterface.getName().equals("")) cv.put(COL_INTERFACE_NAME, vinterface.getName());
            }
            if (vinterface.getButtonIds() != null && vinterface.getButtonIds().length > 0)
                cv.put(COL_INTERFACE_BUTTONS, Utils.convertIntArrayToString(vinterface.getButtonIds()));
            db.insertOrThrow(INTERFACE_TABLE_NAME, null, cv);
        } catch (SQLiteException e) {
            logInterfaceSQLiteExceptionAndThrow(e);
        }
    }

    /**
     * Runs the SQL command to retrieve the interface with the given ID from the interfaces's table.
     * The returned object from the database doesn't fit the VirtualInterface's definition, so the
     * function also transforms the database model into the expected VirtualInterface object model.
     *
     * @param id of the interface to retrieve from the database.
     * @return the retrieved VirtualInterface object or null if no interface was found.
     */
    public VirtualInterface getInterface(int id) {
        try {
            Cursor dbInterface = this.getEntryWithId(INTERFACE_TABLE_NAME, id);
            if (dbInterface == null)
                return null;
            if (!dbInterface.moveToFirst()) //this will move the cursor to the first element
                return null;
            String creationDate = dbInterface.getString(dbInterface.getColumnIndexOrThrow(COL_CREATION_DATE));
            String modificationDate = dbInterface.getString(dbInterface.getColumnIndexOrThrow(COL_MODIF_DATE));
            String name = dbInterface.getString(dbInterface.getColumnIndexOrThrow(COL_INTERFACE_NAME));
            String strBtnIds = dbInterface.getString(dbInterface.getColumnIndexOrThrow(COL_INTERFACE_BUTTONS));
            return new VirtualInterface(id, creationDate, modificationDate, name, Utils.convertStrArrayToIntArray(strBtnIds));
        } catch (Exception e) {
            Logger.Error(CLASS_ID, e.getMessage());
            Toast.makeText(activityContext, activityContext.getString(R.string.error_database_get_interface), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Runs the SQL command to update the interface with the given id in the database.
     *
     * @param vinterface virtual interface model to add to the database.
     * @throws SQLiteException
     */
    public void updateInterface(VirtualInterface vinterface) throws SQLiteException{
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_MODIF_DATE, TimestampUtils.getISO8601StringForCurrentDate());
            if (vinterface.getName() != null) {
                if(!vinterface.getName().equals("")) cv.put(COL_INTERFACE_NAME, vinterface.getName());
            }
            if (vinterface.getButtonIds().equals("")) vinterface.setButtonIds(null);
            if (vinterface.getButtonIds() != null) cv.put(COL_INTERFACE_BUTTONS, Utils.convertIntArrayToString(vinterface.getButtonIds()));
            db.update(INTERFACE_TABLE_NAME, cv, "ID = ?", new String[]{Integer.toString(vinterface.getId())});
        } catch (SQLiteException e) {
            logInterfaceSQLiteExceptionAndThrow(e);
        }
    }

    /**
     * Runs the SQL command to delete the interface with the given ID from the interfaces table.
     *
     * @param id of the interface to delete.
     * @return true if succeeded, false otherwise.
     */
    public boolean removeInterface(int id) {
        boolean success = this.removeEntryWithId(INTERFACE_TABLE_NAME, id);
        if (success) {
            return true;
        } else {
            Toast.makeText(activityContext, activityContext.getString(R.string.error_database_remove_interface), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public Cursor getAllDataFromTable(String tableName) {
        Cursor response = db.rawQuery("SELECT * FROM " + tableName, null);
        return response;
    }

    private void logInterfaceSQLiteExceptionAndThrow(SQLiteException e){
        String errorMsg = e.getMessage().toLowerCase();
        if (errorMsg.contains("unique") && errorMsg.contains("interface_name")) {
            Logger.Info(CLASS_ID, "Couldn't add interface to database, name is not unique.");
        }
        if (errorMsg.contains("not null")) {
            if (errorMsg.contains("id")) {
                Logger.Info(CLASS_ID, "Couldn't add interface to database, id is null.");
            }
            if (errorMsg.contains("creation_date")) {
                Logger.Info(CLASS_ID, "Couldn't add interface to database, creation date is null.");
            }
            if (errorMsg.contains("modification_date")) {
                Logger.Info(CLASS_ID, "Couldn't add interface to database, modification date is null.");
            }
            if (errorMsg.contains("interface_name")) {
                Logger.Info(CLASS_ID, "Couldn't add interface to database, name is null.");
            }
            if(errorMsg.contains("interface_buttons")) {
                Logger.Info(CLASS_ID, "Couldn't add interface to database, no interface buttons.");
            }
        }
        // We throw the exception after logging it for additional handling.
        throw e;
    }

    // ---------------------------------------------------------------------------------------------
    // Utility functions ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the entry from the given table with the given ID.
     *
     * @param table to retrieve the element from.
     * @param id    to retrieve.
     * @return Cursor of the retrieved database object.
     */
    private Cursor getEntryWithId(String table, int id) {
        try {
            Cursor response = db.rawQuery("SELECT * FROM " + table + " WHERE " + COL_ID + " = "
                    + "?", new String[]{Integer.toString(id)});
            return response;
        } catch (Exception e) {
            Logger.Error(CLASS_ID, e.getMessage());
            return null;
        }
    }

    /**
     * General form of the remove with ID function.
     *
     * @param table table targeted by the removal.
     * @param id    targeted by the removal.
     * @return true if succeeded, false otherwise.
     */
    private boolean removeEntryWithId(String table, int id) {
        try {
            SQLiteStatement delStmt = db.compileStatement("DELETE FROM " + table + " WHERE " + COL_ID + " = " + id);
            delStmt.execute();
            return true;
        } catch (Exception e) {
            Logger.Error(CLASS_ID, e.getMessage());
            return false;
        }
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
