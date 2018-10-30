package arbutus.influxdb;

import arbutus.util.PropertiesFile;

public class InfluxdbContext {

	public final String influxdbUrl;
	public final String user;
	public final String password;
	public final String dbName;
	public final String retentionDuration;
	public final int nbActions;
	public final int bufferLimit;
	public final int flushPeriodInSecond;
	public final int waitInfluxDbPeriodMaxInSec;

	public InfluxdbContext() {
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "influxdb.properties";

		PropertiesFile properties = PropertiesFile.getPropertiesVM(propertiesPath);
		
		this.influxdbUrl = properties.getValue("influxdbUrl");
		this.user = properties.getValue("user");
		this.password = properties.getValue("password");
		this.dbName = properties.getValue("dbName");
		this.retentionDuration = properties.getValue("retentionDuration");
		this.nbActions = properties.getValueInt("nbActions", 100);
		this.bufferLimit = properties.getValueInt("bufferLimit", 500);
		this.flushPeriodInSecond = properties.getValueInt("flushPeriodInSecond", 5);
		this.waitInfluxDbPeriodMaxInSec = properties.getValueInt("waitInfluxDbPeriodMaxInSec", 181);
	}
	
	public InfluxdbContext(String url, String dbName) {
		this.influxdbUrl = url;
		this.user = "root";
		this.password = "root";
		this.dbName = dbName;
		this.retentionDuration = "30d";
		this.nbActions = 100;
		this.bufferLimit = 500;
		this.flushPeriodInSecond = 5;
		this.waitInfluxDbPeriodMaxInSec = 181;
	}

}
