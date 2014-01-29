package server.dispenser;

import com.filenet.api.constants.PropertyNames;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.events.Subscription;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.util.Id;

public class DispenserEventAction implements EventActionHandler {

	private static final PropertyFilter PF_USER_STRING = new PropertyFilter();
	
	static
	{
		PF_USER_STRING.addIncludeProperty(1, null, null, PropertyNames.USER_STRING, null);
	}
	
	public void onEvent(ObjectChangeEvent event, Id subscriptionId)
			throws EngineRuntimeException {
	
		String userString = getUserString(event, subscriptionId );
		String[] parts = userString.split(",");
		if ( parts.length < 2) {
			return;
		}
		String propertyName = parts[0].trim();
		String dispenserPath = parts[1].trim();

		ObjectStore objectStore = event.getObjectStore();

		String classId = event.get_SourceClassId().toString();
		Id objectId = event.get_SourceObjectId();
		IndependentlyPersistableObject object = (IndependentlyPersistableObject) objectStore.getObject(classId, objectId );
//		Document object = Factory.Document.fetchInstance(objectStore, objectId, null );
		String value = getValueFromDispenser(event.getObjectStore(), dispenserPath);
		object.getProperties().putValue(propertyName, value );
		object.save( RefreshMode.NO_REFRESH );
	}

	private String getValueFromDispenser(ObjectStore objectStore, String dispenserPath ) {
		Dispenser dispenser = new Dispenser( objectStore, dispenserPath );
		int value = dispenser.getNextValue(false);
		return Integer.toString(value);
	}

	private String getUserString(ObjectChangeEvent event, Id subscriptionId) {
		Subscription subscription = Factory.Subscription.fetchInstance(event
				.getObjectStore(), subscriptionId, PF_USER_STRING);
		return subscription.get_UserString();
	}
}
