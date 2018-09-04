package arbutus.nmea.sentences;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

//1) Time (UTC)
//193134.00230818

public abstract class NMEASentence {
	private static Logger log = Logger.getLogger(NMEASentence.class);
	
	static private final DateFormat df = new SimpleDateFormat("HHmmssddMMyyX");
	
	static synchronized Date parseUTCDate(String date, String time) throws ParseException {
		return df.parse(time.substring(0, 6) + date + "-00");
	}
	
	static protected float parseLatitude(String lat, String hemisphere) {
		float latitudeDegDec = Float.parseFloat(lat.substring(0, 2)) + Float.parseFloat(lat.substring(2)) / (float)60;
		if (hemisphere.compareTo("S") == 0) {
			latitudeDegDec = -latitudeDegDec;
		}
		
		return latitudeDegDec;
	}
	
	static protected float parseLongitude(String longi, String sign) {
		float longitudeDegDec = Float.parseFloat(longi.substring(0, 3)) + Float.parseFloat(longi.substring(3)) / (float)60;
		if (sign.compareTo("W") == 0) {
			longitudeDegDec = -longitudeDegDec;
		}
		
		return longitudeDegDec;
	}
	
	static protected <T extends NMEASentence> void parseNMEASentence(StringBuilder sentence, T typedThis) {
		String[] nmeaFields = sentence.toString().split(",");
		
		Field[] fields = typedThis.getClass().getDeclaredFields();
		
		for(Field field : fields) {
			nmea annotation = field.getAnnotation(nmea.class);
			
			if(annotation != null) {
				field.setAccessible(true);
				
				try {
					if (field.getType() == String.class) {
						field.set(typedThis, nmeaFields[annotation.pos()]);
					}
					else if(field.getType() == Float.TYPE)
					{
						if(!nmeaFields[annotation.pos()].isEmpty()) {
							field.setFloat(typedThis, Float.parseFloat(nmeaFields[annotation.pos()]));
						}
						else {
							field.setFloat(typedThis, Float.NaN);
						}
					}
					else if(field.getType() == Integer.TYPE)
					{
						if(!nmeaFields[annotation.pos()].isEmpty()) {
							field.setInt(typedThis, Integer.parseInt(nmeaFields[annotation.pos()]));
						}
					}
					else if(field.getType() == Position.class) {
						int position = annotation.pos();
						
						float latitude = Float.NaN;
						float longitude = Float.NaN;
						if(!nmeaFields[position].isEmpty() && !nmeaFields[position + 1].isEmpty()) {
							latitude = NMEASentence.parseLatitude(nmeaFields[position++], nmeaFields[position++]);
						}
						
						if(!nmeaFields[position].isEmpty() && !nmeaFields[position + 1].isEmpty()) {
							longitude = NMEASentence.parseLongitude(nmeaFields[position++], nmeaFields[position++]);
						}
						
						field.set(typedThis, new Position(latitude, longitude));
					}
					else if(field.getType() == Status.class) {
						if(!nmeaFields[annotation.pos()].isEmpty()) {
							field.set(typedThis, Status.valueOf(nmeaFields[annotation.pos()].charAt(0)));
						}
						else {
							field.set(typedThis, Status.DataInvalid);
						}
					}
					else if(field.getType() == GPSQuality.class) {
						if(!nmeaFields[annotation.pos()].isEmpty()) {
							field.set(typedThis, GPSQuality.valueOf(nmeaFields[annotation.pos()].charAt(0)));
						}
						else {
							field.set(typedThis, GPSQuality.FixNotAvailable);
						}
					}
					else {
						log.error("The type of the field " + field.getName() + " is not recognized: " + field.getType().getName());
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("parseNMEASentence error", e);
				} 
			}
		}
		
		typedThis.initSpecialFields(nmeaFields);
	}
	
	private final long receptionNanoTime;
	
	public NMEASentence(long receptionNanoTime) {
		this.receptionNanoTime = receptionNanoTime;
	}
	
	public long getReceptionNanoTime() {
		return this.receptionNanoTime;
	}
	
	protected void initSpecialFields(String[] nmeaFields) {
		
	}
}
