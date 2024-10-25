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
        String[] partes = linea.split("=");
        if (partes.length == 2) {
            String nombre = partes[0].trim();
            String valor = partes[1].trim();
            
            // Deducción del tipo del nombre de la variable
            String tipoNombre = deducirTipo(nombre);
            tabla.agregarElemento(nombre, tipoNombre);

            // Deducción del tipo del valor y procesamiento de los elementos
            procesarValor(tabla, valor);
        } else {
            System.err.println("Error: La línea '" + linea + "' no está bien formada.");
        }
    }

    private static void procesarValor(TablaSimbolos tabla, String valor) {
        String[] elementos = valor.split("\\s+|(?=[=+\\-*\\/])|(?<=[=+\\-*\\/])");
        for (String elem : elementos) {
            if (!elem.isEmpty()) {
                String tipo = deducirTipo(elem);
                if (tipo.equals("Desconocido")) {
                    // Aquí consideramos que es un error
                    System.err.println("Error: La entrada '" + elem + "' es inválida o mal formada.");
                    tabla.agregarElemento(elem, "Vacio"); // Dejar vacío en caso de error
                } else {
                    tabla.agregarElemento(elem, tipo);
                }
            }
        }
    }

    private static String deducirTipo(String valor) {
        if (valor.matches("-?\\d+")) { // Número entero
            return "Entero";
        } else if (valor.matches("\".*\"")) { // Cadena entre comillas
            return "Cadena";
        } else if (valor.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // Identificador simple
            return "Identificador";
        } else if (valor.matches("[=+\\-*\\/]")) { // Operadores
            return "Operador";
        }
        // Consideramos cualquier otra entrada como error
        return "Desconocido"; // Esto se usa para determinar que es un error
    }
}
