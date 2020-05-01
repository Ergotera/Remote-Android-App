package ca.ergotera.remote_ir.misc;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;

/**
 * Custom logger allowing logging into logcat and log file on device,
 * providing a way to debug even when devices are not connected to a dev's
 * computer. The log files could also be sent to us in case clients or distant
 * costumers have issues with the product.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class Logger {

    private static final String CLASS_ID = Logger.class.getSimpleName();

    public static final int DEBUG_LEVEL = 0;
    public static final int INFO_LEVEL = 1;
    public static final int WARN_LEVEL = 2;
    public static final int ERROR_LEVEL = 3;
    public static final int FATAL_LEVEL = 4;

    private static final String DEBUG = "DEBUG";
    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
    private static final String ERROR = "ERROR";
    private static final String FATAL = "FATAL";

    private static int minLoggingLevel = DEBUG_LEVEL;

    private static final String DEFAULT_LOG_FILE_NAME = "App.log";

    private static String logFileName = DEFAULT_LOG_FILE_NAME;

    //Singleton implementation
    private static final Logger instance = new Logger();

    // Private constructor: class cannot be instantiated
    private Logger() {}

    private static Context context;

    /**
     * Debug logging function which will output both in log file and in logcat.
     * Will only log if minimum logging level is set to debug or higher.
     *
     * @param classId classId given for the classId.
     * @param text    text of the log.
     */
    public static void Debug(String classId, String text) {
        if (minLoggingLevel <= DEBUG_LEVEL) {
            BaseLevelLog(DEBUG, classId, text);
            Log.d(classId, text);
        }
    }

    /**
     * Info logging function which will output both in log file and in logcat.
     * Will only log if minimum logging level is set to info or higher.
     *
     * @param classId classId given for the classId.
     * @param text    text of the log.
     */
    public static void Info(String classId, String text) {
        if (minLoggingLevel <= INFO_LEVEL) {
            BaseLevelLog(INFO, classId, text);
            Log.i(classId, text);
        }
    }

    /**
     * Warning logging function which will output both in log file and in logcat.
     * Will only log if minimum logging level is set to warning or higher.
     *
     * @param classId classId given for the classId.
     * @param text    text of the log.
     */
    public static void Warn(String classId, String text) {
        if (minLoggingLevel <= WARN_LEVEL) {
            BaseLevelLog(WARN, classId, text);
            Log.w(classId, text);
        }
    }

    /**
     * Error logging function which will output both in log file and in logcat.
     * Will only log if minimum logging level is set to error or higher.
     *
     * @param classId classId given for the classId.
     * @param text    text of the log.
     */
    public static void Error(String classId, String text) {
        if (minLoggingLevel <= ERROR_LEVEL) {
            BaseLevelLog(ERROR, classId, text);
            Log.e(classId, text);
        }
    }

    /**
     * Fatal logging function which will output both in log file and in logcat.
     * Will only log if minimum logging level is set to fatal.
     *
     * @param classId classId given for the classId.
     * @param text    text of the log.
     */
    public static void Fatal(String classId, String text) {
        if (minLoggingLevel <= FATAL_LEVEL) {
            BaseLevelLog(FATAL, classId, text);
            Log.wtf(classId, text);
        }
    }

    /**
     * General logging function used in concrete, public logging functions.
     *
     * @param type    type of the logging.
     * @param classId classId given for the classId.
     * @param text    text of the log.
     */
    private static void BaseLevelLog(String type, String classId, String text) {
        BaseLog("[" + new Timestamp(getTruncatedSystemMillis()) + "] [" + type + "] " + classId + ": " + text);
    }

    /**
     * Base logging function for logging text into log file.
     *
     * @param text text to append to the log file.
     */
    private static void BaseLog(String text) {
        if (context != null) {
            File logFile = new File(context.getFilesDir(), logFileName);

            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                //BufferedWriter for performance, true to set append to file flag
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(text);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Logger.Error(CLASS_ID, "Logger has no context, set it before using the Logger.");
        }
    }

    /**
     * Generates a header for an app execution including some information about the
     * context of execution of the app and information about the device.
     */
    private static void generateAppExecutionHeader() {
        BaseLog(generateTextLineSeparator(100,'='));
        BaseLog("Device: " + android.os.Build.DEVICE);
        BaseLog("Model: " + android.os.Build.MODEL);
        BaseLog("Product: " + android.os.Build.PRODUCT);
        BaseLog("Kernel Version: " + System.getProperty("os.version"));
        BaseLog("SDK Version (API level): " + android.os.Build.VERSION.SDK_INT);
        BaseLog("Time of launch: " + new Timestamp(System.currentTimeMillis()));
        BaseLog(generateTextLineSeparator(100,'='));
    }

    /**
     * Generates text line separator of the given length with the given character.
     *
     * @param length length of the line.
     * @param lineChar character to create the line with.
     * @return String of character of the given length.
     */
    private static String generateTextLineSeparator(int length, char lineChar){
        String line = "";
        for(int i = 0 ; i < length ; i++) {
            line += lineChar;
        }
        return line;
    }

    /**
     * Returns the log file name.
     *
     * @return log file name.
     */
    private String getLogFileName() {
        return logFileName;
    }

    /**
     * Returns the minimum logging level of the logger.
     *
     * @return minimum logging level.
     */
    private int getMinLoggingLevel() {
        return minLoggingLevel;
    }

    /**
     * Truncates the milliseconds from the System's current time to millis for cleaner logging.
     *
     * @return system current time with milliseconds truncated.
     */
    private static long getTruncatedSystemMillis() {
        return 1000 * (System.currentTimeMillis() / 1000);
    }

    /**
     * Sets the context of the logger.
     *
     * @param c new context of the logger.
     */
    public static void setContext(Context c) {
        context = c;
        generateAppExecutionHeader();
    }

    /**
     * Sets the new log file name. Filename should contain extension .log or no extension.
     *
     * @param fileName new filename of the log file.
     */
    private void setLogFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (extension.equals(".log")) {
            logFileName = fileName;
        } else {
            logFileName = fileName + ".log";
        }
    }

    /**
     * Sets a new value to the minimum logging level. The logging level defines which
     * type of logging is the lowest one to be logged.
     * <p>
     * For example, having a minimal logging level set to error will only log error
     * and fatal logs.
     *
     * @param mll minimal logging level.
     */
    public void setMinLoggingLevel(int mll) {
        minLoggingLevel = mll;
    }
}
