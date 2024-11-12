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
    private static TablaSimbolos tabla; // Tabla de símbolos
    private static List<ErrorSemantico> errores; // Lista de errores semánticos
    private static List<String[]> tablaTriplos; // Tabla de triplos
    private static JTextArea textArea; // JTextArea para mostrar y editar el contenido del archivo

    public static void main(String[] args) {
        tabla = new TablaSimbolos(); // Inicializar la tabla de símbolos
        errores = new ArrayList<>(); // Inicializar la lista de errores
        tablaTriplos = new ArrayList<>(); // Inicializar la tabla de triplos

        // Mostrar las tablas en un JFrame
        SwingUtilities.invokeLater(Main::mostrarInterfaz); // Ejecutar mostrarInterfaz en el hilo de despacho de eventos
    }

    private static void mostrarInterfaz() {
        textArea = new JTextArea(10, 50); // Crear un JTextArea con 10 filas y 50 columnas
        textArea.setEditable(true); // Permitir edición en el JTextArea
        JScrollPane scrollPane = new JScrollPane(textArea); // Agregar el JTextArea a un JScrollPane

        JFrame frame = new JFrame("Analizador Semántico"); // Crear un JFrame con el título "Analizador Semántico"
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configurar la operación de cierre del JFrame
        frame.setSize(800, 600); // Establecer el tamaño del JFrame
        frame.setLayout(new BorderLayout()); // Establecer el diseño del JFrame

        // Tablas de símbolos, errores y triplos en pestañas
        JTabbedPane tabbedPane = new JTabbedPane(); // Crear un JTabbedPane
        tabbedPane.addTab("Tabla de Símbolos", crearTablaSimbolos()); // Agregar la tabla de símbolos como una pestaña
        tabbedPane.addTab("Tabla de Errores", crearTablaErrores()); // Agregar la tabla de errores como una pestaña
        tabbedPane.addTab("Tabla de Triplos", crearTablaTriplos()); // Agregar la tabla de triplos como una pestaña

        frame.add(tabbedPane, BorderLayout.CENTER); // Agregar el JTabbedPane al centro del JFrame
        frame.add(scrollPane, BorderLayout.WEST); // Agregar el JScrollPane con el JTextArea a la izquierda del JFrame

        // Panel de botones para seleccionar archivo y compilar
        JPanel buttonPanel = new JPanel(); // Crear un JPanel para los botones
        JButton btnCargar = new JButton("Cargar"); // Crear un botón para cargar archivo
        JButton btnCompilar = new JButton("Compilar"); // Crear un botón para compilar

        buttonPanel.add(btnCargar); // Agregar el botón de cargar al panel
        buttonPanel.add(btnCompilar); // Agregar el botón de compilar al panel
        frame.add(buttonPanel, BorderLayout.NORTH); // Agregar el panel de botones al norte del JFrame

        // Acción para cargar un archivo
        btnCargar.addActionListener((ActionEvent e) -> {
            JFileChooser fileChooser = new JFileChooser(); // Crear un selector de archivos
            int returnValue = fileChooser.showOpenDialog(frame); // Mostrar el selector de archivos
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile(); // Obtener el archivo seleccionado
                cargarDatosDesdeArchivo(selectedFile.getAbsolutePath()); // Cargar datos desde el archivo
            }
        });

        // Acción para compilar el texto del JTextArea
        btnCompilar.addActionListener((ActionEvent e) -> {
            // Limpia las tablas antes de compilar
            errores.clear(); // Limpiar la lista de errores
            tablaTriplos.clear(); // Limpiar la tabla de triplos
            tabla = new TablaSimbolos(); // Reiniciar tabla de símbolos

            // Obtener el texto directamente del JTextArea
            String textoEntrada = textArea.getText(); // Obtener el texto del JTextArea

            // Procesar el texto ingresado directamente en el JTextArea
            String[] lineas = textoEntrada.split("\n"); // Dividir el texto en líneas
            for (int numeroLinea = 0; numeroLinea < lineas.length; numeroLinea++) {
                procesarLinea(tabla, errores, lineas[numeroLinea].trim(), numeroLinea + 1); // Procesar cada línea
            }

            GeneradorTriploDinamico.procesarTexto(textoEntrada, tablaTriplos); // Generar triplos

            // Actualiza las tablas en la interfaz
            actualizarTablas(tabbedPane); // Actualizar las tablas en la interfaz
        });

        frame.setVisible(true); // Hacer visible el JFrame
    }

    private static void cargarDatosDesdeArchivo(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            StringBuilder contenido = new StringBuilder(); // Crear un StringBuilder para el contenido
            String linea;
            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n"); // Cargar contenido al StringBuilder
            }
            textArea.setText(contenido.toString()); // Mostrar contenido en el JTextArea
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al leer el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Mostrar mensaje de error
        }
    }

    private static void actualizarTablas(JTabbedPane tabbedPane) {
        // Actualiza la tabla de símbolos
        tabbedPane.setComponentAt(0, crearTablaSimbolos()); // Actualizar la tabla de símbolos
        // Actualiza la tabla de errores
        tabbedPane.setComponentAt(1, crearTablaErrores()); // Actualizar la tabla de errores
        // Actualiza la tabla de triplos
        tabbedPane.setComponentAt(2, crearTablaTriplos()); // Actualizar la tabla de triplos
    }

    private static JScrollPane crearTablaSimbolos() {
        String[] columnNames = {"Nombre", "Tipo"}; // Nombres de las columnas
        DefaultTableModel model = new DefaultTableModel(columnNames, 0); // Crear el modelo de la tabla

        // Agregar los elementos de la tabla de símbolos al modelo
        for (var entry : tabla.getElementos()) {
            model.addRow(new Object[]{entry.getNombre(), entry.getTipo()}); // Agregar fila al modelo
        }

        JTable table = new JTable(model); // Crear la tabla con el modelo
        return new JScrollPane(table); // Devolver la tabla dentro de un JScrollPane
    }

    private static JScrollPane crearTablaErrores() {
        String[] columnNames = {"Token", "Lexema", "Renglón", "Descripción"}; // Nombres de las columnas
        DefaultTableModel model = new DefaultTableModel(columnNames, 0); // Crear el modelo de la tabla

        // Agregar los errores al modelo de la tabla
        for (ErrorSemantico error : errores) {
            model.addRow(new Object[]{error.getToken(), error.getLexema(), error.getRenglon(), error.getDescripcion()}); // Agregar fila al modelo
        }

        JTable table = new JTable(model); // Crear la tabla con el modelo
        return new JScrollPane(table); // Devolver la tabla dentro de un JScrollPane
    }

    private static JScrollPane crearTablaTriplos() {
        String[] columnNames = {"ID", "Dato Objeto", "Dato Fuente", "Operador"}; // Nombres de las columnas
        DefaultTableModel model = new DefaultTableModel(columnNames, 0); // Crear el modelo de la tabla

        // Contador para el ID, que se autoincrementará
        int id = 1;

        // Agregar los triplos al modelo de la tabla
        for (String[] triplo : tablaTriplos) {
            // Cada fila debe incluir el ID autoincrementado y los datos de la tabla de triplos
            model.addRow(new Object[]{id++, triplo[0], triplo[1], triplo[2]}); // Agregar fila al modelo
        }

        JTable table = new JTable(model); // Crear la tabla con el modelo
        return new JScrollPane(table); // Devolver la tabla dentro de un JScrollPane
    }

    private static void procesarLinea(TablaSimbolos tabla, List<ErrorSemantico> errores, String linea, int numeroLinea) {
        if (linea.contains("=")) { // Si la línea contiene un signo de igual
            String[] partes = linea.split("="); // Dividir la línea en partes
            if (partes.length == 2) { // Si hay dos partes
                String nombre = partes[0].trim(); // Obtener el nombre
                String valor = partes[1].trim(); // Obtener el valor
                String tipoValor = procesarExpresion(tabla, errores, valor, numeroLinea); // Procesar la expresión
                tabla.agregarElemento(nombre, tipoValor); // Agregar el elemento a la tabla de símbolos
            } else {
                String nombre = partes[0].trim(); // Obtener el nombre
                tabla.agregarElemento(nombre, "Vacio"); // Agregar el elemento con tipo "Vacio"
            }
        } else {
            procesarExpresion(tabla, errores, linea, numeroLinea); // Procesar la expresión
        }
    }

    private static String procesarExpresion(TablaSimbolos tabla, List<ErrorSemantico> errores, String expresion, int numeroLinea) {
        String[] elementos = expresion.split("\\s+|(?=[=+\\-*\\/<=!&()])|(?<=[=+\\-*\\/<=!&()])"); // Dividir la expresión en elementos
        String tipoResultado = "Desconocido"; // Tipo de resultado inicial
        boolean hayErrores = false; // Bandera para errores

        for (String elem : elementos) {
            if (!elem.isEmpty()) { // Si el elemento no está vacío
                String tipo = deducirTipo(elem); // Deducir el tipo del elemento

                if (tipo.equals("Identificador")) { // Si el tipo es identificador
                    String tipoReal = tabla.obtenerTipo(elem); // Obtener el tipo real del identificador
                    if (tipoReal.equals("Vacio")) { // Si el tipo real es "Vacio"
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), elem, numeroLinea, "Variable indefinida")); // Agregar error semántico
                        tipo = "Vacio"; // Establecer el tipo como "Vacio"
                        hayErrores = true; // Establecer la bandera de errores
                    } else {
                        tipo = tipoReal; // Establecer el tipo como el tipo real
                    }
                } else if (tipo.equals("Desconocido")) { // Si el tipo es desconocido
                    tipo = "Vacio"; // Establecer el tipo como "Vacio"
                    hayErrores = true; // Establecer la bandera de errores
                }

                tabla.agregarElemento(elem, tipo); // Agregar el elemento a la tabla de símbolos

                if (tipoResultado.equals("Desconocido")) { // Si el tipo de resultado es desconocido
                    tipoResultado = tipo; // Establecer el tipo de resultado
                } else if (!tipoResultado.equals(tipo) && !tipoResultado.equals("Vacio") && !tipo.equals("Vacio")) { // Si hay incompatibilidad de tipos
                    if ((tipoResultado.equals("Entero") || tipoResultado.equals("Real")) && tipo.equals("Cadena") ||
                        (tipo.equals("Entero") || tipo.equals("Real")) && tipoResultado.equals("Cadena")) {
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), expresion, numeroLinea, "Incompatibilidad de tipos en la operación.")); // Agregar error semántico
                        tipoResultado = "Vacio"; // Establecer el tipo de resultado como "Vacio"
                        hayErrores = true; // Establecer la bandera de errores
                    } else if (tipoResultado.equals("Entero") && tipo.equals("Real")) {
                        tipoResultado = "Real"; // Promover a Real si hay mezcla de Entero y Real
                    } else if (tipoResultado.equals("Real") && tipo.equals("Entero")) {
                        // Mantener tipoResultado como Real
                    } else {
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), expresion, numeroLinea, "Incompatibilidad de tipos en la operación.")); // Agregar error semántico
                        tipoResultado = "Vacio"; // Establecer el tipo de resultado como "Vacio"
                        hayErrores = true; // Establecer la bandera de errores
                    }
                }
            }
        }

        return hayErrores ? "Vacio" : tipoResultado; // Devolver el tipo de resultado
    }

    private static String deducirTipo(String valor) {
        if (valor.matches("-?\\d+\\.\\d+f?")) { // Si el valor es un número real o flotante
            return "Real";
        } else if (valor.matches("-?\\d+")) { // Si el valor es un número entero
            return "Entero";
        } else if (valor.matches("\".*\"")) { // Si el valor es una cadena
            return "Cadena";
        } else if (valor.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // Si el valor es un identificador
            return "Identificador";
        } else if (valor.matches("[=+\\-*\\/<=!&()]")) { // Si el valor es un operador
            return "Operador";
        }
        return "Desconocido"; // Si el valor no coincide con ningún patrón
    }
}