package reciter.utils;

import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author szd2013
 * 
 * Introduce delay for pubmed api calls
 *
 */
@Data
@Slf4j
public class ThreadDelay {
	
	/**
	 * @param timeout Timeout for the delay
	 */
	public static void pubmedApiDelay(long timeout) {
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			log.error("InterruptedException", e);
		}
	}

}
