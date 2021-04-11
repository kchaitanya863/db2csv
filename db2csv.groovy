@Grab('info.picocli:picocli:2.0.3')
@Grab(group='mysql', module='mysql-connector-java', version='8.0.15')
@Grab(group='org.postgresql', module='postgresql', version='42.2.5')
@Grab('com.opencsv:opencsv:4.0')
@picocli.groovy.PicocliScript
@Command(header = [
        $/@|bold,green       _ _    ___                |@/$,
        $/@|bold,green      | | |  |__ \               |@/$,
        $/@|bold,green    __| | |__   ) |_________   __|@/$,
        $/@|bold,green   / _\` | '_ \ / // __/__\ \ / /|@/$,
        $/@|bold,green  | (_| | |_) / /| (__\__ \\ V / |@/$,
        $/@|bold,green   \__,_|_.__/____\___|___/ \_/  |@/$,
        $/@|bold,green                                 |@/$,
        $/@|bold,green                                 |@/$
],

        description = "Get Database tables as csv",
        version = 'db2csv v1.0.0', showDefaultValues = true,
        footerHeading = "%n============================%n",
        footer = ["Developed by @kchaitanya863",
                "ASCII Art thanks to http://patorjk.com/software/taag/"]
)

import groovy.transform.Field
import com.opencsv.CSVWriter
import groovy.time.*
import java.security.MessageDigest
import static picocli.CommandLine.*
import groovy.sql.Sql
//files list
// @Parameters(arity = "1", paramLabel = "FILE", description= "The file(s) whose checksum to calculate.")
// @Field File[] files
//client
@Option(names = ["--client"], description = "select client MySQL or PostgreSQL")
@Field String client = "MySQL"
//all tables
@Option(names = ["-a", "--all"], description = "download all tables in the database")
@Field boolean all_tables = false
//use ssl
@Option(names = ["-s", "--ssl"], description = "use ssl connection to the server")
@Field boolean ssl = false
//host name
@Option(names = ["-h", "--host"], required = true, description = "hostname of the database server.")
@Field String hostname
//database
@Option(names = ["-d", "--database"], required = true, description = "database name to use.")
@Field String database
//username
@Option(names = ["-u", "--user"], required = true, description = "username of the database server.")
@Field String username
//password
@Option(names = ["-p", "--password"], required = true, description = "password to the database server.")
@Field String password
//port
@Option(names = ["-port"], description = "port of the database server.")
@Field int port = 3306
//resultset fetch size
@Option(names = ["-r", "--resultSetfetchSize"], description = "Number of records to fetch from server in one call")
@Field int resultSetfetchSize = 200
//query
@Option(names = ["-q", "--query"], description = "query to save. ex.: select * from tableName where id < 6;")
@Field String query
//table
@Option(names = ["-t", "--table"], description = "table to save.")
@Field String table
//output Folder
@Option(names = ["-o", "--output"], description = "path to output")
@Field String outputFolder
//verbose
@Option(names = ["-v", "--verbose"], description = "Print more info on execution")
@Field boolean verbose = false
//quote Character
@Option(names = ["--quote"], description = "CSV Quote Character.")
@Field char quoteCharacter = "\""
//CSV Escape Character
@Option(names = ["--escape"], description = "CSV Quote Character.")
@Field char csvEscapeCharacter = "\""
//CSV Delimiter
@Option(names = ["--delimiter"], description = "CSV delimiter.")
@Field char delimiter = ","
//PostgreSQL Schema
@Option(names = ["--schema"], description = "PostgreSQL Schema.")
@Field String schema = "public"
//help
@Option(names = ["-?", "--help"], usageHelp = true, description = "Show this help message and exit.")
@Field boolean helpRequested

//business logic
def timeStart = new Date()
String DB_DRIVER
String defaultEscapeCharacter
String DB_URL
if(!outputFolder)
    outputFolder = "db2csv_${database}_${new Date()}"
switch (client.toLowerCase()){
    case "mysql":
        DB_DRIVER = "com.mysql.cj.jdbc.Driver"
        defaultEscapeCharacter = "`"
        DB_URL = "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?useCursorFetch=true&useSSL=${ssl}&requireSSL=${ssl}"
        break;
    case "postgresql":
        DB_DRIVER = "org.postgresql.Driver"
        defaultEscapeCharacter = "\""
        DB_URL = "jdbc:postgresql://$hostname:$port/$database?user=$username&password=$password&sslmode=${ssl?"require":"disable"}"
        database = schema //changing this would effect schema
}
/**
    * get Sql instance with given connection configuration
    * @return Sql
    */
