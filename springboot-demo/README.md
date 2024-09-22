Prerequisite: Java8, Maven3+

This is a Spring demo project of the locker services, based on the [springboot-multi-service-launcher](https://github.com/rameez4ever/springboot-demo/tree/master/springboot-multi-service-launcher) template:
1) cd springboot-demo
2) mvn clean install  (This may take time as it ll resolve all the dependencies.)
3) To run all microservices
	~ cd launcher
	~ mvn exec:java
	
	You can change ports here /launcher/src/main/resources/application.yml
