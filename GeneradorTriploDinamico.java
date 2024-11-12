import java.io.*;
import java.util.*;
import java.util.regex.*;

class GeneradorTriploDinamico {
    static int temporalID = 1; // ID para los temporales
    static List<String> temporalesLibres = new ArrayList<>(); // Lista de temporales libres

    public static void procesarArchivo(String archivoEntrada, List<String[]> tabla) {
        try {
            File file = new File(archivoEntrada); // Crear un objeto File con el archivo de entrada
            BufferedReader br = new BufferedReader(new FileReader(file)); // Crear un BufferedReader para leer el archivo
            String line;

            // Procesar las líneas del archivo
            while ((line = br.readLine()) != null) {
                procesarLinea(line, br, tabla); // Procesar cada línea del archivo
            }

            // Añadir el cierre con "…"
            tabla.add(new String[]{"...", "...", "..."}); // Añadir una línea de cierre a la tabla
        } catch (IOException e) {
            e.printStackTrace(); // Imprimir la traza de la excepción en caso de error
        }
    }

    public static void procesarTexto(String textoEntrada, List<String[]> tabla) {
        try (BufferedReader br = new BufferedReader(new StringReader(textoEntrada))) { // Crear un BufferedReader para leer el texto de entrada
            String line;
            while ((line = br.readLine()) != null) {
                procesarLinea(line, br, tabla); // Procesar cada línea del texto de entrada
            }
            tabla.add(new String[]{"...", "...", "..."}); // Añadir una línea de cierre a la tabla
        } catch (IOException e) {
            e.printStackTrace(); // Imprimir la traza de la excepción en caso de error
        }
    }

    private static void procesarLinea(String line, BufferedReader br, List<String[]> tabla) throws IOException {
        line = line.trim(); // Eliminar espacios en blanco al inicio y al final de la línea

        if (line.startsWith("while")) {
            procesarWhile(line, br, tabla); // Procesar una línea que comienza con "while"
        } else if (line.contains("=")) {
            procesarAsignacion(line, tabla); // Procesar una línea que contiene una asignación
        }
    }

    private static void procesarAsignacion(String line, List<String[]> tabla) {
        String[] partes = line.split("="); // Dividir la línea en partes usando el signo "="
        String variable = partes[0].trim(); // Obtener la variable de la asignación
        String expresion = partes[1].trim(); // Obtener la expresión de la asignación

        String temporal = evaluarExpresion(expresion, tabla); // Evaluar la expresión y obtener un temporal
        liberarTemporal(temporal); // Liberar el temporal después de usarlo
        tabla.add(new String[]{variable, temporal, "="}); // Añadir la asignación a la tabla
    }

    private static String evaluarExpresion(String expresion, List<String[]> tabla) {
        Matcher matcher = Pattern.compile("[\\w.]+|[+\\-*/()]").matcher(expresion); // Crear un Matcher para encontrar operandos y operadores
        List<String> operandos = new ArrayList<>(); // Lista para almacenar los operandos
        List<String> operadores = new ArrayList<>(); // Lista para almacenar los operadores

        while (matcher.find()) {
            String token = matcher.group();
            if (token.matches("[+\\-*/()]")) {
                operadores.add(token); // Añadir el operador a la lista de operadores
            } else {
                operandos.add(token); // Añadir el operando a la lista de operandos
            }
        }

        if (operandos.size() == 1) {
            String temporal = obtenerTemporalLibre();
            tabla.add(new String[]{temporal, operandos.get(0), "="}); // Añadir la asignación a la tabla
            return temporal; // Devolver el temporal actual
        }

        String temporalActual = obtenerTemporalLibre(); // Obtener el primer temporal libre
        tabla.add(new String[]{temporalActual, operandos.get(0), "="}); // Añadir la asignación inicial a la tabla

        for (int i = 1; i < operandos.size(); i++) {
            String operando1 = temporalActual; // Usar el temporal actual como primer operando
            String operando2 = operandos.get(i); // Obtener el segundo operando
            String operador = operadores.get(i - 1); // Obtener el operador

            tabla.add(new String[]{temporalActual, operando1, operador}); // Añadir la operación a la tabla
            tabla.add(new String[]{temporalActual, operando2, "="}); // Añadir la asignación a la tabla

            temporalActual = temporalActual.equals("T1") ? "T2" : "T1"; // Alternar entre T1 y T2
        }

        liberarTemporal(temporalActual); // Liberar el temporal después de usarlo
        return temporalActual; // Devolver el temporal actual
    }

