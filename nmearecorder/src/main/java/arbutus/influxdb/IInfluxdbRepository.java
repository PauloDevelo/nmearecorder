package arbutus.influxdb;

import java.util.Date;
import java.util.HashMap;

public interface IInfluxdbRepository {
	void addPoint(String measuremntName, Date utcTime, HashMap<String, Float> fields);
}
