package szum.mthesis.indorpositiontracker.orm;

import com.orm.SugarRecord;

/**
 * Created by blazej on 25/04/16.
 */
public class BleSample extends SugarRecord {

    long pathId;
    String deviceName;
    long timestamp;
    int signalStrength;

    public BleSample() {}

    public BleSample(String deviceName, long timestamp, int signalStrength) {
        this.deviceName = deviceName;
        this.timestamp = timestamp;
        this.signalStrength = signalStrength;
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }
}

