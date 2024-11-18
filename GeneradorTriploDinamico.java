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
        Matcher matcher = Pattern.compile("\\w+\\.?\\w*|[+\\-*/]").matcher(expresion); // Crear un Matcher para encontrar operandos y operadores
        List<String> operandos = new ArrayList<>(); // Lista para almacenar los operandos
        List<String> operadores = new ArrayList<>(); // Lista para almacenar los operadores
    
        // Extraer operadores y operandos de la expresión
        while (matcher.find()) {
            String token = matcher.group();
            if (token.matches("[+\\-*/]")) {
                operadores.add(token);
            } else {
                operandos.add(token);
            }
        }
    
        // Si solo hay un operando, asignarlo directamente a un temporal
        if (operandos.size() == 1) {
            String temporal = obtenerTemporalLibre();
            tabla.add(new String[]{temporal, operandos.get(0), "="});
            return temporal; // Devolvemos el temporal sin liberarlo
        }
    
        // Procesar operandos y operadores en orden
        String temporalActual = obtenerTemporalLibre(); // Primer temporal para el primer operando
        tabla.add(new String[]{temporalActual, operandos.get(0), "="}); // Asignamos el primer operando al primer temporal
    
        for (int i = 1; i < operandos.size(); i++) {
            String operando1 = temporalActual; // El temporal actual con el resultado parcial
            String operando2 = operandos.get(i); // El siguiente operando
    
            // Verifica que haya un operador disponible en este índice
            if (i - 1 < operadores.size()) {
                String operador = operadores.get(i - 1); // El operador correspondiente
    
                // Almacenar el resultado de la operación en el mismo temporalActual en la última operación
                if (i < operandos.size() - 1) {
                    String nuevoTemporal = obtenerTemporalLibre();
                    tabla.add(new String[]{nuevoTemporal, operando1, operador});
                    tabla.add(new String[]{nuevoTemporal, operando2, "="});
                    liberarTemporal(temporalActual);
                    temporalActual = nuevoTemporal;
                } else {
                    tabla.add(new String[]{temporalActual, operando1, operador});
                    tabla.add(new String[]{temporalActual, operando2, "="});
                }
            } else {
                System.err.println("Error: operador faltante para la expresión en la posición " + i);
                break; // Rompe el bucle si falta un operador
            }
        }
    
        return temporalActual; // Devuelve el temporal final que contiene el resultado completo
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
    
            // Si el operando izquierdo no es un temporal ni un valor inmediato, asignarlo a un nuevo temporal
            String tempIzq = (izq.matches("T\\d+") || izq.matches("\\d+")) ? izq : obtenerTemporalLibre();
            if (!izq.equals(tempIzq)) {
                tabla.add(new String[]{tempIzq, izq, "="});
            }
    
            // Si el operando derecho no es un temporal ni un valor inmediato, asignarlo a un nuevo temporal
            String tempDer = (der.matches("T\\d+") || der.matches("\\d+")) ? der : obtenerTemporalLibre();
            if (!der.equals(tempDer)) {
                tabla.add(new String[]{tempDer, der, "="});
            }
    
            // Añadir la comparación a la tabla usando los temporales correctos
            tabla.add(new String[]{tempIzq, tempDer, op});
    
            if (esAND) {
                int saltoFalso = 0; // Salto fuera del ciclo
                int siguienteCond = tabla.size() + 3; // Salto para la siguiente condición
    
                saltosPendientes.add(new int[]{tabla.size(), siguienteCond, 1});
                tabla.add(new String[]{"TR1", "TRUE", Integer.toString(siguienteCond)});
    
                saltosPendientes.add(new int[]{tabla.size(), saltoFalso, 2});
                tabla.add(new String[]{"TR2", "FALSE", Integer.toString(saltoFalso)});
    
            } else { // OR
                int saltoTrue = indiceInicioCiclo + 10;
                saltosPendientes.add(new int[]{tabla.size(), saltoTrue, 1});
                tabla.add(new String[]{"TR1", "TRUE", Integer.toString(saltoTrue)});
    
                int saltoFalse = (i < condicionesArray.length - 1) ? tabla.size() + 2 : 0;
                saltosPendientes.add(new int[]{tabla.size(), saltoFalse, 2});
                tabla.add(new String[]{"TR2", "FALSE", Integer.toString(saltoFalse)});
            }
    
            // Liberar los temporales si fueron asignados
            if (!izq.equals(tempIzq)) liberarTemporal(tempIzq);
            if (!der.equals(tempDer)) liberarTemporal(tempDer);
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
            temporalesLibres.add(temporal); // Añadir el temporal a la lista de temporales libres solo si no está duplicado
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

