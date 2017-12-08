import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se ha encontrado el archivo de configuracion.");
            alert.setResizable(true);
            alert.showAndWait();
            System.exit(1);
        } catch (IOException e){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ha abido un problema leyendo el archivo de configuracion, verifique que el mismo no este corrupto.");
            alert.setResizable(true);
            alert.showAndWait();
            System.exit(1);
        }

        USERNAME = mainProperties.getProperty("psqldb.usuario");
        PASSWORD = mainProperties.getProperty("psqldb.pass");
        CONNECTIONSTRING = "jdbc:postgresql://"+mainProperties.getProperty("psqldb.host")+"/"+mainProperties.getProperty("psqldb.nombre");

    }

}
