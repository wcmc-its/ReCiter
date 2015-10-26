package reciter.engine;

public class ReCiterEngineFactory {

	public Engine getReCiterEngine(ReCiterEngineProperty reCiterEngineProperty) {
		return new ReCiterEngine(reCiterEngineProperty);
	}
}
