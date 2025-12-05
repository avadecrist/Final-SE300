For this to run:
Use Java 17. For tests (Not jacoco the other one)
Maven command mvn clean test jacoco:report
Make sure ports 8080 and test ports 8082 8888 and 9999 are free for CI testing. 
API dog not used, used mockserver and rest assured to run locally. This shld be on port 9999