Sql getConnection(DB_DRIVER, DB_URL, username, password){
    def sql
    def driver = Class.forName(DB_DRIVER, false, this.class.classLoader).newInstance()
    def properties = new Properties()
    properties.setProperty('user', username)
    properties.setProperty('password', password)
    conn = driver.connect(DB_URL, properties)
    sql = Sql.newInstance(conn)
    return sql
}
String getConnectionQueryString(){
    return "select count(1) FROM information_schema.tables WHERE table_schema = '$database'"
}
String getTableNamesQueryString(){
    return "select table_name FROM information_schema.tables WHERE table_schema = '$database'"
}
def log(props){
    if(verbose){
        println "${new Date()}: $props"
    }
}

def getErrorString(def errStr){
    def error = ''
    // failed, not exist, connect. , UnknownHostException
    if (errStr.indexOf("failed") != -1 || errStr.contains("Access denied"))
        error = "Authentication failed.  Check your username / password."
    else if (errStr.indexOf("not exist") != -1 || errStr.contains("Unknown database"))
        error = "Database name does not exist.  Check your database name."
    else if (errStr.indexOf("UnknownHostException") != -1 || errStr.contains("Communications link failure"))
        error = "Server not found.  Check your host name and port"
    else if(errStr.contains("SSL Connection required, but not supported by server."))
        error = "SSL Connection required, but not supported by server. Try using SSL: no."
    else
        error = "Connection timeout.  Check that your server is up or the port might be incorrect."
    return error
}
/**
* check if connection can be established to the specified Database
*/
boolean canConnect(DB_DRIVER, DB_URL, username, password) {
    def retval = false,sql
    try {
        sql = getConnection(DB_DRIVER, DB_URL, username, password)
        def connectionString = getConnectionQueryString()
        def countRows = sql.firstRow(connectionString)
        if (countRows) {
            retval = true
        }
        log("CONNECTION TEST COMPLETED")
    } catch (err) {
        log(err)
        println getErrorString(err.toString())

    } finally {
        if(sql)
            sql.close()
    }
    return retval
}


if(!canConnect(DB_DRIVER, DB_URL, username, password))
    log( "CONNECTION COULD NOT BE ESTABLISHED")
else
    log("CONNECTION ESTABLISHED")
if(!all_tables && !table)
    println("Please provide a tablename (-t) with or use all tables (-a).")

try{
    //def Writer csvFileWriter = new PrintWriter(new File("$outputFolder/$table"))
    Sql sql = getConnection(DB_DRIVER, DB_URL, username, password)
    sql.withStatement {stmt -> stmt.fetchSize = resultSetfetchSize}
    def tables = []
    if(all_tables)
        sql.eachRow(this.getTableNamesQueryString()){tables << it.table_name}
    else if (table)
        tables << table
    tables.each {
        //Write to CSV
        File f = new File("${outputFolder}/${it}.csv")
        f.getParentFile().mkdirs()
        CSVWriter writer = new CSVWriter(new FileWriter(f), delimiter, quoteCharacter, csvEscapeCharacter);
        query = "select * from $defaultEscapeCharacter$it$defaultEscapeCharacter".toString()
        sql.query(query,{
            writer.writeAll(it,true);
        })
        writer.close()
        log("File: ${f.name} Created for table: $it")
    }
    if(tables.size() == 0)
        log("No tables found in database $database")
}catch(Exception e){
    e.printStackTrace()
}
def timeStop = new Date()
TimeDuration duration = TimeCategory.minus(timeStop, timeStart)
println "Finished. Elapsed time: $duration"












