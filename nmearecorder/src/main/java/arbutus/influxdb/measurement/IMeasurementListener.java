package arbutus.influxdb.measurement;

public interface IMeasurementListener {
	void onMeasurementChanged(Measurement measurement);
}
