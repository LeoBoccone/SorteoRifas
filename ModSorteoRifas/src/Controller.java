import java.sql.*;

public class Controller {

    public static Connection connect() {
        Connection c = null;
        String user = PSQLHelper.USERNAME;
        String password = PSQLHelper.PASSWORD;
        String driver = PSQLHelper.DRIVER;
        String connectionString = PSQLHelper.CONNECTIONSTRING;
        try {
            Class.forName(driver);
            c = DriverManager
                    .getConnection(connectionString,
                            user, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }
        if (c == null){
            System.exit(1);
        }
        return c;
    }
}
