FROM groovy:alpine
ADD db2csv.groovy /home/db2csv.groovy
ENTRYPOINT groovy /home/db2csv.groovy
