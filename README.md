# Team O's 4347.002 Project

First, you'll need to get the CSV and the SQL files downloaded then create the database schema. Then run the (SQL file) file from mysql prompt using the “source” command: source (sqlfilename.sql) 

After successfully completing the above steps, change the parameters “host”, “user” , “password” of the java.connect() method to your values in each java file except {java cofe file that gets the csv files) file.

Language: Java
Framework: Spring
Platform: Java Platform, Standard Edition (Java SE)
Software Version: Java JDK 19

How to set up Database: 
1. Get the library.sql file into your terminal, then verify by opening datbase and then using this command 'SHOW TABLE;'
2. Get all SOURCES:
    SOURCE (yourpath)/InsertAuthors.sql
    SOURCE (yourpath)/InsertBooks.sql
    SOURCE (yourpath)/InsertBookAuthors.sql
    LOAD DATA LOCAL INFILE '/Users/(yourpath)/Downloads/author_table.csv' INTO TABLE AUTHORS FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n'(Name, Author_id);
    LOAD DATA LOCAL INFILE '/Users/(yourpath)/Downloads/author_table.csv' INTO TABLE BORROWER FIELDS TERMINATED BY ',' LINES TERMINATED BY                 '\n'(Card_id,ssn,Bname,Address,Phone);'
Once all sources are loaded to the terminal make sure to check the files. (ex. 'SELECT * FROM BORROWER;')

4. Now that we have all sources we need to connect our database to our IDE with JDBC Connection. You need to download the latest jar file:(https://dev.mysql.com/downloads/connector/j/)
Once that is downloaded you need to add the file to your IDE which depends what you use. (ex. I used intellji and added the jar file to my a directory i created under Project Stuctures --> Modules --> Dependencies. Then from there I created a MyJDBC class that called the connections and connected the IDE to my database.




   



