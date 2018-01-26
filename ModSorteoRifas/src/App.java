import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;


public class App {

	public static final String PATH_PROJECT = System.getProperty("user.dir").concat("/");
	public static final String PATH_GENERADOS = PATH_PROJECT.concat("generados/");
	private static final String TOTAL_CSV = PATH_PROJECT.concat("generados/TOTAL.csv");
	private static final String COLISIONES_TXT = PATH_PROJECT.concat("generados/Colisiones.txt");
    private static String FILENAME_PEDIDOS_INICIAL = PATH_PROJECT;
    private static String FILENAME_PEDIDOS_EXTRA = PATH_PROJECT;
    private static Map<Integer,Rifa> rifas = new TreeMap<>();
    private static ArrayList<Favorito> favoritos = new ArrayList<>();
    private static ArrayList<Favorito> favoritosExtra = new ArrayList<>();
	private static Map<Integer,Integrante> integrantes = new TreeMap<>();
    private static ArrayList<String> colisiones = new ArrayList<>();
    private static ArrayList<Extra> extras = new ArrayList<>();

    public static Integer EnteroSeteado = 0;
    public static Integer TerminacionSeteado = 0;
    public static Integer rifadisponibles = 0;

	private static void initialize() throws Exception {
		if (!Files.isDirectory(Paths.get(PATH_GENERADOS))){
			Files.createDirectory(Paths.get(PATH_GENERADOS));
		}
		if(FILENAME_PEDIDOS_INICIAL != PATH_PROJECT){
            initializeFavoritos();
        }
        initializeTitulares();
		initializeRifas();
	}

