package reciter.engine;

import java.util.List;

import reciter.engine.erroranalysis.Analysis;

public interface Engine {

	EngineOutput run(EngineParameters parameters);

	List<Feature> generateFeature(EngineParameters parameters);
}
