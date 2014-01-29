package server.customevent;

import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Document;
import com.filenet.api.core.DynamicReferentialContainmentRelationship;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.CustomEvent;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;

public class DocumentFiledEventAction implements EventActionHandler {

    public void onEvent(ObjectChangeEvent event, Id subscriptionId)
			throws EngineRuntimeException {

    	if ( event.get_SourceObject() instanceof DynamicReferentialContainmentRelationship ) {
    		DynamicReferentialContainmentRelationship  relationship = (DynamicReferentialContainmentRelationship) event.get_SourceObject();
    		IndependentObject object = relationship.get_Head();
    		if ( object instanceof Document ) {
    			raiseCustomEvent((Document) object);
    		}
    	}
    }

	private void raiseCustomEvent(Document document) {
		ObjectStore objectStore = document.getObjectStore();
		CustomEvent customEvent = Factory.CustomEvent.createInstance(objectStore, "DocumentFiledEvent" );
		customEvent.set_EventStatus( new Integer(0) );
		document.raiseEvent(customEvent);
		document.save(RefreshMode.NO_REFRESH);
	}
}
