package isel.tfm.locker_partner_manager.backendRunner;

import isel.tfm.common.BackendRunner;
import isel.tfm.locker_partner_manager.LockerPartnerManagerApplication;

public class LockerPartnerManagerServiceBackendRunner extends BackendRunner{
	public LockerPartnerManagerServiceBackendRunner() {
        super(LockerPartnerManagerApplication.class, CustomizationBean.class);
    }
}
