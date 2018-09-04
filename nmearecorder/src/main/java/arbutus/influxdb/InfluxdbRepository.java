package arbutus.influxdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

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

			try {
				influxDB.write(builder.build());
			}
			catch (Exception ex) {
				log.error("Error in addPoint", ex);
			}
		}
	}

	@Override
	public ServiceState getState() {
		if(influxDB == null || !Ping()) {
			return ServiceState.STOPPED;
		}
		else {
			return ServiceState.STARTED; 
		}
	}

	@Override
	public void start() {
		if(getState() == ServiceState.STOPPED) {
			try {
				influxDB = InfluxDBFactory.connect(properties.getValue("influxdbUrl"), properties.getValue("user"), properties.getValue("password"));

				String dbName = properties.getValue("dbName");
				if(!this.databaseExists(dbName)) {
					influxDB.query(new Query("CREATE DATABASE " + dbName, dbName, true));

					influxDB.setDatabase(dbName);

					String commandRetention = "CREATE RETENTION POLICY aRetentionPolicy ON "+ dbName + " DURATION " + properties.getValue("retentionDuration") + " REPLICATION 1 DEFAULT";
					influxDB.query(new Query(commandRetention, dbName, true));

					influxDB.setRetentionPolicy("aRetentionPolicy");
				}
				else {
					influxDB.setDatabase(dbName);
					influxDB.setRetentionPolicy("aRetentionPolicy");
				}

				influxDB.enableBatch(BatchOptions.DEFAULTS.exceptionHandler(
						(failedPoints, throwable) -> { this.logInfluxError(failedPoints, throwable); })
						);
			}
			catch(Exception ex) {
				log.fatal("Error when starting the InfluxDb repo as a service.", ex);
				closeInfluxDb();
			}
		}
		else {
			log.warn("Attempt to start influxdbRepository although it is already started.");
		}
	}

	@Override
	public void stop() {
		if(getState() == ServiceState.STARTED) {
			closeInfluxDb();
		}
		else {
			log.warn("Attempt to stop influxdbRepository although it is already stopped.");
		}
	}

	private void closeInfluxDb() {
		try {
			influxDB.close();
		}
		catch(Exception ex) {
			log.error("Error when closing InfluxDbRepo.", ex);
		}
		finally {
			influxDB = null;
		}
	}

	private boolean Ping() {
		try {
			return influxDB.ping().isGood();
		}
		catch(Exception ex) {
			log.error("Error in the ping :" + ex.getMessage());
			return false;
		}
	}

	private List<String> describeDatabases() {
		try {
			QueryResult result = influxDB.query(new Query("SHOW DATABASES", ""));
			// {"results":[{"series":[{"name":"databases","columns":["name"],"values":[["mydb"]]}]}]}
			// Series [name=databases, columns=[name], values=[[mydb], [unittest_1433605300968]]]
			List<List<Object>> databaseNames = result.getResults().get(0).getSeries().get(0).getValues();
			List<String> databases = new ArrayList<>();
			if (databaseNames != null) {
				for (List<Object> database : databaseNames) {
					databases.add(database.get(0).toString());
				}
			}
			return databases;
		}
		catch(Exception ex) {
			log.error("Error when getting the existing database names", ex);
			return new ArrayList<String>();
		}
	}


	private boolean databaseExists(String dbName) {
		List<String> databases = this.describeDatabases();
		for (String databaseName : databases) {
			if (databaseName.trim().equals(dbName)) {
				return true;
			}
		}
		return false;
	}

	private void logInfluxError(Iterable<Point> failedPoints, Throwable throwable) {
		InfluxdbRepository.log.error("Error when writing points", throwable);
	}
}
