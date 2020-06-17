Distributed Systems Engineering SS20
===================================

In order to run locally:
-----------------------------

- change directory to actor-simulator and build image with `docker build -t actor-simulator`
- change to project root and run `docker-compose up` to start all services and frontend
- open web UI on http://localhost:4200/
- start simulation with `docker run --network dse20_default actor-simulator`


In order to run on GCP:
-----------------------------

- deploy and expose services
- set in environment.ts IP of API Gateway
- build and push client module
- deploy and expose client app
- open web app on IP of client from cluster
- to restart simulator `kubectl get pods` and then `kubectl delete pod actor-simulator-<id>`