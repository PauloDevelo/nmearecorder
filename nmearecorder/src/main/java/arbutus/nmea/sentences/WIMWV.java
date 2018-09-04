package arbutus.nmea.sentences;

//MWV Wind Speed and Angle
//$--MWV,x.x,a,x.x,a*hh
//       1   2 3   4 5
//1) Wind Angle, 0 to 360 degrees
//2) Reference, R = Relative, T = True
//3) Wind Speed
//4) Wind Speed Units, K/M/N
//5) Status, A = Data Valid
//6) Checksum

public class WIMWV extends NMEASentence {
	public static final String sticker = "$WIMWV";
	
	private boolean isTrue = false;
	
	@nmea(pos=1)
	private float windAngle = Float.NaN;
	
	private float windSpeedKn = Float.NaN;
	
	@nmea(pos=5)
	private Status status;
	
	public WIMWV(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence,this);
	}

	/**
	 * @return the windAngle
	 */
	public float getWindAngle() {
		return windAngle;
	}

	/**
	 * @return the windSpeedKn
	 */
	public float getWindSpeedKn() {
		return windSpeedKn;
	}
	
	public boolean isTrue() {
		return isTrue;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	@Override
	protected void initSpecialFields(String[] nmeaFields) {
		if(!nmeaFields[2].isEmpty()) {
			if(nmeaFields[2].toUpperCase().charAt(0) == 'T') {
				isTrue = true;
			}
		}
		
		if(!nmeaFields[3].isEmpty() && !nmeaFields[4].isEmpty()) {
			char windSpeedUnit = nmeaFields[4].toUpperCase().charAt(0);
			switch (windSpeedUnit) {
			case 'K':
				windSpeedKn = (float) (Float.parseFloat(nmeaFields[3]) * 0.539957);
				break;
			case 'M':
				windSpeedKn = (float) (Float.parseFloat(nmeaFields[3]) * 0.868976);
				break;
			case 'N':
				windSpeedKn = Float.parseFloat(nmeaFields[3]);
				break;
			default:
				break;
			}
		}
	}
	
}
