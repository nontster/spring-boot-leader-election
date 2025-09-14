# Spring Boot Leader Election Demo

This project demonstrates how to use Spring Cloud Kubernetes for leader election.

## Prerequisites

*   Java 17
*   Maven
*   Docker
*   Kubernetes cluster (e.g., Minikube, Docker Desktop)

## Building the project

To build the project, run the following command:

```bash
./mvnw clean install
```

## Bumping the version

To bump the version of the application, run the following command:

```bash
./bump-version.sh <new-version>
```

This will:

*   Update the version in `pom.xml`.
*   Update the image version in `kubernetes/deployment.yaml`.
*   Update the image version in `src/test/resources/deployment.yaml`.

## Building the container image

The container image is built using the `docker-maven-plugin`. Running the `install` command will build the image.

The image name is `nontster/spring-leader:1.0.1`.

## Deploying to Kubernetes

To deploy the application to Kubernetes, run the following commands:

```bash
kubectl apply -f kubernetes/rbac.yaml
kubectl apply -f kubernetes/secret.yaml
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```

## Verifying leader election

To verify leader election, you can check the logs of the pods. Only one pod should be the leader at any given time.

```bash
kubectl logs -l app=leader-election-demo
```

You can also access the `/leader` endpoint to see which pod is the leader.

```bash
kubectl port-forward svc/leader-election-demo 8080:80
```

Then, in a separate terminal, run:

```bash
curl http://localhost:8080/leader
```