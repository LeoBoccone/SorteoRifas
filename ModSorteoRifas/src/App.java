import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;


public class App
{
	public static final String PATH_PROJECT = System.getProperty("user.dir").concat("/");
	public static final String PATH_GENERADOS = PATH_PROJECT.concat("generados/");
	private static final String TOTAL_CSV = PATH_PROJECT.concat("generados/TOTAL.csv");
	private static final String TOTAL_AUN_DISPONIBLES_TXT = PATH_PROJECT.concat("generados/TOTAL_AUN_DISPONIBLES.txt");
    private static final String FILENAME_TITULARES = PATH_PROJECT.concat("Titulares.txt");
    private static final String FILENAME_ACOMPANANTES = PATH_PROJECT.concat("Acompanantes.txt");
    private static String FILENAME_PEDIDOS_INICIAL = PATH_PROJECT;
    private static String FILENAME_PEDIDOS_EXTRA = PATH_PROJECT;
	private static String FILENAME_RIFAS_DISPONIBLES = PATH_PROJECT;

	private static Integer primerRifa = 1000;
	private static Integer ultimaRifa = 80999;
	private static final Integer MAX_INTENTOS_TERMINACION = 1000;
	private static final Integer CANT_RIFAS_POR_INTEGRANTE = 100;
	private static final Integer MAX_RIFAS_A_PEDIR = 5;

	private static Integer completos = 0;
	private static Integer cant_titulares = 0;
    private static Integer cant_acompanantes = 0;
    private static Integer cant_rifas_pedidas_total = 0;
	private static Random rnd = new Random();
	private static SortedMap<Integer, Integer> rifasAsignadas = new TreeMap<>();
	private static ArrayList<Integer> rifasDisponibles = new ArrayList<>();
	private static ArrayList<Integrante> integrantes = new ArrayList<>();
    private static ArrayList<Integrante> integrantesPrimerasRifas = new ArrayList<>();
	
	
	// **************
	// INICIALIZACIÓN
	// **************

	private App(){}

	private static void initialize() throws Exception {
		if (!Files.isDirectory(Paths.get(PATH_GENERADOS))){
			Files.createDirectory(Paths.get(PATH_GENERADOS));
		}
        initializeTitulares();
        initializeAcompanantes();
        if (!FILENAME_PEDIDOS_EXTRA.equals(PATH_PROJECT) && !FILENAME_RIFAS_DISPONIBLES.equals(PATH_PROJECT)) {
            initializePedidosExtra();
        }
		initializeRifas();
        Main.logMessage("# Rifas pedidas = " + cant_rifas_pedidas_total + " - Rifas disponibles sin pedir: " + rifasDisponibles.size());
		if (cant_rifas_pedidas_total > rifasDisponibles.size()) {
			throw new Exception("ERROR: Hay más rifas pedidas que las disponibles.");
		}
	}

