package isel.tfm.locker_screen_io.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_screen_io.LockerScreenIOApplication;

public class LockerScreenIOServiceBackendRunner extends BackendRunner{
	public LockerScreenIOServiceBackendRunner() {
        super(LockerScreenIOApplication.class, CustomizationBean.class);
    }
}