	private static void initializeFavoritos() {
        try {
            Reader in = new FileReader(FILENAME_PEDIDOS_INICIAL);
            List<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in).getRecords();
            String query = "INSERT INTO \"Favoritos\" (\"ID\", \"Favorito\", \"Integrante\", \"isExtra\") VALUES ";
            for (CSVRecord favRecord : records) {
                if (favRecord.get("Pregunta").toUpperCase().contains("NUMERO")) {
                    boolean isExtra = favRecord.get("Pregunta").toUpperCase().contains("ADICIONAL");
                    Favorito fav = new Favorito(favRecord.get("Respuesta"), favRecord.get("Integrante"), isExtra);
                    String resp = favRecord.get("Respuesta").trim().isEmpty() ? "''" : "'"+favRecord.get("Respuesta")+"'";
                    query += "(uuid_generate_v4(), " + resp + ", " + favRecord.get("Integrante") + ", " + isExtra +"), ";
                }
            }
            query = query.substring(0, query.length()-2);
            Connection dbConnection = Controller.connect();
            Statement stm = dbConnection.createStatement();
            stm.executeUpdate(query);
            dbConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeTitulares() {
        System.out.println("# INICIO - initializeTitulares");
        try {
            Connection dbConnection = Controller.connect();
            Statement stm = dbConnection.createStatement();
            String query = "select * from \"Integrante\"";
            ResultSet result = stm.executeQuery(query);
            dbConnection.close();
            result.next();
            while (!result.isAfterLast()){
                Integrante inte = new Integrante(result.getInt("Numero"), result.getInt("CantRifas"), result.getBoolean("Acompanante"),result.getInt("CantExtra"));
                integrantes.put(inte.getId(),inte);
                result.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("# FIN - initializeTitulares");
    }

    private static void initializeRifas() throws Exception{
        System.out.println("# INICIO - initializeRifas");
        try {
            Connection dbConnection = Controller.connect();
            Statement stm = dbConnection.createStatement();
            String query ="";
            if(FILENAME_PEDIDOS_INICIAL != PATH_PROJECT){
                query = "select * from \"Rifa\" order by random()";
            }else{
                query = "select * from \"Rifa\" Where \"Integrante\" = 888 order by random()";
            }

            System.out.println(query);
            ResultSet result = stm.executeQuery(query);
            dbConnection.close();
            result.next();

            while(!result.isAfterLast()){
                Rifa rifa = new Rifa( result.getInt("Integrante"),result.getInt("Numero"));
                rifas.put(rifa.getNumero(),rifa);
                result.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("# FIN - initializeRifas");
    }

    private static void uploadFavoritos()throws Exception {
	    try{
            Connection dbConnection = Controller.connect();
            Statement stm = dbConnection.createStatement();
            String query = "select * from \"Favoritos\" Order by random()";
            System.out.println(query);
            ResultSet result = stm.executeQuery(query);
            dbConnection.close();
            result.next();

            while (!result.isAfterLast()) {
                Favorito favo = new Favorito(result.getString("Favorito"), String.valueOf(result.getInt("Integrante")), result.getBoolean("isExtra"));
                if(favo.isExtra){
                    favoritosExtra.add(favo);
                }else{
                    favoritos.add(favo);
                }
                result.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	private static void executeSorteo() throws Exception {
	    uploadFavoritos();
        try {
            for(Favorito fav : favoritos){
                if(fav.isValid()){
                    if(fav.favorito.length() > 3){
                        setEntero(fav);
                    }
                }
            }
            for(Favorito fav : favoritos){
                if(fav.isValid()){
                    if(fav.favorito.length() == 3){
                        setTerminacion(fav);
                    }
                }
            }

            for(Favorito fav : favoritosExtra){
                if(fav.isValid()){
                    if(fav.favorito.length() > 3){
                        setEntero(fav);
                    }
                }
            }
            for(Favorito fav : favoritosExtra){
                if(fav.isValid()){
                    if(fav.favorito.length() == 3){
                        setTerminacion(fav);
                    }
                }
            }

            List<Rifa> auxRifas = new ArrayList<>(rifas.values());
            Collections.shuffle(auxRifas);
            for(Integrante inte : integrantes.values()){
                for(Rifa rif : auxRifas){
                    if(rif.getIntegrante() == 0){
                        rif.setIntegrante(inte.getId());
                        inte.getRifas().add(rif);
                    }
                    if(inte.isConAcompanante() && inte.getRifas().size() == 200){
                        break;
                    }
                    else if(!inte.isConAcompanante() && inte.getRifas().size() == 100){
                        break;
                    }
                }
            }

            for(Rifa rif : auxRifas){
                if(rif.getIntegrante() == 0){
                    rif.setIntegrante(888);
                    rifadisponibles +=1;
                }
            }
            updateRifas();
        } catch (Exception e) {
            e.printStackTrace();
        }

        printTotalRifas2File();
	}

	private static void setTerminacion(Favorito fav){
        List<Rifa> terminacionRifas = rifas.values().stream().filter(p-> String.valueOf(p.getNumero()).endsWith((fav.favorito))).collect(Collectors.toList());
        for(Rifa rif : terminacionRifas){
            Integrante inte = integrantes.get(Integer.valueOf(fav.integrante));
            if(rif.getIntegrante() == 0){
                if((inte.isConAcompanante() && inte.getRifas().size() == 10) || (!inte.isConAcompanante() && inte.getRifas().size() == 5)){
                    break;
                }
                rif.setIntegrante(inte.getId());
                inte.getRifas().add(rif);
                TerminacionSeteado += 1;
                break;
            }
        }
    }

    private static void setEntero(Favorito fav){
        Rifa rif =  rifas.get(Integer.valueOf(fav.favorito));
        Integrante inte = integrantes.get(Integer.valueOf(fav.integrante));
        if(rif != null && inte != null){
            if(rif.getIntegrante() != 0){
                colisiones.add("Rifa "+rif.getNumero()+" pedida por "+fav.integrante +" ya asignada a "+rif.getIntegrante());
            }else{
                if((inte.isConAcompanante() && inte.getRifas().size() < 10) || (!inte.isConAcompanante() && inte.getRifas().size() < 5)){
                    if(inte.getId() == null){
                        System.out.println();
                    }
                    rif.setIntegrante(inte.getId());
                    inte.getRifas().add(rif);
                    EnteroSeteado +=1;
                }
            }
        }
    }

    private static void updateRifas(){
        try {
            String query = "UPDATE  \"Rifa\" SET \"Numero\" = c.rifaNumero, \"Integrante\" = c.integranteNumero from (values ";
            for (Rifa rifa : rifas.values()) {
                query += "(" + rifa.getNumero() + ", " + rifa.getIntegrante() + "), ";
            }
            query = query.substring(0, query.length() - 2);
            query +=") as c(rifaNumero, integranteNumero) where c.rifaNumero = \"Numero\" ";

            Connection dbConnection = Controller.connect();
            Statement stm = dbConnection.createStatement();
            stm.executeUpdate(query);
            dbConnection.close();
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

	private static void printResume() {
		System.out.println();
        Main.logMessage("# ***************************************");
        Main.logMessage("# Total enteros: "+EnteroSeteado);
        Main.logMessage("# Total terminaciones: "+TerminacionSeteado);
        Main.logMessage("# Rifas disponibles: "+rifadisponibles);
        Main.logMessage("# ***************************************");
		System.out.println();
	}

    private static void printTotalRifas2File() throws IOException {
        try{
            List<String> lines2 = new ArrayList<>();
            lines2.add("RIFA,ID INTEGRANTE");
            for (Rifa rifa : rifas.values()) {
                lines2.add(rifa.getNumero() + "," + rifa.getIntegrante());
            }
            Path file2 = Paths.get(TOTAL_CSV);
            Files.write(file2, lines2, Charset.forName("UTF-8"));
            System.out.println( "# FIN - Creación archivo mapa CSV con todas las asignaciones en " + TOTAL_CSV);


            // Colisiones
            List<String> lines3 = new ArrayList<>();
            lines3.addAll(colisiones);
            Path file3 = Paths.get(COLISIONES_TXT);
            Files.write(file3, lines3, Charset.forName("UTF-8"));
            System.out.println( "# FIN - Creación archivo con colisiones en " + COLISIONES_TXT);
        } catch (IOException e) {
            throw e;
        }
    }

    private static void sorteoExtra() throws IOException {
        Reader in = new FileReader(FILENAME_PEDIDOS_EXTRA);
        List<CSVRecord> records = CSVFormat.RFC4180.withHeader().parse(in).getRecords();
        for (CSVRecord extRecord : records) {
            if (extRecord.get("Pregunta").toUpperCase().contains("CUANTAS RIFAS")) {
                Extra ext = new Extra(Integer.valueOf(extRecord.get("Integrante")), Integer.valueOf(extRecord.get("Respuesta")));
                extras.add(ext);
            }
        }
        List<Rifa> rifasExtras = new ArrayList<>(rifas.values());
        for(Extra ext : extras){
            Integrante inte = integrantes.get(ext.getIntegrante());
            if(inte.getCantExtra() == 0 || (inte.isConAcompanante() && inte.getCantExtra() == 1)){
                for (int i = 0; i < ext.getCantExtra(); i++) {
                    rifasExtras.get(i).setIntegrante(ext.getIntegrante());
                    rifasExtras.remove(i);
                }
                inte.setCantExtra(inte.getCantExtra()+1);
            }
        }
        updateRifas();
        printTotalRifas2File();
    }

	// ******************
	// PROGRAMA PRINCIPAL
	// ******************
	
    public static void sortear( String[] args ) throws Exception {

        if (args.length == 2 && args[0] != null && args[1] != null){
            FILENAME_PEDIDOS_EXTRA = args[0];
        } else if (args.length == 1 && args[0] != null){
            FILENAME_PEDIDOS_INICIAL = args[0];
        }
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(out);
        System.out.println();
        System.out.println( "# INICIO PROGRAMA" );
        Date fecha = new Date();
        System.out.println("# FECHA: " + fecha);
        initialize();
        if(FILENAME_PEDIDOS_INICIAL != PATH_PROJECT){
            executeSorteo();
            printResume();
        }else{
            sorteoExtra();
        }
        System.out.println( "# FIN PROGRAMA" );
    }
}
