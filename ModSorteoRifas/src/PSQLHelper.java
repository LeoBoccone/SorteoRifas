import java.io.*;
import java.util.Properties;

/**
 * Created by emile on 17/10/15.
 */
public class PSQLHelper {

    public static String USERNAME;
    public static String PASSWORD;
    public static String CONNECTIONSTRING;
    public static String DRIVER = "org.postgresql.Driver";

    public static void psqlSetVars(){

        Properties mainProperties = new Properties();

        File jarPath=new File(PSQLHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String propertiesPath=jarPath.getParentFile().getAbsolutePath();
        try {
            mainProperties.load(new FileInputStream(propertiesPath + "/SorteoRifas.properties"));
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        } catch (IOException e){
            System.out.println("ioexeption");
        }

        USERNAME = mainProperties.getProperty("psqldb.usuario");
        PASSWORD = mainProperties.getProperty("psqldb.pass");
        CONNECTIONSTRING = "jdbc:postgresql://"+mainProperties.getProperty("psqldb.host")+"/"+mainProperties.getProperty("psqldb.nombre");

    }

}
