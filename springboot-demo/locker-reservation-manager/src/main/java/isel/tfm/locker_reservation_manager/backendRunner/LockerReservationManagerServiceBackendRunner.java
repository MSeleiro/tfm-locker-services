package isel.tfm.locker_reservation_manager.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_reservation_manager.LockerReservationManagerApplication;

public class LockerReservationManagerServiceBackendRunner extends BackendRunner{
	public LockerReservationManagerServiceBackendRunner() {
        super(LockerReservationManagerApplication.class, CustomizationBean.class);
    }
}
