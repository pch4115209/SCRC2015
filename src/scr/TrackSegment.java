package scr;

/**
 * A particular section of track, which is used as part of the track segmentation code.
 */
public class TrackSegment {

	private double distance;
	private int type;
	// 0 straight
	// 1 right
	// 2 left
	private double start;

	//Discrete value between 1-10
	private int sharpness;

	public TrackSegment() {
		// System.out.println("Seg created, no values");
	}

	public TrackSegment (double distance, double start, int type) {
		this.distance = distance;
		this.type = type;
		this.start = start;
		this.sharpness = 5;
		// System.out.println("Created TrackSegment size: " + distance + " type: " + type);
	}

	public double getDistance() {
		return this.distance;
	}

	public double getStart() {
		return this.start;
	}

	public float getSpeed() {
		if (type == 1 || type ==2) {
			return 400;
		} else {
			return 100;
		}
	}
	public int getType() {
		return type;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public void setSharpness(int sharpness) {
		this.sharpness = sharpness;
	}

    @Override
    public String toString() {
        String string = "start=" + start + System.getProperty("line.separator");
        string += "distance=" + distance + System.getProperty("line.separator");
        string += "type=" + type + System.getProperty("line.separator");
        string += "sharpness=" + sharpness + System.getProperty("line.separator");
        return string;
    }

}