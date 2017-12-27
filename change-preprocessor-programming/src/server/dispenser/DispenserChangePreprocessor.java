package server.dispenser;

import com.filenet.api.action.Create;
import com.filenet.api.action.PendingAction;
import com.filenet.api.constants.ReservationType;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.RepositoryObject;
import com.filenet.api.engine.ChangePreprocessor;
import com.filenet.api.exception.EngineRuntimeException;

public class DispenserChangePreprocessor implements ChangePreprocessor {

	@Override
	public boolean preprocessObjectChange(IndependentlyPersistableObject object)
			throws EngineRuntimeException {
		
		if ( isCreate(object) ) {
			String dispenserPath = object.getProperties().getStringValue("DispenserPath");
			String value = getValueFromDispenser( ((RepositoryObject) object).getObjectStore(), dispenserPath );
			object.getProperties().putValue("ObjectId", value );
			return true;
		}
		return false;
	}

	boolean isCreate(IndependentlyPersistableObject object) {
		PendingAction actions[] = object.getPendingActions();
		for ( PendingAction action : actions) {
			if ( action instanceof Create ) {
				ReservationType reservationType = ((Create) action).getReservationType();
				if ( reservationType == null ) return true;
			}
		}
		return false;
	}

	private String getValueFromDispenser(ObjectStore objectStore, String dispenserPath ) {
		Dispenser dispenser = new Dispenser( objectStore, dispenserPath );
		int value = dispenser.getNextValue(false);
		return Integer.toString(value);
	}
}
