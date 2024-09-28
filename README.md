# LockyCloudCore - Locker Services

This repository was created as support for the TFM *"LockyCloudCore - CTT locker management infrastructure"*, developed during the 2023-2024 semesters as a partial requirement for the degree of Master's in Computer Science and Engineering. It includes all of the code developed during this thesis, as well as extra Annex files referenced in the TFM.

### Organization

- Folder [faas](./faas) includes our serverless prototype, developed as the main work of our thesis, and deployment instructions. 

- Folder [springboot-demo](./springboot-demo/) includes the SpringBoot demo developed for comparison purposes. It includes deployment instructions, but should be tested after our prototype.

- Folder [backend-ui](./backend-ui/) includes the web application developed for observability of our prototype. It comes with instructions on how to configure the environment and run the web-app.

- Folder [dapr](./dapr) includes the code developed on our first proposal, Event-Driven Microservices with Dapr.


- Folder [extra-files](./extra-file) has the Annexes referenced in the TFM text, namely the [JMETER test results](./extra-files/jmeter-results/), and the [Prometheus metrics](./extra-files/prometheus-metrics/metrics.txt) measured by the built-in Prometheus in faasd.