    private static void procesarWhile(String line, BufferedReader br, List<String[]> tabla) throws IOException {
        String condiciones = line.substring(line.indexOf("(") + 1, line.indexOf(")")); // Obtener las condiciones del while
        String[] condicionesArray = condiciones.split("&&|\\|\\|"); // Dividir las condiciones usando "&&" o "||"
        boolean esAND = condiciones.contains("&&"); // Verificar si las condiciones están unidas por "&&"

        int indiceInicioCiclo = tabla.size() + 1; // Índice de inicio del ciclo
        List<int[]> saltosPendientes = new ArrayList<>(); // Lista para almacenar los saltos pendientes

        // Manejar las condiciones
        for (int i = 0; i < condicionesArray.length; i++) {
            String condicion = condicionesArray[i].trim();
            String[] partes = condicion.split(" ");
            if (partes.length < 3) {
                continue; // Saltar si la condición no tiene el formato esperado
            }
            String izq = partes[0].trim();
            String op = partes[1].trim();
            String der = partes[2].trim();

            String tempIzq = obtenerTemporalLibre(); // Obtener un temporal libre para el operando izquierdo
            tabla.add(new String[]{tempIzq, izq, "="}); // Añadir la asignación a la tabla

            String tempDer = obtenerTemporalLibre(); // Obtener un temporal libre para el operando derecho
            tabla.add(new String[]{tempDer, der, "="}); // Añadir la asignación a la tabla

            String tempComp = obtenerTemporalLibre(); // Obtener un temporal libre para la comparación
            tabla.add(new String[]{tempComp, tempIzq, op}); // Añadir la comparación a la tabla
            tabla.add(new String[]{tempComp, tempDer, "="}); // Añadir la comparación a la tabla

            if (esAND) {
                int saltoFalso = 0; // Salto fuera del ciclo
                int siguienteCond = tabla.size() + 3; // Salto para la siguiente condición

                // Si es `AND`, fallo en una condición significa salida del ciclo.
                saltosPendientes.add(new int[]{tabla.size(), siguienteCond, 1});
                tabla.add(new String[]{"TR1", "TRUE", Integer.toString(siguienteCond)});

                saltosPendientes.add(new int[]{tabla.size(), saltoFalso, 2});
                tabla.add(new String[]{"TR2", "FALSE", Integer.toString(saltoFalso)});

            } else { // OR
                // Si es TRUE, saltar directamente al cuerpo del ciclo
                int saltoTrue = indiceInicioCiclo + 10; // Salta al contenido del while
                saltosPendientes.add(new int[]{tabla.size(), saltoTrue, 1});
                tabla.add(new String[]{"TR1", "TRUE", Integer.toString(saltoTrue)});

                // Si es FALSE, saltar a la primera línea de la siguiente condición (si existe)
                int saltoFalse = (i < condicionesArray.length - 1) ? tabla.size() + 2 : 0;
                saltosPendientes.add(new int[]{tabla.size(), saltoFalse, 2});
                tabla.add(new String[]{"TR2", "FALSE", Integer.toString(saltoFalse)});
            }

            liberarTemporal(tempIzq); // Liberar los temporales después de usarlos
            liberarTemporal(tempDer);
            liberarTemporal(tempComp);
        }

        // Procesar el contenido dentro del ciclo while
        String innerLine;
        while ((innerLine = br.readLine()) != null) {
            innerLine = innerLine.trim();
            if (innerLine.equals("}")) {
                break; // Salir del ciclo cuando se encuentra el cierre del while
            } else if (innerLine.contains("=")) {
                procesarAsignacion(innerLine, tabla); // Procesar una asignación dentro del while
            }
        }

        int indiceFinCiclo = tabla.size() + 2; // Índice de fin del ciclo

        // Actualizar saltos
        for (int[] salto : saltosPendientes) {
            int saltoPos = salto[0];
            tabla.get(saltoPos)[2] = (salto[1] == 0) ? Integer.toString(indiceFinCiclo) : Integer.toString(salto[1]);
        }

        // Añadir salto incondicional al inicio del ciclo
        tabla.add(new String[]{"vacio", Integer.toString(indiceInicioCiclo), "JR"});
    }

    private static String obtenerTemporalLibre() {
        if (!temporalesLibres.isEmpty()) {
            return temporalesLibres.remove(0); // Reutilizar un temporal libre si está disponible
        }
        return "T" + (temporalID++); // Devolver un nuevo temporal si no hay libres
    }

    private static void liberarTemporal(String temporal) {
        if (!temporalesLibres.contains(temporal)) {
            temporalesLibres.add(temporal); // Añadir el temporal a la lista de temporales libres si no está ya presente
        }
    }

    public static void imprimirTabla(List<String[]> tabla) {
        System.out.println("ID\tDato Objeto\tDato Fuente\tOperador"); // Imprimir encabezados de la tabla
        int id = 1;
        for (String[] fila : tabla) {
            System.out.printf("%d\t%s\t\t%s\t\t%s%n", id++, fila[0], fila[1], fila[2]); // Imprimir cada fila de la tabla
        }
    }
}