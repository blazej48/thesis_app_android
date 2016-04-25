package szum.mthesis.indorpositiontracker.entities;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

/**
 * Created by blazej on 25/04/16.
 */
public class Step extends SugarRecord {

    long pathId;
    long stepTime;
    int orientation;

    @Ignore long mStepDuration = 500; //[ms]
    @Ignore double mStepLength = 0;

    public Step(){}

    public Step(long time, int orientation) {
        this.stepTime = time;
        this.orientation = orientation;
    }

    public List<Step> getPathSteps(long pathId) {
        return find(Step.class, "path_id = ?", String.valueOf(pathId));
    }

    public long getStepTime() {
        return stepTime;
    }

    public void setStepTime(long time) {
        this.stepTime = time;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public long getmStepDuration() {
        return mStepDuration;
    }

    public void setmStepDuration(long mStepDuration) {
        this.mStepDuration = mStepDuration;
    }

    public double getmStepLength() {
        return mStepLength;
    }

    public void setmStepLength(double mStepLength) {
        this.mStepLength = mStepLength;
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }

    @Override
    public String toString() {
        return "Step{" +
                "stepTime=" + stepTime +
                ", orientation=" + orientation +
                ", mStepDuration=" + mStepDuration +
                ", mStepLength=" + mStepLength +
                '}';
    }
}
