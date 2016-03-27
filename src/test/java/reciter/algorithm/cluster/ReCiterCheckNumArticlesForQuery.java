package reciter.algorithm.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.engine.Engine;
import reciter.engine.ReCiterEngineFactory;
import reciter.engine.ReCiterEngineProperty;

public class ReCiterCheckNumArticlesForQuery {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ReCiterCheckNumArticlesForQuery.class);

	private ReCiterEngineFactory reCiterEngineFactory;
	private Engine engine;
	
	public ReCiterCheckNumArticlesForQuery() {
		reCiterEngineFactory = new ReCiterEngineFactory();
		ReCiterEngineProperty property = new ReCiterEngineProperty();
		engine = reCiterEngineFactory.getReCiterEngine(property);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		ReCiterCheckNumArticlesForQuery reCiterCheckNumArticlesForQuery = new ReCiterCheckNumArticlesForQuery();
//		reCiterCheckNumArticlesForQuery.engine.checkNumQueries();
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}
}
