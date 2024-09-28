# Locker Services Metrics Interface

**Prerequisite** (local machine): [Node.js](https://nodejs.org/en/download/prebuilt-installer), npm 10.8.2, Install [Docker](https://docs.docker.com/engine/install/ubuntu/), Complete the steps of the [faas README](../faas/README.md)

---

This is our metrics interface for the locker services, made using [React](https://react.dev/). This guide provides instructions on how to setup and use this web application.

## Setup a Prometheus instance to Federate metrics
In order to query metrics from the built-in Prometheus instance in faasd, we need to deploy a secondary Prometheus instance configured to fetch the metrics from the built-in faasd ([documentation](https://docs.openfaas.com/architecture/metrics/)).

**IMPORTANT**: Before deploying the container, edit the last line of the [prometheus.yml](./prometheus/prometheus.yml) file to include the IP address of your Raspberry Pi.

**Note:** Replace /path/to/prometheus.yml with the full path to the file.

```bash
cd prometheus

docker run -p 9090:9090 -v /path/to/prometheus.yml:/etc/prometheus/prometheus.yml -v prometheus-data:/prometheus prom/prometheus
```

## Setup the Node environment

### Install Dependencies:

```bash
npm install
```

## Create .env file for the proxy to faasd

In order to properly fetch the number of functions deployed, we must utilize a proxy to directly ask faasd. For this, we must provide an auth token to access the faasd gateway, the same as in the faasd guide, but with an added username, which is, by default, admin. 

Create the following .env file in the backend-ui folder

```
REACT_APP_MIDDLEWARE_AUTH=admin:<your_faasd_token_here>
```

## Deploy the metrics interface

```bash
npm start
```
