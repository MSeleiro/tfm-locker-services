package isel.tfm.locker_door_manager.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_door_manager.LockerDoorManagerApplication;

public class LockerDoorManagerServiceBackendRunner extends BackendRunner{
	public LockerDoorManagerServiceBackendRunner() {
        super(LockerDoorManagerApplication.class, CustomizationBean.class);
    }
}
