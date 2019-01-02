# CirdlesWebServices

This project consists of the RESTful CIRDLES web services

## Deployment

Deployment of this project is performed by a cronjob that executes a deployment BASH script.

The cronjob is executed as root, so to make changes to the cronjob one must: 

```BASH
sudo crontab -e
```
The deployment script resides in /usr/local/bin/deploy if changes need to be made.

## Updating on Server

In order to update this project one simply needs to update the github page, and the deployment script will handle the rest.

The deployment script is executed at 2:00AM every day, but if immeadiate updates are needed one can simply execute the deployment script manually:

```BASH
ssh $USER@cirdles.cs.cofc.edu
sudo ./usr/local/bin/deploy
```

The deployment script will then perform the following:

1. Pull changes from github
2. Perform a maven clean install on project (build)
3. Overwrites tomcat webapp endpoint (/opt/tomcat9/webapps/Services.war) with newly created Services.war file

## Built With

* [JavaEE](http://www.oracle.com/technetwork/java/javaee/tech/index.html) - Specifications for creating web services
* [Maven](https://maven.apache.org/) - Dependency Management
* [JitPack](https://jitpack.io/) - Package repository for github/Maven
* [NGINX](https://www.nginx.com/) - Server Platform / Traffic director
* [Tomcat 9](http://tomcat.apache.org/) - Java Servlet Container


## Project Structure

```
CirdlesWebServices
├── .gitignore
├── LICENSE
├── README.md
├── pom.xml (configuration, maven)
├── src/main
|   ├── webapp (configuration files)
|       ├── WEB-INF
|           ├── web.xml (endpoints, etc.)
|       ├── META-INF
|           ├── context.xml (Tomcat-specific)
|       ├── index.html
|   ├── java/org/cirdles/webServices (servlets and utilities)
|       ├── ambapo
|           ├── AmbapoServlet.java
|       ├── calamari
|           ├── CalamariServlet.java
|           ├── PrawnFileHandlerService.java
|       ├── requestUtils
|           ├── JSONUtils.java
```
