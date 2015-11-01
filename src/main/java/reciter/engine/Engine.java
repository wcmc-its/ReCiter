package reciter.engine;

public interface Engine {

	ReCiterEngineProperty getReCiterEngineProperty();
	void run(String lastName, String firstInitial, String middleName, String cwid, String email);
	void run();
}
