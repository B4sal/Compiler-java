import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TablaSimbolos tabla = new TablaSimbolos();
        List<ErrorSemantico> errores = new ArrayList<>();

        // Ruta del archivo a cargar (cambia esto según tu archivo)
        String archivoEntrada = "datos.txt"; // Asegúrate de que este archivo esté en el directorio correcto

        // Cargar datos desde el archivo
        cargarDatosDesdeArchivo(tabla, errores, archivoEntrada);

        // Mostrar la tabla de símbolos
        tabla.mostrarTabla();

        // Mostrar la tabla de errores
        mostrarTablaErrores(errores);
    }

    private static void cargarDatosDesdeArchivo(TablaSimbolos tabla, List<ErrorSemantico> errores, String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 1;
            while ((linea = br.readLine()) != null) {
                procesarLinea(tabla, errores, linea.trim(), numeroLinea);
                numeroLinea++;
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private static void procesarLinea(TablaSimbolos tabla, List<ErrorSemantico> errores, String linea, int numeroLinea) {
        // Separamos la línea en el operador '=' si está presente
        if (linea.contains("=")) {
            String[] partes = linea.split("=");
            if (partes.length == 2) {
                String nombre = partes[0].trim();
                String valor = partes[1].trim();

                // Procesar y deducir el tipo de la expresión completa
                String tipoValor = procesarExpresion(tabla, errores, valor, numeroLinea);
                tabla.agregarElemento(nombre, tipoValor); // Agrega el nombre y tipo a la tabla
            } else {
                System.err.println("Error: La línea '" + linea + "' no está bien formada.");
                String nombre = partes[0].trim();
                tabla.agregarElemento(nombre, "Vacio"); // Marcar como vacío si no está bien formada
            }
        } else {
            // Procesar otras líneas como estructuras de control o expresiones sin asignación
            procesarExpresion(tabla, errores, linea, numeroLinea);
        }
    }

    private static String procesarExpresion(TablaSimbolos tabla, List<ErrorSemantico> errores, String expresion, int numeroLinea) {
        // Este patrón se usa para dividir la expresión, asegurándose de manejar operadores y paréntesis
        String[] elementos = expresion.split("\\s+|(?=[=+\\-*\\/<=!&()])|(?<=[=+\\-*\\/<=!&()])");
        String tipoResultado = "Desconocido"; 
        boolean hayErrores = false;

        for (String elem : elementos) {
            if (!elem.isEmpty()) {
                String tipo = deducirTipo(elem);

                if (tipo.equals("Identificador")) {
                    String tipoReal = tabla.obtenerTipo(elem);
                    if (tipoReal.equals("Vacio")) {
                        // Agregar a la lista de errores
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), elem, numeroLinea, "Variable indefinida"));
                        tipo = "Vacio";
                        hayErrores = true;
                    } else {
                        tipo = tipoReal;
                    }
                } else if (tipo.equals("Desconocido")) {
                    System.err.println("Error: La entrada '" + elem + "' es inválida o mal formada.");
                    tipo = "Vacio";
                    hayErrores = true;
                }

                // Agregar el elemento a la tabla de símbolos
                tabla.agregarElemento(elem, tipo);

                // Actualizar el tipo de la expresión en base a los elementos
                if (tipoResultado.equals("Desconocido")) {
                    tipoResultado = tipo; 
                } else if (!tipoResultado.equals(tipo) && !tipoResultado.equals("Vacio") && !tipo.equals("Vacio")) {
                    // Comprobamos la incompatibilidad de tipos
                    if ((tipoResultado.equals("Entero") || tipoResultado.equals("Real")) && tipo.equals("Cadena") ||
                        (tipo.equals("Entero") || tipo.equals("Real")) && tipoResultado.equals("Cadena")) {
                        // Agregar a la lista de errores
                        errores.add(new ErrorSemantico("ErrSem" + (errores.size() + 1), expresion, numeroLinea, "Incompatibilidad de tipos en la operación."));
                        tipoResultado = "Vacio"; 
                        hayErrores = true;
                    }
                }
            }
        }

        return hayErrores ? "Vacio" : tipoResultado; // Devuelve el tipo final de la expresión
    }

    private static String deducirTipo(String valor) {
        if (valor.matches("-?\\d+\\.\\d+")) { // Número real
            return "Real";
        } else if (valor.matches("-?\\d+")) { // Número entero
            return "Entero";
        } else if (valor.matches("\".*\"")) { // Cadena entre comillas
            return "Cadena";
        } else if (valor.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // Identificador simple
            return "Identificador";
        } else if (valor.matches("[=+\\-*\\/<=!&()]")) { // Operadores y paréntesis
            return "Operador";
        }
        return "Desconocido"; 
    }

    private static void mostrarTablaErrores(List<ErrorSemantico> errores) {
        System.out.println("\nTabla de Errores:");
        System.out.printf("%-10s %-15s %-10s %-40s%n", "Token", "Lexema", "Renglón", "Descripción");
        System.out.println("-------------------------------------------------------------");
        for (ErrorSemantico error : errores) {
            System.out.printf("%-10s %-15s %-10d %-40s%n", error.getToken(), error.getLexema(), error.getRenglon(), error.getDescripcion());
        }
    }
}
