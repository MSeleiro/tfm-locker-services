package isel.tfm.launcher;

public class LauncherApplication {
	public static void main(String[] args) {
		try {
			MicroservicesStarter.startBackends();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
