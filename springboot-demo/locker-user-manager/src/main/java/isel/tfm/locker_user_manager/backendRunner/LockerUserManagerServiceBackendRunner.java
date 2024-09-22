package isel.tfm.locker_user_manager.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_user_manager.LockerUserManagerApplication;

public class LockerUserManagerServiceBackendRunner extends BackendRunner{
	public LockerUserManagerServiceBackendRunner() {
        super(LockerUserManagerApplication.class, CustomizationBean.class);
    }
}
