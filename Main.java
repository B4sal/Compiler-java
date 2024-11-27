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
            JButton btnOptimizar = new JButton("Optimizar"); // Crear un botón para optimizar


            buttonPanel.add(btnCargar); // Agregar el botón de cargar al panel
            buttonPanel.add(btnCompilar); // Agregar el botón de compilar al panel
            buttonPanel.add(btnOptimizar); // Agregar el botón al panel
            

            frame.add(buttonPanel, BorderLayout.NORTH); // Agregar el panel de botones al norte del JFrame

            btnOptimizar.addActionListener((ActionEvent e) -> {
                String textoEntrada = textArea.getText();
                String[] lineas = textoEntrada.split("\n");
                List<String> codigo = new ArrayList<>();
            
                for (String linea : lineas) {
                    if (!linea.trim().isEmpty()) {
                        codigo.add(linea.trim());
                    }
                }
            
                Optimizador optimizador = new Optimizador();
                String codigoOptimizado = optimizador.optimizarCodigo(codigo);
            
                textArea.setText(codigoOptimizado);
                JOptionPane.showMessageDialog(frame, "Código optimizado con éxito.", "Optimización", JOptionPane.INFORMATION_MESSAGE);
            });
            
            

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
                    String nombre = partes[0].trim(); // Obtener el nombre (antes del igual)
                    String valor = partes[1].trim(); // Obtener el valor (después del igual)
                    String tipoValor = procesarExpresion(tabla, errores, valor, numeroLinea, nombre); // Pasar solo la parte antes del igual como 'nombre'
                    tabla.agregarElemento(nombre, tipoValor); // Agregar el elemento a la tabla de símbolos
                } else {
                    String nombre = partes[0].trim(); // Obtener el nombre (antes del igual)
                    tabla.agregarElemento(nombre, "Vacio"); // Agregar el elemento con tipo "Vacio"
                }
            } else {
                procesarExpresion(tabla, errores, linea, numeroLinea, linea); // Llamada con todos los argumentos necesarios
            }
        }

        private static String procesarExpresion(TablaSimbolos tabla, List<ErrorSemantico> errores, String expresion, int numeroLinea, String lineaCompleta) {
            // Aquí, lineaCompleta contendrá la parte antes del '=' en una asignación
            // (es decir, la variable o el identificador)
            String[] elementos = expresion.split("\\s+|(?=[=+\\-*\\/<=!&(){}])|(?<=[=+\\-*\\/<=!&(){}])");
            String tipoResultado = "Desconocido";
            String primerElementoIncompatible = null; // Para almacenar el primer elemento incompatible
            boolean hayErrores = false;
        
            for (String elem : elementos) {
                if (!elem.isEmpty()) {
                    // Include operators in the symbol table
                    if (elem.equals("+") || elem.equals("-") || elem.equals("*") || elem.equals("/") || 
                        elem.equals("<") || elem.equals(">") || elem.equals("=") || elem.equals("}") || 
                        elem.equals("(") || elem.equals(")")) {
                        tabla.agregarElemento(elem, "Operador"); // Add operator to the symbol table
                        continue; // Continue to the next element
                    }
        
                    String tipo = deducirTipo(elem); // Deduce the type of the element
        
                    // Si el tipo es "Desconocido", asignar a "Vacio" (por seguridad)
                    if (tipo.equals("Desconocido")) {
                        tipo = "Vacio"; // Asignamos un tipo por defecto si no se puede deducir
                        hayErrores = true;
                    }
        
                    // Si el elemento es un identificador, obtener su tipo real desde la tabla
                    if (tipo.equals("Identificador")) {
                        String tipoReal = tabla.obtenerTipo(elem);
                        if (tipoReal.equals("Vacio")) {
                            // Error: variable indefinida
                            errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), elem, numeroLinea, "Variable indefinida en la asignación: " + lineaCompleta));
                            hayErrores = true;
                            break; // Terminar procesamiento si la variable no está definida
                        } else {
                            tipo = tipoReal; // Asignar el tipo real de la variable
                        }
                    }
        
                    // Agregar el elemento a la tabla de símbolos
                    tabla.agregarElemento(elem, tipo);
        
                    // Comprobación de incompatibilidad de tipos
                    if (!tipoResultado.equals("Desconocido") && !tipoResultado.equals(tipo)) {
                        // Si uno de los tipos es Cadena y el otro es numérico, guardamos la cadena como el problema
                        if (tipo.equals("Cadena")) {
                            primerElementoIncompatible = elem; // Guardamos el primer elemento incompatible (cadena)
                        } else if (tipoResultado.equals("Cadena")) {
                            primerElementoIncompatible = primerElementoIncompatible == null ? elementos[0] : primerElementoIncompatible;
                        }
                        hayErrores = true;
                        break; // Terminar procesamiento si hay incompatibilidad de tipos
                    } else {
                        // Si no hay incompatibilidad, actualizar tipoResultado
                        tipoResultado = tipo;
                    }
                }
            }
        
            // Si hubo errores, agregamos el error de incompatibilidad de tipos con la línea completa de la asignación
            if (hayErrores && primerElementoIncompatible != null) {
                errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), primerElementoIncompatible, numeroLinea, 
                                               "Incompatibilidad de tipos en la operación: " + lineaCompleta));
                return "Vacio"; // Retornar "Vacio" si hubo un error
            }
        
            return tipoResultado; // Retornar el tipo de resultado si no hay errores
        }
        

        private static String deducirTipo(String elem) {
            if (elem.matches("^[0-9]+$")) {
                return "Entero"; // Número entero
            } else if (elem.matches("^[0-9]*\\.[0-9]+$")) {
                return "Real"; // Número real
            } else if (elem.startsWith("\"") && elem.endsWith("\"")) {
                return "Cadena"; // Cadena de texto
            } else if (elem.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                return "Identificador"; // Identificadores válidos (ej: variables)
            } else {
                return "Desconocido"; // Caso desconocido
            }
        }
        
        
    }