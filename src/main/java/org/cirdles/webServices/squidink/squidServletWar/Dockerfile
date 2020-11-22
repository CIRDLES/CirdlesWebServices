FROM tomcat:9.0-alpine

LABEL maintainer=”joshdgilley@gmail.com”

ADD squid_servlet.war /usr/local/tomcat/webapps/
ADD server.xml /usr/local/tomcat/conf/
ADD tomcat-users.xml /usr/local/tomcat/conf/
ADD context.xml /usr/local/tomcat/webapps/manager/META-INF/



EXPOSE 8080

CMD ["catalina.sh", "run"]