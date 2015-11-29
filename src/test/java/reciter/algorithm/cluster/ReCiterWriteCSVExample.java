package reciter.algorithm.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.engine.Engine;
import reciter.engine.ReCiterEngineFactory;
import reciter.engine.ReCiterEngineProperty;

public class ReCiterWriteCSVExample {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterWriteCSVExample.class);

	private ReCiterEngineFactory reCiterEngineFactory;
	private Engine engine;
	
	public ReCiterWriteCSVExample() {
		reCiterEngineFactory = new ReCiterEngineFactory();
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		engine = reCiterEngineFactory.getReCiterEngine(property);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		ReCiterWriteCSVExample example = new ReCiterWriteCSVExample();
		example.engine.constructAnalysis();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}
}
