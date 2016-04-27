package szum.mthesis.indorpositiontracker;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

/**
 * Created by blazej on 01/03/16.
 *
 * Logger
 */
public class Logger {

    private java.util.logging.Logger mLogger;
    private FileHandler mFileHandler;
    private static Logger mSelf;

    private static List<String> logs = new ArrayList<>();

    private static LogListener logListener;

    private Logger(){

        mLogger = java.util.logging.Logger.getLogger("position_tracker");

        try {
            String path = Environment.getExternalStorageDirectory() + "/position_tracker/";

            File folder = new File(path);
            if (!folder.exists()) {
                folder.mkdir();
            }

            path = path + (new Date()).toString().replace(" ", "_") + ".txt";
            File file = new File(path);
            file.createNewFile();

            mFileHandler = new FileHandler(path);
            mLogger.addHandler(mFileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            mFileHandler.setFormatter(formatter);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Logger self(){
        if( mSelf == null ){
            mSelf = new Logger();
        }
        return mSelf;
    }

    public static void d(String tag, String msg){
        String logmsg = String.format("%-20s : %s", tag, msg);
        self().mLogger.log(Level.INFO,logmsg);
        logToScreen(Level.INFO, logmsg);
    }

    public static void w(String tag, String msg){
        String logmsg = String.format("%-20s : %s", tag, msg);
        self().mLogger.log(Level.WARNING,logmsg);
        logToScreen(Level.WARNING, logmsg);
    }

    public static void e(String tag, String msg){
        String logmsg = String.format("%-20s : %s", tag, msg);
        self().mLogger.log(Level.SEVERE,logmsg);
        logToScreen(Level.SEVERE, logmsg);
    }

    private static void logToScreen(Level level, String logmsg){
        String log = level + ":  " + logmsg;
        logs.add(log);
        if(logListener != null){
            logListener.addNewLog(log);
        }
    }

    public static void registerLogListener(LogListener _logListener){
        logListener = _logListener;
        logListener.setLogs(logs);
    }

    public static void unregisterLogListener(){
        logListener = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public static void clearLog() {
        logs.clear();
        if (logListener != null) {
            logListener.setLogs(logs);
        }
    }

    public interface LogListener{
        void addNewLog(String log);
        void setLogs(List<String> logs);
    }
}
