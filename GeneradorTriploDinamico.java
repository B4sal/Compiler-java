import java.io.*;
import java.util.*;
import java.util.regex.*;

class GeneradorTriploDinamico {
    static int temporalID = 1; // ID para los temporales
    static int lineaID = 1; // ID para las líneas

    public static void main(String[] args) {
        try {
            File file = new File("datos.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            List<String[]> tabla = new ArrayList<>();

            // Procesar las líneas del archivo
            while ((line = br.readLine()) != null) {
                procesarLinea(line, br, tabla);
            }

            // Añadir el cierre con "…"
            tabla.add(new String[]{"...", "...", "..."});
            imprimirTabla(tabla);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void procesarLinea(String line, BufferedReader br, List<String[]> tabla) throws IOException {
        line = line.trim();

        if (line.startsWith("while")) {
            procesarWhile(line, br, tabla);
        } else if (line.contains("=")) {
            procesarAsignacion(line, tabla);
        }
    }

    private static void procesarAsignacion(String line, List<String[]> tabla) {
        String[] partes = line.split("=");
        String variable = partes[0].trim();
        String expresion = partes[1].trim();

        String temporal = evaluarExpresion(expresion, tabla);
        tabla.add(new String[]{variable, temporal, "="});
    }

    private static String evaluarExpresion(String expresion, List<String[]> tabla) {
        Matcher matcher = Pattern.compile("[\\w]+|[+\\-*/]").matcher(expresion);
        List<String> operandos = new ArrayList<>();
        List<String> operadores = new ArrayList<>();

        while (matcher.find()) {
            String token = matcher.group();
            if (token.matches("[+\\-*/]")) {
                operadores.add(token);
            } else {
                operandos.add(token);
            }
        }

        // Asignación inicial
        String temporal = obtenerTemporalLibre();
        if (!operandos.isEmpty()) {
            tabla.add(new String[]{temporal, operandos.get(0), "="});
        }

        // Generar triplos para la expresión
        for (int i = 1; i < operandos.size(); i++) {
            String tempNuevo = obtenerTemporalLibre();
            tabla.add(new String[]{tempNuevo, temporal, operadores.get(i - 1)});
            tabla.add(new String[]{temporal, operandos.get(i), "="});
            temporal = tempNuevo; // Actualiza el temporal
        }

        return temporal; // Retorna el último temporal
    }
    private static void procesarWhile(String line, BufferedReader br, List<String[]> tabla) throws IOException {
        String condiciones = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
        String[] condicionesArray = condiciones.split("&&");
    
        // Guardar el índice de inicio del ciclo
        int indiceInicioCiclo = lineaID;
    
        // Procesar cada condición del while
        for (String condicion : condicionesArray) {
            String[] partes = condicion.trim().split(" ");
            String izq = partes[0].trim();
            String op = partes[1].trim();
            String der = partes[2].trim();
    
            String tempIzq = obtenerTemporalLibre();
            tabla.add(new String[]{tempIzq, izq, "="});
    
            String tempDer = obtenerTemporalLibre();
            tabla.add(new String[]{tempDer, der, "="});
    
            String tempComp = obtenerTemporalLibre();
            tabla.add(new String[]{tempComp, tempIzq, op});
    
            // Determinar las líneas de salto
            int lineaTrue = lineaID + 1; // Línea siguiente después de la condición
            int lineaFalse = -1; // Se asignará luego el valor correcto
    
            // Agregar saltos para TRUE y FALSE
            tabla.add(new String[]{"TR" + (temporalID++), "TRUE", Integer.toString(lineaTrue)});
            tabla.add(new String[]{"TR" + (temporalID++), "FALSE", "temp"}); // Asignar temporalmente "temp"
        }
    
        // Procesar el contenido dentro del ciclo while
        String innerLine;
        while ((innerLine = br.readLine()) != null) {
            innerLine = innerLine.trim();
            if (innerLine.equals("}")) {  // Fin del bloque while
                break;
            } else if (innerLine.contains("=")) {
                procesarAsignacion(innerLine, tabla);
            }
            lineaID++; // Aumentar el ID de línea para cada línea procesada
        }
    
        // Obtener el índice del fin del ciclo
        int indiceFinCiclo = lineaID; // Obtener el índice de la línea que sigue al cierre del bloque while
    
        // Actualizar los saltos
        for (String[] row : tabla) {
            if (row[2].equals("temp")) {
                row[2] = Integer.toString(indiceFinCiclo); // Asignar el índice final a los saltos FALSE
            }
        }
    
        // Añadir el salto incondicional para regresar al inicio del ciclo
        tabla.add(new String[]{"vacio", Integer.toString(indiceInicioCiclo), "JR"});
    }
    
    
    
    
    

    private static String obtenerTemporalLibre() {
        return "T" + temporalID++;
    }

    private static void imprimirTabla(List<String[]> tabla) {
        System.out.println("ID\tDato Objeto\tDato Fuente\tOperador");
        int id = 1;
        for (String[] fila : tabla) {
            System.out.printf("%d\t%s\t\t%s\t\t%s%n", id++, fila[0], fila[1], fila[2]);
        }
    }
}
