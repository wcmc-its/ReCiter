package reciter.algorithm.article.score.predictor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class NeuralNetworkModelArticlesScorer {

	
	private static final Logger log = LoggerFactory.getLogger(NeuralNetworkModelArticlesScorer.class);
	
	public JSONArray executeArticleScorePredictor(String category, String articleScoreModelFileName,String articleDataFilename,String s3BucketName)
	{
	
		StopWatch stopWatch = new StopWatch(category);
		stopWatch.start(category);
	
	    try {
	    		
			 	System.out.println("category and fileName "+ category + " - " + articleScoreModelFileName +" - " + articleDataFilename + " - " + s3BucketName);

			 	//Prepare to call the Python script
	            ProcessBuilder processBuilder = new ProcessBuilder("python", articleScoreModelFileName,articleDataFilename,s3BucketName);
	            processBuilder.redirectErrorStream(true); 
	            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
	            processBuilder.environment().put("TF_CPP_MIN_LOG_LEVEL", "2");
	            processBuilder.environment().put("TF_ENABLE_ONEDNN_OPTS", "0"); // Optional, to disable oneDNN ops
	            processBuilder.directory(new File("src/main/resources/scripts")); // Set the directory where the script is located
	            
	            // Start the process
	            Process process = processBuilder.start();
	            
            	// Capture the output
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                
                // Wait for the process to complete
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    // Process output
                    String jsonOutput = output.toString();
                    System.out.println("jsonOutput : ->" + jsonOutput);
                    
                    return new JSONArray(jsonOutput);
                } else {
                    System.out.println("Script execution failed with exit code: " + exitCode);
                }	
	
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		stopWatch.stop();
		log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
		return null;
	}
}
