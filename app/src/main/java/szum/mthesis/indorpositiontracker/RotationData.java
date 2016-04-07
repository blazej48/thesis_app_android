package szum.mthesis.indorpositiontracker;

/**
 * Created by blazej on 02/03/16.
 */
public class RotationData {

    private float yaw, pitch, roll;
    private long time;

    public RotationData(float[] vals, long time) {
        this.time = time;
        this.yaw = vals[0];
        this.pitch = vals[1];
        this.roll = vals[2];
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public float getRoll() {
        return roll;
    }

    public long getTime() {
        return time;
    }
}
