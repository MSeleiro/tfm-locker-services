package isel.tfm.locker_controller.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_controller.LockerControllerApplication;

public class LockerControllerServiceBackendRunner extends BackendRunner{
	public LockerControllerServiceBackendRunner() {
        super(LockerControllerApplication.class, CustomizationBean.class);
    }
}
