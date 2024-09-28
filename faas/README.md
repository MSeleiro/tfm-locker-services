# Locker Services Serverless Prototype

**Prerequisite** (local / virtual machine): Java 11, Install [Docker](https://docs.docker.com/engine/install/ubuntu/), Install [Gradle](https://gradle.org/install/) 

---

This is our serverless prototype of the locker services, supported by the the OpenFaaS variant, [faasd](https://github.com/openfaas/faasd). This guide provides instructions to remotely deploy the functions from a local or virtual machine into a Raspberry Pi.

To deploy this system, do the folowing:

## Install faasd on your Raspberry Pi
1) Follow this [guide](https://blog.alexellis.io/faasd-for-lightweight-serverless/) for detailed commands on the installation process. 
    - **IMPORTANT:** Skip the "Get faas-cli" step, as we will be remotely deploying the functions.

2) Save your auth key after installation, it will be required in the next step, and later for the Metrics UI.

```bash
sudo cat /var/lib/faasd/secrets/basic-auth-password
```

## Setup MariaDB in your Raspberry Pi

### Installation:
```bash
sudo apt update
sudo apt upgrade
```

```bash
sudo apt install mariadb-server
```

### Setup root access:

1) Enter command:
```bash
sudo mysql_secure_installation
```

2) Press enter to continue (no password by default).
3) Press Y on all choices, including setting up a new password.

### Create user to access our database schema:

```bash
sudo mariadb

CREATE DATABASE ctt_locker;

CREATE USER '<username>'@'localhost' IDENTIFIED BY '<password>';

GRANT ALL PRIVILEGES ON ctt_locker.* TO '<username>'@'localhost';

FLUSH PRIVILEGES;
```

### Insert schema in MariaDB

```bash
cd schema

mysql -u <username> -p < locker_schema.sql
```

## Setup faas-cli on your local machine or virtual machine.

### Installation:
#### Linux

```bash
curl -sSL https://cli.openfaas.com | sudo -E sh
```

#### Windows
```powershell
$version = (Invoke-WebRequest "https://api.github.com/repos/openfaas/faas-cli/releases/latest" | ConvertFrom-Json)[0].tag_name
(New-Object System.Net.WebClient).DownloadFile("https://github.com/openfaas/faas-cli/releases/download/$version/faas-cli.exe", "faas-cli.exe")
```

If the powershell command fails to run, here is an alternative method using [Chocolatey](https://chocolatey.org/install):

```console
choco install faas-cli
```

### Login to faas-cli with the faasd auth key.


```bash
echo <PASSWORD> | faas-cli login -s -g 192.168.xxx.xxx:8080
```

### Fetch function template from the faas-cli store

```bash
cd faas

faas-cli template store pull java11
```

## Create .env file for the functions

The functions developed make use of environment variables, particularly for database access. To properly deploy the functions, create a **.env** with the following structure. Only change the fields with <...>

```yml
environment:
  MARIADB_USER: <username>
  MARIADB_PASS: <password>
  DB: ctt_locker
  RPI_ADDR: <rpi_ip_address>
  FAASD_PORT: 3306
  SIM_IP: <door_simulator_ip_address>   # addressed at the end of this README
  SIM_PORT: 52520
  SIM_PATH: /api
  OPENFAAS_URL: http://<rpi_ip_address>:8080/function/  # don't forget this one
  BACKEND_URL: http://.../
  RESERVATIONS_DB_FUNC: locker-reservation-manager
  DOOR_IO_HANDLER: locker-door-io
  MAIN_CONTROLLER: locker-controller
  JDBC_DRIVER: jdbc:mariadb://
```

## Deploy all functions

```bash
faas-cli deploy -f stack.yml -g 192.168.xxx.xxx:8080
```

#### Remove functions:

```bash
faas-cli rm -f stack.yml -g 192.168.xxx.xxx:8080
```

---

### Addendum: Door simulator situation

```yml
SIM_IP: <door_simulator_ip_address>
```

As part of this prototype, and for testing purposes, we used a python project developed by CTT to simulate the locker doors. As this project is confidential, it is not included in this repository.
The effects of this is that the entire workflow of the prototype can not be properly tested.