    private static void initializeTitulares() throws IOException{
        System.out.println("# INICIO - initializeTitulares");
        String idTitularStr;
        Integer idTitular;
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME_TITULARES))) {
            while ((idTitularStr = br.readLine()) != null) {
                idTitular = Integer.valueOf(idTitularStr);
                Integrante integrante = new Integrante(idTitular, CANT_RIFAS_POR_INTEGRANTE);
                integrantes.add(integrante);
                integrantesPrimerasRifas.add(integrante);
                cant_titulares++;
            }
        } catch (IOException e) {
            throw e;
        }
        System.out.println("# FIN - initializeTitulares");
    }

    private static void initializeAcompanantes() throws IOException{
        System.out.println("# INICIO - initializeAcompanantes");
        String idTitularAsociado;
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME_ACOMPANANTES))) {
            while ((idTitularAsociado = br.readLine()) != null) {
                Integrante integranteTitular = findIntegranteById(Integer.valueOf(idTitularAsociado), true);
                if (integranteTitular == null) {
                    System.out.println("ERROR: El titular número " + idTitularAsociado + " no existe por lo tanto no se le puede asociar acompañante. Verifique nuevamente");
                }
                integranteTitular.setConAcompanante(true);
                integranteTitular.setCantRifasASortear(integranteTitular.getCantRifasASortear()*2);
                cant_acompanantes++;
            }
        } catch (IOException e) {
            throw e;
        }
        System.out.println("# FIN - initializeAcompanantes");
    }
	
	private static void initializePedidosExtra() throws IOException{
		System.out.println("# INICIO - initializePedidosExtra");

		if (Files.exists(Paths.get(FILENAME_PEDIDOS_EXTRA))) {
            try (BufferedReader br = new BufferedReader(new FileReader(FILENAME_PEDIDOS_EXTRA))) {
                String linea, idTitularStr, cantRifasPedidas;
                while ((linea = br.readLine()) != null) {
                    idTitularStr = linea.split("=")[0];
                    cantRifasPedidas = linea.split("=")[1];
                    cant_rifas_pedidas_total += Integer.valueOf(cantRifasPedidas);
                    Integrante integrante = new Integrante(Integer.valueOf(idTitularStr), Integer.valueOf(cantRifasPedidas));
                    integrantes.add(integrante);
                    cant_titulares++;
                }
            } catch (IOException e) {
                throw e;
            }
        }
    	System.out.println("# FIN - initializePedidosExtra");
    }
	
	private static void initializeRifas() throws Exception{
		System.out.println("# INICIO - initializePrimerasRifas");

        for (Integer i=primerRifa;i<=ultimaRifa;i++){
            rifasDisponibles.add(i);
        }
        if (!FILENAME_PEDIDOS_INICIAL.equals(PATH_PROJECT)) {
            Main.logMessage("# Rifas emitidas inicialmente: " + rifasDisponibles.size());
            initializeAsignacionRifasPedidas();
        }
        else if (!FILENAME_PEDIDOS_EXTRA.equals(PATH_PROJECT) && !FILENAME_RIFAS_DISPONIBLES.equals(PATH_PROJECT)) {
            try (BufferedReader br = new BufferedReader(new FileReader(FILENAME_RIFAS_DISPONIBLES))) {
                String rifaStr;
                Integer rifa;
                while ((rifaStr = br.readLine()) != null) {
                    rifa = Integer.valueOf(rifaStr);
                    rifasDisponibles.add(rifa);
                    if (rifa < primerRifa) {
                        primerRifa = rifa;
                    }
                    if (rifa > ultimaRifa) {
                        ultimaRifa = rifa;
                    }
                }
            } catch (IOException e) {
                throw e;
            }
        }

        System.out.println("# FIN - initializePrimerasRifas");
    }

    private static void initializeAsignacionRifasPedidas() throws Exception {
        System.out.println("# INICIO - initializeAsignacionRifasPedidas");
        boolean ok;
        try (BufferedReader br = new BufferedReader(new FileReader(FILENAME_PEDIDOS_INICIAL))) {
            String linea, rifa, termination, idIntegrante;
            while ((linea = br.readLine()) != null) {
                rifa = linea.split("=")[0];
                idIntegrante = linea.split("=")[1];
                // TERMINACIÓN
                if (rifa.contains("*")){
                    System.out.println("----------------------------");
                    System.out.println("Terminación inicial: " + rifa);
                    termination = rifa.substring(1);
                    rifa = sortTermination(termination).toString();
                    Integer intentos = 0;
                    while ((!isRifaValida(rifa) || isRifaAsignada(Integer.valueOf(rifa))) && intentos < MAX_INTENTOS_TERMINACION) {
                        rifa = sortTermination(termination).toString();
                        intentos++;
                    }
                    if (intentos.equals(MAX_INTENTOS_TERMINACION)) {
                        Main.logMessage("NO ES POSIBLE ENCONTRAR OTRO NÚMERO CON TERMINACIÓN " + termination + ". ESTÁN TODOS RESERVADOS");
                        throw new Exception("NO FUE POSIBLE ENCONTRAR OTRO NÚMERO CON TERMINACIÓN " + termination + ". ESTÁN TODOS RESERVADOS");
                    } else {
                        System.out.println("Número generado: " + rifa);
                    }
                    System.out.println("----------------------------");
                }
                // FIN TERMINACIÓN
                ok = asignarPedida(Integer.valueOf(idIntegrante), Integer.valueOf(rifa));
                if (!ok) {
                    Main.logMessage("****** NO FUE POSIBLE ASIGNAR LA RIFA " + rifa + " AL INTEGRANTE " + idIntegrante);
                    Exception e = new Exception();
                    throw e;
                }
            }
        } catch (IOException e) {
            throw e;
        }
        System.out.println("# FIN - initializeAsignacionRifasPedidas");
    }

    private static boolean isRifaValida(String rifa) {
        return (Integer.valueOf(rifa) >= primerRifa && Integer.valueOf(rifa) <= ultimaRifa);
    }
	
	// **********
	// ASIGNACIÓN
	// **********

    private static boolean isRifaAsignada(Integer rifa){
        if (rifasAsignadas.get(rifa) == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean asignarPedida(Integer idIntegrante, Integer rifa) throws Exception {
        if (isRifaAsignada(rifa)) {
            System.out.println("ERROR: Esta rifa ya está reservada para otro integrante. Numero rifa: " + rifa);
            System.out.println("ERROR: Integrantes que la pidieron:  " + rifasAsignadas.get(rifa) + " y " + idIntegrante);
            return false;
        }
        cant_rifas_pedidas_total += 1;
        Integrante integrante = findIntegranteById(idIntegrante, true);
        if (integrante == null) {
            integrante = findIntegranteById(idIntegrante, false);
            if (integrante == null) {
                System.out.println("ERROR: El integrante "  + idIntegrante + " no existe.");
            } else {
                System.out.println("ERROR: El integrante "  + idIntegrante + " pidió más rifas que el máximo permitido para él que es " + getMaxRifasAPedir(integrante));
            }
            return false;
        }
        asignarRifa(rifa, integrante);
        if (integrante.getSizeRifas() == getMaxRifasAPedir(integrante)) {
            integrantesPrimerasRifas.remove(integrante);
        }
        return true;
    }

	private static void asignarRifa(Integer rifa, Integrante integrante) throws Exception {
        if (rifa < primerRifa || rifa > ultimaRifa) {
            Main.logMessage("****** EL NÚMERO DE RIFA " + rifa + " NO ESTÁ EN EL RANGO DE RIFAS VÁLIDAS: (" + primerRifa + "," + ultimaRifa + ")");
            Exception e = new Exception();
            throw e;
        } else {
            integrante.addRifa(rifa);
            rifasAsignadas.put(rifa, integrante.getId());
            rifasDisponibles.remove(rifa);
            if (integrante.getSizeRifas().equals(integrante.getCantRifasASortear())) {
                completos++;
            }
        }
	}

    private static Integer getMaxRifasAPedir(Integrante integrante){
        if (integrante.isConAcompanante()){
            return 2*MAX_RIFAS_A_PEDIR;
        } else {
            return MAX_RIFAS_A_PEDIR;
        }
    }

    private static Integer getRifasASortear(Integrante integrante){
        if (integrante.isConAcompanante()){
            return 2*CANT_RIFAS_POR_INTEGRANTE;
        } else {
            return CANT_RIFAS_POR_INTEGRANTE;
        }
    }

    private static Integer sortDigit() {
        Integer digit = Integer.valueOf((int) (rnd.nextDouble() * 10));
        return digit;
    }

    private static Integer sortTermination(String termination) {
        Integer length = termination.length();
        switch (length) {
            case 1:
                termination = (sortDigit().toString()).concat(termination);
                termination = String.valueOf(sortTermination(termination));
                break;
            case 2:
                termination = (sortDigit().toString()).concat(termination);
                termination = String.valueOf(sortTermination(termination));
                break;
            case 3:
                termination = (sortDigit().toString()).concat(termination);
                termination = String.valueOf(sortTermination(termination));
                break;
            case 4:
                termination = (sortDigit().toString()).concat(termination);
                termination = String.valueOf(sortTermination(termination));
                break;
            case 5:
                break;

            default:
                break;
        }

        return Integer.valueOf(termination);
    }
	
	// ***********
	// IMPRESIONES
	// ***********
	
	private static void printRifasXIntegrantes2Files() throws IOException {
		for (Integrante integrante : integrantes) {
			integrante.printRifas2File();
			//integrante.printRifas2PDF();
		}
		System.out.println( "# FIN - Creación archivos Integrantes con sus asignaciones");
	}
	
	private static void printTotalRifas2File() throws IOException {
		try{
			Integer idIntegrante;
			List<String> lines = new ArrayList<String>();
			lines.add("RIFA - ID INTEGRANTE");
			for (Integer rifa : rifasAsignadas.keySet()) {
				idIntegrante = rifasAsignadas.get(rifa);
				lines.add(rifa + "-" + idIntegrante);
			}
			/*
			Path file = Paths.get(TOTAL_TXT);
			Files.write(file, lines, Charset.forName("UTF-8"));
			System.out.println( "# FIN - Creación archivo mapa con todas las asignaciones en " + TOTAL_TXT);
			*/
			
			List<String> lines2 = new ArrayList<String>();
			lines2.add("RIFA;ID INTEGRANTE");
			for (Integer rifa : rifasAsignadas.keySet()) {
				idIntegrante = rifasAsignadas.get(rifa);
				lines2.add(rifa + ";" + idIntegrante);
			}
			Path file2 = Paths.get(TOTAL_CSV);
			Files.write(file2, lines2, Charset.forName("UTF-8"));
			System.out.println( "# FIN - Creación archivo mapa CSV con todas las asignaciones en " + TOTAL_CSV);
			

			// TOTAL AUN DISPONIBLES
			List<String> lines3 = new ArrayList<String>();
			for (Integer rifa : rifasDisponibles) {
				lines3.add(rifa + "-" + "null");
			}
			Path file3 = Paths.get(TOTAL_AUN_DISPONIBLES_TXT);
			Files.write(file3, lines3, Charset.forName("UTF-8"));
			System.out.println( "# FIN - Creación archivo mapa con todas las rifas aun disponibles en " + TOTAL_AUN_DISPONIBLES_TXT);
		} catch (IOException e) {
		   throw e;
		}
	}
	
	
	private static void saveOutput() throws IOException {
		printTotalRifas2File();
    	//printRifasXIntegrantes2Files();
	}

	
	// **********
	// AUXILIARES
	// **********
	
	private static Integer nextIntegrante(Integer posIntegrante, Integer tope) {
		if (posIntegrante >= tope - 1){
			posIntegrante = 0;
		} else {
			posIntegrante++;
		}
		return posIntegrante;
	}
	
	private static Integer maxCantRifas() {
		Integer max = 0;
		for (Integrante integrante : integrantes) {
			if (integrante.getSizeRifas() > max)
				max = integrante.getSizeRifas();
		}
		return max;
	}
	
	private static Integer minCantRifas() {
		Integer min = 99999;
		for (Integrante integrante : integrantes) {
			if (integrante.getSizeRifas() < min)
				min = integrante.getSizeRifas();
		}
		return min;
	}

    private static Integrante findIntegranteById(Integer idBuscado, boolean primerasRifas){
        if (primerasRifas){
            for (Integrante integrante : integrantesPrimerasRifas) {
                if (integrante.getId().equals(idBuscado)) {
                    return integrante;
                }
            }
        } else {
            for (Integrante integrante : integrantes) {
                if (integrante.getId().equals(idBuscado)) {
                    return integrante;
                }
            }
        }
        System.out.println("ERROR: Integrante con ID " + idBuscado + " no encontrado");
        return null;
    }

	// ******
	// SORTEO
	// ******
	
	private static void executeSorteo() throws Exception {
    	Integer rifa, posRifa, posIntegrante = 0;
    	
    	while (rifasDisponibles.size() > 0 && completos < cant_titulares){
			posRifa = (int) (rnd.nextDouble() * rifasDisponibles.size());
			rifa = rifasDisponibles.get(posRifa);
			Integrante integrante = integrantes.get(posIntegrante);
			while ((integrante.getSizeRifas() >= integrante.getCantRifasASortear()) && completos != cant_titulares) {
				posIntegrante = nextIntegrante(posIntegrante, cant_titulares);
				integrante = integrantes.get(posIntegrante);
			}
    		asignarRifa(rifa, integrante);
    		posIntegrante = nextIntegrante(posIntegrante, cant_titulares);
    	}
    	System.out.println("# Rifas disponibles post-sorteo: " + rifasDisponibles.size());
    	saveOutput();
	}
	
	private static void printResume() {
		System.out.println();
        Main.logMessage("# ***************************************");
        Main.logMessage("# Total titulares: " + cant_titulares);
        Main.logMessage("# Total acompañantes: " + cant_acompanantes);
        Main.logMessage("# Rifas sorteadas: " + rifasAsignadas.size());
        Main.logMessage("# Rifas disponibles: " + rifasDisponibles.size());
        Main.logMessage("# Max cantidad rifas integrante: " + maxCantRifas());
        Main.logMessage("# Min cantidad rifas integrante: " + minCantRifas());
        Main.logMessage("# ***************************************");
		System.out.println();
	}

	// ******************
	// PROGRAMA PRINCIPAL
	// ******************
	
    public static void sortear( String[] args ) throws Exception
    {
        if (args.length == 2 && args[0] != null && args[1] != null){
            FILENAME_PEDIDOS_EXTRA = FILENAME_PEDIDOS_EXTRA.concat(args[0]);
            FILENAME_RIFAS_DISPONIBLES = FILENAME_RIFAS_DISPONIBLES.concat(args[1]);
        } else if (args.length == 1 && args[0] != null){
            FILENAME_PEDIDOS_INICIAL = FILENAME_PEDIDOS_INICIAL.concat(args[0]);
        }
        Main.logMessage("******************** FILENAME_PEDIDOS_EXTRA: " + FILENAME_PEDIDOS_EXTRA);
        Main.logMessage("******************** FILENAME_RIFAS_DISPONIBLES: " + FILENAME_RIFAS_DISPONIBLES);
        PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(out);
    	System.out.println();
    	System.out.println( "# INICIO PROGRAMA" );
		Date fecha = new Date();
		System.out.println("# FECHA: " + fecha);
    	initialize();
    	executeSorteo();
    	printResume();
    	System.out.println( "# FIN PROGRAMA" );
    }

}
