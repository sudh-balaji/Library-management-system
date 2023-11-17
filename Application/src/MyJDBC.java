import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 * This connects the MySQL Database to the IDE
 * @createdBy: Aileen Mata
 * @createdDate: 11/16/23
 */

public class MyJDBC {
    private Connection connection;

    public MyJDBC() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library",
                    "root", "AddUrPassWord"); //add you password here
                    System.out.println(connection);
        } catch (SQLException e) {
            e.printStackTrace(); // Handle error connection
        }
    }
    public Connection getConnection() {
        return this.connection;
    }
    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle closure errors appropriately
        }
    }
}
