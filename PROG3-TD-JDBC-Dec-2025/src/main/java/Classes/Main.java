package Classes;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DBConnection dbConnection = new DBConnection();
        try {
            System.out.println("Connection successful");
            System.out.println(dbConnection.getDBConnection());
        } catch (SQLException e) {
            throw new RuntimeException("err : " + e);
        }
    }
}