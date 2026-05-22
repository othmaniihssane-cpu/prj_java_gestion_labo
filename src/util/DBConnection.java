package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/laboratoire?useSSL=false&allowPublicKeyRetrieval=true",
                "admin",
                "admin"
        );
    }
}
