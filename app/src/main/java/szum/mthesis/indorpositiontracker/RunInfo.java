package szum.mthesis.indorpositiontracker;

public class RunInfo {
	
	private long date;
	private long id;
	private double initLatitude;
	private double initLongitude;
	private int stepsCount;
	private long walkTime;
	private double walkDistance;
	private int rotationCorrection;
	private float avgAccuracy;

	public RunInfo(long id, long date, double initLongitude, double initLatitude, int stepsCount,long walkTime, double walkDistance, int rotationCorrection, float avgAccuracy){
		super();
		this.id = id;
		this.date = date;
		this.initLatitude = initLatitude;
		this.initLongitude = initLongitude;
		this.stepsCount = stepsCount;
		this.walkTime = walkTime;
		this.walkDistance = walkDistance;
		this.rotationCorrection = rotationCorrection;
		this.avgAccuracy = avgAccuracy;
	}

	public long getDate() {
		return date;
	}

	public long getId() {
		return id;
	}

	public double getInitLongitude() {
		return initLongitude;
	}

	public double getInitLatitude() {
		return initLatitude;
	}

	public int getStepsCount() {
		return stepsCount;
	}

	public long getWalkTime() {
		return walkTime;
	}

	public double getWalkDistance() {
		return walkDistance;
	}

	public double getRotationCorrection() {
		return rotationCorrection;
	}

	public float getAvgAccuracy() {
		return avgAccuracy;
	}
}
