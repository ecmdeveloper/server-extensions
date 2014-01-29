package server.hello;

import java.util.logging.Logger;

import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

public class SimpleEventActionHandler implements EventActionHandler {

	private static final String CLASS_NAME = SimpleEventActionHandler.class.getName();
	private static Logger logger = Logger.getLogger( CLASS_NAME );
	
	public void onEvent(ObjectChangeEvent event, Id subscriptionId)
			throws EngineRuntimeException {
	    try {
			logger.info("Event: " + event.getClassName() );
			logger.info("subscriptionId " + subscriptionId.toString() );
			System.out.println( "Hello, World!" );
		} catch (Exception e) {
			logger.throwing(CLASS_NAME, "onEvent", e);
		}
	}
}
