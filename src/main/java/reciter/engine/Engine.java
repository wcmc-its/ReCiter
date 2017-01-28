package reciter.engine;

import java.util.List;

public interface Engine {

	EngineOutput run(EngineParameters parameters, StrategyParameters strategyParameters);

	List<Feature> generateFeature(EngineParameters parameters);
}
