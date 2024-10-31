import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static TablaSimbolos tabla;
    private static List<ErrorSemantico> errores;
    private static List<String[]> tablaTriplos;
    private static JTextArea textArea; // JTextArea para mostrar y editar el contenido del archivo

    public static void main(String[] args) {
        tabla = new TablaSimbolos();
        errores = new ArrayList<>();
        tablaTriplos = new ArrayList<>(); // Tabla para triplos

        // Mostrar las tablas en un JFrame
        SwingUtilities.invokeLater(Main::mostrarInterfaz);
    }

    private static void mostrarInterfaz() {
        textArea = new JTextArea(10, 50);
        textArea.setEditable(true); // Permitir edición
        JScrollPane scrollPane = new JScrollPane(textArea);

        JFrame frame = new JFrame("Analizador Semántico");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Tablas de símbolos, errores y triplos en pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tabla de Símbolos", crearTablaSimbolos());
        tabbedPane.addTab("Tabla de Errores", crearTablaErrores());
        tabbedPane.addTab("Tabla de Triplos", crearTablaTriplos());

        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.WEST); // Agregar el JScrollPane con el JTextArea a la izquierda

        // Panel de botones para seleccionar archivo y compilar
        JPanel buttonPanel = new JPanel();
        JButton btnCargar = new JButton("Cargar");
        JButton btnCompilar = new JButton("Compilar");
        
        buttonPanel.add(btnCargar);
        buttonPanel.add(btnCompilar);
        frame.add(buttonPanel, BorderLayout.NORTH);

        // Acción para cargar un archivo
        btnCargar.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                cargarDatosDesdeArchivo(selectedFile.getAbsolutePath());
            }
        });

        // Acción para compilar el texto del JTextArea
        btnCompilar.addActionListener((ActionEvent e) -> {
            // Limpia las tablas antes de compilar
            errores.clear();
            tablaTriplos.clear();
            tabla = new TablaSimbolos(); // Reiniciar tabla de símbolos

            // Obtener el texto directamente del JTextArea
            String textoEntrada = textArea.getText();

            // Procesar el texto ingresado directamente en el JTextArea
            String[] lineas = textoEntrada.split("\n");
            for (int numeroLinea = 0; numeroLinea < lineas.length; numeroLinea++) {
                procesarLinea(tabla, errores, lineas[numeroLinea].trim(), numeroLinea + 1);
            }

            GeneradorTriploDinamico.procesarTexto(textoEntrada, tablaTriplos);
            
            // Actualiza las tablas en la interfaz
            actualizarTablas(tabbedPane);
        });

        frame.setVisible(true);
    }

    private static void cargarDatosDesdeArchivo(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            StringBuilder contenido = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n"); // Cargar contenido al StringBuilder
            }
            textArea.setText(contenido.toString()); // Mostrar contenido en el JTextArea
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al leer el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void actualizarTablas(JTabbedPane tabbedPane) {
        // Actualiza la tabla de símbolos
        tabbedPane.setComponentAt(0, crearTablaSimbolos());
        // Actualiza la tabla de errores
        tabbedPane.setComponentAt(1, crearTablaErrores());
        // Actualiza la tabla de triplos
        tabbedPane.setComponentAt(2, crearTablaTriplos());
    }

    private static JScrollPane crearTablaSimbolos() {
        String[] columnNames = {"Nombre", "Tipo"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        // Agregar los elementos de la tabla de símbolos al modelo
        for (var entry : tabla.getElementos()) {
            model.addRow(new Object[]{entry.getNombre(), entry.getTipo()});
        }

        JTable table = new JTable(model);
        return new JScrollPane(table);
    }

    private static JScrollPane crearTablaErrores() {
        String[] columnNames = {"Token", "Lexema", "Renglón", "Descripción"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Agregar los errores al modelo de la tabla
        for (ErrorSemantico error : errores) {
            model.addRow(new Object[]{error.getToken(), error.getLexema(), error.getRenglon(), error.getDescripcion()});
        }

        JTable table = new JTable(model);
        return new JScrollPane(table);
    }

    private static JScrollPane crearTablaTriplos() {
        String[] columnNames = {"ID", "Dato Objeto", "Dato Fuente", "Operador"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
    
        // Contador para el ID, que se autoincrementará
        int id = 1;
    
        // Agregar los triplos al modelo de la tabla
        for (String[] triplo : tablaTriplos) {
            // Cada fila debe incluir el ID autoincrementado y los datos de la tabla de triplos
            model.addRow(new Object[]{id++, triplo[0], triplo[1], triplo[2]});
        }
    
        JTable table = new JTable(model);
        return new JScrollPane(table);
    }

    private static void procesarLinea(TablaSimbolos tabla, List<ErrorSemantico> errores, String linea, int numeroLinea) {
        if (linea.contains("=")) {
            String[] partes = linea.split("=");
            if (partes.length == 2) {
                String nombre = partes[0].trim();
                String valor = partes[1].trim();
                String tipoValor = procesarExpresion(tabla, errores, valor, numeroLinea);
                tabla.agregarElemento(nombre, tipoValor);
            } else {
                String nombre = partes[0].trim();
                tabla.agregarElemento(nombre, "Vacio");
            }
        } else {
            procesarExpresion(tabla, errores, linea, numeroLinea);
        }
    }

    private static String procesarExpresion(TablaSimbolos tabla, List<ErrorSemantico> errores, String expresion, int numeroLinea) {
        String[] elementos = expresion.split("\\s+|(?=[=+\\-*\\/<=!&()])|(?<=[=+\\-*\\/<=!&()])");
        String tipoResultado = "Desconocido"; 
        boolean hayErrores = false;

        for (String elem : elementos) {
            if (!elem.isEmpty()) {
                String tipo = deducirTipo(elem);

                if (tipo.equals("Identificador")) {
                    String tipoReal = tabla.obtenerTipo(elem);
                    if (tipoReal.equals("Vacio")) {
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), elem, numeroLinea, "Variable indefinida"));
                        tipo = "Vacio";
                        hayErrores = true;
                    } else {
                        tipo = tipoReal;
                    }
                } else if (tipo.equals("Desconocido")) {
                    tipo = "Vacio";
                    hayErrores = true;
                }

                tabla.agregarElemento(elem, tipo);

                if (tipoResultado.equals("Desconocido")) {
                    tipoResultado = tipo; 
                } else if (!tipoResultado.equals(tipo) && !tipoResultado.equals("Vacio") && !tipo.equals("Vacio")) {
                    if ((tipoResultado.equals("Entero") || tipoResultado.equals("Real")) && tipo.equals("Cadena") ||
                        (tipo.equals("Entero") || tipo.equals("Real")) && tipoResultado.equals("Cadena")) {
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), expresion, numeroLinea, "Incompatibilidad de tipos en la operación."));
                        tipoResultado = "Vacio"; 
                        hayErrores = true;
                    }
                }
            }
        }

        return hayErrores ? "Vacio" : tipoResultado; 
    }

    private static String deducirTipo(String valor) {
        if (valor.matches("-?\\d+\\.\\d+")) { 
            return "Real";
        } else if (valor.matches("-?\\d+")) { 
            return "Entero";
        } else if (valor.matches("\".*\"")) { 
            return "Cadena";
        } else if (valor.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { 
            return "Identificador";
        } else if (valor.matches("[=+\\-*\\/<=!&()]")) { 
            return "Operador";
        }
        return "Desconocido"; 
    }
}
