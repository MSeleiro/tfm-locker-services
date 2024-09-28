# Locker Services SpringBoot demo

**Prerequisite** (Raspberry Pi): Java 8, Install [Maven3+](https://maven.apache.org/install.html), Complete the steps of the [faas README](../faas/README.md)

---

This is a Spring demo project of the locker services, based on the [springboot-multi-service-launcher](https://github.com/rameez4ever/springboot-demo/tree/master/springboot-multi-service-launcher) template. This project needs to be locally deployed in the Raspberry Pi.

To deploy this system, do the folowing:

1) Switch Java versions, we want Java 8 for this project
```bash
sudo update-alternatives --config java
```

2) Deploy Spring services
```bash
cd springboot-demo

mvn clean install 

cd launcher

mvn exec:java
```
	
You can change service ports in /launcher/src/main/resources/application.yml
