Distributed Systems Engineering SS20
===================================

In order to make run locally:
-----------------------------

- change directory to actor-simulator and build image with `docker build -t actor-simulator`
- change to project root and run `docker-compose up` to start all services and frontend
- open web UI on http://localhost:4200/
- start simulation with `docker run --network dse20_default actor-simulator`
