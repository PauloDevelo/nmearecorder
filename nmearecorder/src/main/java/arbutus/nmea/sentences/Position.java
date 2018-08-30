package arbutus.nmea.sentences;

public class Position {
	private float latitude = Float.NaN;
	private float longitude = Float.NaN;

	public Position(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the latitude
	 */
	public float getLatitude() {
		return latitude;
	}

	/**
	 * @return the longitude
	 */
	public float getLongitude() {
		return longitude;
	}
	
	
}
