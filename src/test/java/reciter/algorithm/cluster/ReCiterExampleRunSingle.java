package reciter.algorithm.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.engine.Engine;
import reciter.engine.ReCiterEngineFactory;
import reciter.engine.ReCiterEngineProperty;

public class ReCiterExampleRunSingle {
	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterExampleRunSingle.class);

	private ReCiterEngineFactory reCiterEngineFactory;
	private Engine engine;
	
	public ReCiterExampleRunSingle() {
		reCiterEngineFactory = new ReCiterEngineFactory();
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		engine = reCiterEngineFactory.getReCiterEngine(property);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		ReCiterExampleRunSingle example = new ReCiterExampleRunSingle();
		example.engine.run("mecharl");
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}
}
