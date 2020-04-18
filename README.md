# Messaging Service

Assumptions:
* No user management from service besides validation of existing message box on get/delete operations, a new message box will be created if messages for unknown user is received
* Current storage is in memory only, depending on volumes, sla and such, a suitable persistent storage would be needed
* Implemented as a microservices intended for running in Kubernetes. 
* Running on Kubernetes would solve redundancy using load balancer and multiple pods (once proper persistent storage is added)
* Kubernetes could be used to handle scalability by scaling on metrics such as memory, cpu, requests/s
* Deleting non existing messages is silently ignored, handles possibly overlapping delete requests for same messages 

# How to build, compile and run:
Prerequisites is having git, maven and Java 11

* git clone https://github.com/castensson/messagingservice.git
* Move into cloned repository
* mvn package
* java -jar target/MessageService-0.0.1-SNAPSHOT.jar

# Access application
Application is started on port 8080

Swagger documentation and interface will be available at
http://localhost:8080/swagger-ui.html

API docs which can for example be imported into Postman available at:
http://localhost:8080/v2/api-docs

Metrics to be scraped from Prometheus available at
http://localhost:8080/actuator/prometheus

# Summing up
Far from production ready but illustrating a possible solution for creating a simple messaging service. To keep it simple there is no persistence om messaging, they live in memory for now. Depending on the requirments for the services a suitable backend storage would need to be choses. This could for example be a relational database such as PostgreSQL or if there were to be a massive amount of messages being sent Cassandra could be a possible candidate. 



