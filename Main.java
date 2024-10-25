import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        TablaSimbolos tabla = new TablaSimbolos();

        // Ruta del archivo a cargar (cambia esto según tu archivo)
        String archivoEntrada = "datos.txt"; // Asegúrate de que este archivo esté en el directorio correcto

        // Cargar datos desde el archivo
        cargarDatosDesdeArchivo(tabla, archivoEntrada);

        // Mostrar la tabla de símbolos
        tabla.mostrarTabla();
    }

    private static void cargarDatosDesdeArchivo(TablaSimbolos tabla, String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                procesarLinea(tabla, linea.trim());
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }

    private static void procesarLinea(TablaSimbolos tabla, String linea) {
        // Verificar si la línea tiene más de un '=' para detectar errores de mal formulación
        if (linea.split("=").length > 2) {
            System.err.println("Error: La línea '" + linea + "' no está bien formada.");
            String nombre = linea.split("=")[0].trim();
            tabla.agregarElemento(nombre, "Vacio");
            return;
        }
    
        String[] partes = linea.split("=");
        if (partes.length == 2) {
            String nombre = partes[0].trim();
            String valor = partes[1].trim();
    
            // Procesar y deducir el tipo de la expresión completa
            String tipoValor = procesarExpresion(tabla, valor);
            if (tipoValor.equals("Vacio") || tipoValor.equals("Desconocido")) {
                tabla.agregarElemento(nombre, "Vacio"); // Error en la expresión
            } else {
                tabla.agregarElemento(nombre, tipoValor); // Agregar el tipo deducido
            }
        } else {
            System.err.println("Error: La línea '" + linea + "' no está bien formada.");
            String nombre = partes[0].trim();
            tabla.agregarElemento(nombre, "Vacio"); // Marcar como vacío si no está bien formada
        }
    }
    

    private static String procesarExpresion(TablaSimbolos tabla, String expresion) {
        String[] elementos = expresion.split("\\s+|(?=[=+\\-*\\/])|(?<=[=+\\-*\\/])");
        String tipoResultado = "Entero"; // Asumimos que empieza como Entero
        boolean hayErrores = false; // Variable para verificar errores
        boolean operandoInvalido = false; // Controlar errores por tipos incompatibles
    
        for (String elem : elementos) {
            if (!elem.isEmpty()) {
                String tipo = deducirTipo(elem);
    
                if (tipo.equals("Identificador")) {
                    String tipoReal = tabla.obtenerTipo(elem);
                    if (tipoReal.equals("Vacio")) {
                        System.err.println("Error: El identificador '" + elem + "' no está definido.");
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
    
                // Si la expresión mezcla tipos incorrectos, marcar error
                if ((tipoResultado.equals("Entero") || tipoResultado.equals("Real")) && tipo.equals("Cadena") || 
                    (tipo.equals("Entero") || tipo.equals("Real")) && tipoResultado.equals("Cadena")) {
                    System.err.println("Error: La operación contiene una cadena en un contexto matemático.");
                    operandoInvalido = true;
                }
    
                // Agregar el elemento a la tabla de símbolos
                tabla.agregarElemento(elem, tipo);
    
                // Actualizar el tipo de la expresión en base a los elementos
                tipoResultado = actualizarTipoResultado(tipoResultado, tipo);
            }
        }
    
        // Si la expresión contiene una cadena dentro de una operación matemática
        if (operandoInvalido) {
            return "Vacio";
        }
    
        return hayErrores ? "Vacio" : tipoResultado; // Devuelve el tipo final de la expresión
    }
    
    

    private static String actualizarTipoResultado(String tipoActual, String nuevoTipo) {
        // Si cualquiera de los tipos es Real, el resultado es Real
        if (tipoActual.equals("Real") || nuevoTipo.equals("Real")) {
            return "Real";
        }
        // Si el tipo es "Vacio", la expresión es inválida
        if (nuevoTipo.equals("Vacio")) {
            return "Vacio";
        }
        // Si ambos son Enteros, el resultado sigue siendo Entero
        return "Entero";
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
