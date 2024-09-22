package isel.tfm.locker_reservation_manager.backendRunner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

public class CustomizationBean implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>{
	
	@Value( "${backend.apps.locker-reservation-manager.contextPath}" )
	private String contextPath;
	
	@Value( "${backend.apps.locker-reservation-manager.port}" )
	private Integer port;
	
	@Override
    public void customize(ConfigurableServletWebServerFactory factory) {
		factory.setContextPath(contextPath);
		factory.setPort(port);
    }
}
