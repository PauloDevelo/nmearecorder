package arbutus.influxdb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.influxdb.InfluxdbRepositoryTest;
import arbutus.influxdb.measurement.ComputedMeasurementTest;
import arbutus.influxdb.measurement.InfluxMeasurementTest;
import arbutus.influxdb.measurement.MeasurementTest;
import arbutus.influxdb.measurement.NMEAMeasurementTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	InfluxdbRepositoryTest.class,
	ComputedMeasurementTest.class,
	InfluxMeasurementTest.class,
	MeasurementTest.class,
	NMEAMeasurementTest.class
})

public class InfluxDBSuite {
}
