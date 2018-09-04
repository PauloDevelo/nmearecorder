package arbutus.nmea.sentences;

//$PGRME Estimated Position Error
//Garmin proprietary sentence

//$PGRME,x.x,M,x.x,M,x.x,M*hh
//       1   2 3   4 5   6 7
//1) Estimated horizontal position error (HPE)
//2) Unit, metres
//3) Estimated vertical error (VPE)
//4) Unit, metres
//5) Overall spherical equivalent position error
//6) Unit, metres
//7) Checksum

public class PGRME extends NMEASentence {
	public static final String sticker = "$PGRME";
	
	@nmea(pos=1)
	private float estimatedHPEInMeters = Float.NaN;
	@nmea(pos=3)
	private float estimatedVPEInMeters = Float.NaN;
	@nmea(pos=5)
	private float overallSphericalErrorInMeter = Float.NaN;
	
	public PGRME(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return Estimated horizontal position error (HPE)
	 */
	public float getEstimatedHPEInMeters() {
		return estimatedHPEInMeters;
	}

	/**
	 * @return Estimated vertical error (VPE)
	 */
	public float getEstimatedVPEInMeters() {
		return estimatedVPEInMeters;
	}

	/**
	 * @return Overall spherical equivalent position error
	 */
	public float getOverallSphericalErrorInMeter() {
		return overallSphericalErrorInMeter;
	}
}
