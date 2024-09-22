package isel.tfm.locker_door_io.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_door_io.LockerDoorIOApplication;

public class LockerDoorIOServiceBackendRunner extends BackendRunner{
	public LockerDoorIOServiceBackendRunner() {
        super(LockerDoorIOApplication.class, CustomizationBean.class);
    }
}
