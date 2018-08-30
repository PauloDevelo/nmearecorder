package arbutus.service;

public interface IService {
	public ServiceState getState();
	public void start();
	public void stop();
}
