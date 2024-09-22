package isel.tfm.locker_controller.backendRunner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

public class CustomizationBean implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>{
	
	@Value( "${backend.apps.locker-controller.contextPath}" )
	private String contextPath;
	
	@Value( "${backend.apps.locker-controller.port}" )
	private Integer port;
	
	@Override
    public void customize(ConfigurableServletWebServerFactory factory) {
		factory.setContextPath(contextPath);
		factory.setPort(port);
    }
}
