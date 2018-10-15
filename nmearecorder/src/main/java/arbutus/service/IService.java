package arbutus.service;

public interface IService {
	public ServiceState getState();
	public void start() throws Exception;
	public void stop();
}
