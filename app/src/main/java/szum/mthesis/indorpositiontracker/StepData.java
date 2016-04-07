package szum.mthesis.indorpositiontracker;

/**
 * Created by blazej on 17.02.16.
 */
public class StepData {

    private long mStepTime;
    private int mStepOrientation;
    private long mStepDuration = 500; //[ms]
    private double mStepLength = 0;
    private int mStepOrientationCalc = 0;

    public StepData(long stepTime, int mStepOrientation) {
        this.mStepTime = stepTime;
        this.mStepOrientation = mStepOrientation;
    }

    public long getmStepTime() {
        return mStepTime;
    }

    public void setmStepTime(long stepTime) {
        this.mStepTime = stepTime;
    }

    public int getmStepOrientation() {
        return mStepOrientation;
    }

    public void setmStepOrientation(int mStepOrientation) {
        this.mStepOrientation = mStepOrientation;
    }

    public int getmStepOrientationCalc() {
        return mStepOrientationCalc;
    }

    public void setmStepOrientationCalc(int mStepOrientationCalc) {
        this.mStepOrientationCalc = mStepOrientationCalc;
    }

    @Override
    public String toString() {
        return "StepData{" +
                "mStepTime=" + mStepTime +
                ", mStepOrientation=" + mStepOrientation +
                ", mStepDuration=" + mStepDuration +
                ", mStepLength=" + mStepLength +
                ", mStepOrientationCalc=" + mStepOrientationCalc +
                '}';
    }

    public void setmStepLength(double mStepLength) {
        this.mStepLength = mStepLength;
    }

    public double getmStepLength() {
        return mStepLength;
    }

    public long getmStepDuration() {
        return mStepDuration;
    }

    public void setmStepDuration(long mStepDuration) {
        this.mStepDuration = mStepDuration;
    }
}
