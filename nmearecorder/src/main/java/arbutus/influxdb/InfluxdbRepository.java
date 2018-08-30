package arbutus.influxdb;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;

import arbutus.service.IService;
import arbutus.service.ServiceState;
import arbutus.util.PropertiesFile;

public class InfluxdbRepository implements IService, IInfluxdbRepository {
	private static Logger log = Logger.getLogger(InfluxdbRepository.class);
	
	private PropertiesFile properties = null;
	private InfluxDB influxDB = null;

	public InfluxdbRepository() {
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "influxdb.properties";
		
		log.debug(propertiesPath);
				 
		properties = PropertiesFile.getPropertiesVM(propertiesPath);
	}
	
	
	@Override
	public void addPoint(String measuremntName, Date utcTime, HashMap<String, Float> fields) {
		if(getState() == ServiceState.STARTED) {
			Builder builder = Point.measurement(measuremntName).time(utcTime.getTime(), TimeUnit.MILLISECONDS);
			
			for(String fieldName : fields.keySet()) {
				builder.addField(fieldName, fields.get(fieldName));
			}
			
			influxDB.write(builder.build());
		}
	}

	@Override
	public ServiceState getState() {
		if(influxDB == null || !influxDB.ping().isGood()) {
			return ServiceState.STOPPED;
		}
		else {
			return ServiceState.STARTED; 
		}
	}

	@Override
	public void start() {
		if(getState() == ServiceState.STOPPED) {
			influxDB = InfluxDBFactory.connect(properties.getValue("influxdbUrl"), properties.getValue("user"), properties.getValue("password"));
			
			String dbName = properties.getValue("dbName");
			influxDB.query(new Query("CREATE DATABASE " + dbName, dbName, true));
			
			influxDB.setDatabase(dbName);
			
			String commandRetention = "CREATE RETENTION POLICY aRetentionPolicy ON "+ dbName + " DURATION " + properties.getValue("retentionDuration") + " REPLICATION 1 DEFAULT";
			influxDB.query(new Query(commandRetention, dbName, true));
			
			influxDB.setRetentionPolicy("aRetentionPolicy");

			influxDB.enableBatch(BatchOptions.DEFAULTS);
		}
	}

	@Override
	public void stop() {
		if(getState() == ServiceState.STARTED) {
			influxDB.close();
			influxDB = null;
		}
	}

}
