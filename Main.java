import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        TablaSimbolos tabla = new TablaSimbolos();
        TablaErrores tablaErrores = new TablaErrores();

        // Ruta del archivo a cargar (cambia esto según tu archivo)
        String archivoEntrada = "datos.txt"; // Asegúrate de que este archivo esté en el directorio correcto

        // Cargar datos desde el archivo
        cargarDatosDesdeArchivo(tabla, tablaErrores, archivoEntrada);

        // Mostrar la tabla de símbolos
        tabla.mostrarTabla();

        // Mostrar la tabla de errores
        tablaErrores.mostrarErrores();
    }

    private static void cargarDatosDesdeArchivo(TablaSimbolos tabla, TablaErrores tablaErrores, String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int numeroLinea = 1; // Contador para el número de línea
            while ((linea = br.readLine()) != null) {
                procesarLinea(tabla, tablaErrores, linea.trim(), numeroLinea);
                numeroLinea++;
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private static void procesarLinea(TablaSimbolos tabla, TablaErrores tablaErrores, String linea, int numeroLinea) {
        String[] partes = linea.split("=");
        if (partes.length == 2) {
            String nombre = partes[0].trim();
            String valor = partes[1].trim();

            // Procesar y deducir el tipo de la expresión completa
            String tipoValor = procesarExpresion(tabla, tablaErrores, valor, numeroLinea);
            tabla.agregarElemento(nombre, tipoValor);
        } else {
            System.err.println("Error: La línea '" + linea + "' no está bien formada.");
            String nombre = partes[0].trim();
            tabla.agregarElemento(nombre, "Vacio"); // Marcar como vacío si no está bien formada
        }
    }

    private static String procesarExpresion(TablaSimbolos tabla, TablaErrores tablaErrores, String expresion, int numeroLinea) {
        String[] elementos = expresion.split("\\s+|(?=[=+\\-*\\/])|(?<=[=+\\-*\\/])");
        String tipoResultado = "Desconocido"; // Asumimos que empieza como Desconocido
        boolean hayErrores = false;

        for (String elem : elementos) {
            if (!elem.isEmpty()) {
                String tipo = deducirTipo(elem);

                if (tipo.equals("Identificador")) {
                    String tipoReal = tabla.obtenerTipo(elem);
                    if (tipoReal.equals("Vacio")) {
                        System.err.println("Error: El identificador '" + elem + "' no está definido.");
                        tablaErrores.agregarError(elem, numeroLinea, "Variable indefinida");
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
                    tipoResultado = tipo; // Solo cambia si tipoResultado es desconocido
                } else if (!tipoResultado.equals(tipo) && !tipoResultado.equals("Vacio") && !tipo.equals("Vacio")) {
                    // Comprobamos la incompatibilidad de tipos
                    if ((tipoResultado.equals("Entero") || tipoResultado.equals("Real")) && tipo.equals("Cadena") ||
                        (tipo.equals("Entero") || tipo.equals("Real")) && tipoResultado.equals("Cadena")) {
                        System.err.println("Error: No se puede realizar operaciones entre Entero/Real y Cadena.");
                        tablaErrores.agregarError(elem, numeroLinea, "Incompatibilidad de tipos");
                        tipoResultado = "Vacio"; // Indicar que hay un error
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
        } else if (valor.matches("[=+\\-*\\/]")) { // Operadores
            return "Operador";
        }
        return "Desconocido"; // Esto se usa para determinar que es un error
    }
}
