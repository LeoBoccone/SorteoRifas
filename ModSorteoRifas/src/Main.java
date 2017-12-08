import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Main extends JPanel
                             implements ActionListener {
    static private final String newline = "\n";
    private JButton botonPedidosIniciales, botonPedidosExtra, botonRifasDisponibles, botonSortear;
    private static JTextArea log;
    private JFileChooser fc;
    private boolean pedidosInicialesDefinida = false;
    private boolean pedidosExtraDefinida = false;
    private boolean rifasDisponiblesDefinida = false;
    public static String FILENAME_PEDIDOS_INICIALES_AUX = null;
    public static String FILENAME_PEDIDOS_EXTRA_AUX = null;
    public static String FILENAME_RIFAS_DISPONIBLES_AUX = null;

    public Main() {
        super(new BorderLayout());

        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        fc = new JFileChooser();

        botonPedidosIniciales = new JButton("Cargar pedidos iniciales");
        botonPedidosIniciales.addActionListener(this);

        botonPedidosExtra = new JButton("Cargar pedidos extra");
        botonPedidosExtra.addActionListener(this);
        
        botonRifasDisponibles = new JButton("Cargar rifas disponibles");
        botonRifasDisponibles.addActionListener(this);
        
        botonSortear = new JButton("SORTEAR !");
        botonSortear.addActionListener(this);

        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(botonPedidosIniciales);
        buttonPanel.add(botonPedidosExtra);
        buttonPanel.add(botonRifasDisponibles);
        buttonPanel.add(botonSortear);

        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == botonSortear) {
            if (pedidosInicialesDefinida) {
                logMessage("Se va a realizar un sorteo inicial con los números Pedidos iniciales.");
                String[] parameters = {FILENAME_PEDIDOS_INICIALES_AUX};
                try {
                    App.sortear(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                logMessage("FIN");
            }
            else {
                if (pedidosExtraDefinida && rifasDisponiblesDefinida) {
                    logMessage("Se va a realizar el sorteo de las rifas extas.");
                    String[] parameters = {FILENAME_PEDIDOS_EXTRA_AUX, FILENAME_RIFAS_DISPONIBLES_AUX};
                    try {
                        App.sortear(parameters);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!pedidosExtraDefinida && !rifasDisponiblesDefinida) {
                        logMessage("Todavía no se seleccionó ningún archivo.");
                    } else if (pedidosExtraDefinida) {
                        logMessage("Todavía no se seleccionó el archivo de Disponibles.");
                    } else if (rifasDisponiblesDefinida) {
                        logMessage("Todavía no se seleccionó el archivo de Pedidos.");
                    }
                }
                log.setCaretPosition(log.getDocument().getLength());
            }
        } else {
            int returnVal = fc.showOpenDialog(Main.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (actionEvent.getSource() == botonPedidosIniciales) {
                    FILENAME_PEDIDOS_INICIALES_AUX = file.getName();
                    pedidosInicialesDefinida = true;
                    logMessage("Se cargó el archivo de Pedidos Iniciales: " + file.getName() + ".");
                } else if (actionEvent.getSource() == botonPedidosExtra) {
                    FILENAME_PEDIDOS_EXTRA_AUX = file.getName();
                    pedidosExtraDefinida = true;
                    logMessage("Se cargó el archivo de Pedidos Extra: " + file.getName() + ".");
                } else if (actionEvent.getSource() == botonRifasDisponibles) {
                    FILENAME_RIFAS_DISPONIBLES_AUX = file.getName();
                    rifasDisponiblesDefinida = true;
                    logMessage("Se cargó el archivo de Rifas Disponibles: " + file.getName() + ".");

                } else {
                    logMessage("Acción cancelada por el usuario.");
                }
                log.setCaretPosition(log.getDocument().getLength());
            }

        }
    }

    public static void logMessage(String message){
        log.append(message + newline);
        System.err.println("--- " + message);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Sorteo Rifas Extra - CCEEA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new Main());

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}
