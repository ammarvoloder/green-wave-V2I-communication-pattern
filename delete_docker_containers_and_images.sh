#### Delete created containers and images ####
#!/bin/bash
docker rm $(docker ps -a -q) -v -f
docker rmi actor-registry-service:latest
docker rmi api-gateway:latest
