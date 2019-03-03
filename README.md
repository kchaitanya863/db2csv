# db2csv

### Install Groovy first
`sudo apt-get install groovy`

## Run this script without cloning 
`groovy https://raw.githubusercontent.com/kchaitanya863/db2csv/master/db2csv.groovy`

## Download single table in the database
`groovy db2csv.groovy -h <db_hostname> -u <username> -p <password> -d database_name -t table_name`

## Download all tables in the database
`groovy db2csv.groovy -h localhost -u root -p root -d database_name -a`

## Download from query 
`groovy db2csv.groovy -h localhost -u root -p root -d database_name -q "select * from test where id < 40"`


``` 
      _ _    ___                
     | | |  |__ \               
   __| | |__   ) |___ _____   __
  / _\` | '_ \ / // __/ __\ \ / /
 | (_| | |_) / /| (__\__ \\ V / 
  \__,_|_.__/____\___|___/ \_/  
                                
                                
Usage: db2csv [-?asv] [--client=<client>] [--delimiter=<delimiter>]
              [--escape=<csvEscapeCharacter>] [--quote=<quoteCharacter>]
              [--schema=<schema>] -d=<database> -h=<hostname>
              [-o=<outputFolder>] -p=<password> [-port=<port>] [-q=<query>]
              [-r=<resultSetfetchSize>] [-t=<table>] -u=<username>
Get Database tables as csv
      --client=<client>   select client MySQL or PostgreSQL
                            Default: MySQL
      --delimiter=<delimiter>
                          CSV delimiter.
                            Default: ,
      --escape=<csvEscapeCharacter>
                          CSV Quote Character.
                            Default: "
      --quote=<quoteCharacter>
                          CSV Quote Character.
                            Default: "
      --schema=<schema>   PostgreSQL Schema.
                            Default: public
  -?, --help              Show this help message and exit.
  -a, --all               download all tables in the database
  -d, --database=<database>
                          database name to use.
  -h, --host=<hostname>   hostname of the database server.
  -o, --output=<outputFolder>
                          path to output
  -p, --password=<password>
                          password to the database server.
      -port=<port>        port of the database server.
                            Default: 3306
  -q, --query=<query>     query to save. ex.: select * from tableName where id < 6;
  -r, --resultSetfetchSize=<resultSetfetchSize>
                          Number of records to fetch from server in one call
                            Default: 200
  -s, --ssl               use ssl connection to the server
  -t, --table=<table>     table to save.
  -u, --user=<username>   username of the database server.
  -v, --verbose           Print more info on execution

For more details, see:
Developed by @kchaitanya863
ASCII Art thanks to http://patorjk.com/software/taag/

```
