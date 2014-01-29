package server.dispenser;

import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.CustomObject;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.exception.ExceptionCode;
import com.filenet.api.property.Properties;
import com.filenet.api.property.PropertyFilter;

public class Dispenser {

	private static final String DISPENSER_CLASS_NAME = "Dispenser";
	private static final String COUNTER_PROPERTY_NAME = "Counter";
	
	/** 
	 * This property filter is used to minimize data returned in fetches and refreshes.
	 */
	private static final PropertyFilter PF_COUNTER = new PropertyFilter();
	
	static
	{
	    PF_COUNTER.addIncludeProperty(1, null, null, COUNTER_PROPERTY_NAME, null);
	}

	/**
	 * Set by constructor or some other means.
	 * Fetchless instantiation is OK.
	 */
	private final CustomObject dispenser;
	
	public Dispenser(ObjectStore objectStore, String dispenserPath ) {
		dispenser = Factory.CustomObject.getInstance(objectStore,
				DISPENSER_CLASS_NAME, dispenserPath);
	}

	/**
	 * Get the next value efficiently by exploiting First Writer Wins
	 */
	public int getNextValue(boolean feelingUnlucky)
	{
	    final Properties dispenserProperties = dispenser.getProperties();
	    // Object might be updated by someone else, so try a few times
	    for (int attemptNumber=0; attemptNumber<10; ++attemptNumber)
	    {
	        // If cached data invalid, fetch the current value
	        // from the server.  This also covers the fetchless
	        // instantiation case.
	        if (feelingUnlucky
	        ||  dispenser.getUpdateSequenceNumber() == null 
	        ||  !dispenserProperties.isPropertyPresent(COUNTER_PROPERTY_NAME))
	        {
	            // fetchProperties will fail if the USN doesn't match, so null it out
	            dispenser.setUpdateSequenceNumber(null);
	            dispenser.fetchProperties(PF_COUNTER);  // R/T
	        }
	        int oldValue = dispenserProperties.getInteger32Value(COUNTER_PROPERTY_NAME);
	        int newValue = oldValue + 1;
	        dispenserProperties.putValue(COUNTER_PROPERTY_NAME, newValue);
	        try
	        {
	            // Because we use a refreshing save, the counter property's
	            // new value will be returned from the server.
	            dispenser.save(RefreshMode.REFRESH, PF_COUNTER);  // R/T
	            return newValue;
	        }
	        catch (EngineRuntimeException ere)
	        {
	            ExceptionCode ec = ere.getExceptionCode();
	            if (ec != ExceptionCode.E_OBJECT_MODIFIED)
	            {
	                // If we get an exception for any reason other than
	                // the object being concurrently modified, rethrow it.
	                throw ere;
	            }
	            // Someone else modified it.  Invalidate our cached data and try again.
	            dispenser.setUpdateSequenceNumber(null);
	            dispenserProperties.removeFromCache(COUNTER_PROPERTY_NAME);
	            continue;  
	        }
	    }
	    // too many iterations without success
	    throw new RuntimeException("Oops");
	}
}