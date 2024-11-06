import java.io.*;
import java.util.*;
import java.util.regex.*;

class GeneradorTriploDinamico {
    static int temporalID = 1; // ID para los temporales

    public static void procesarArchivo(String archivoEntrada, List<String[]> tabla) {
        try {
            File file = new File(archivoEntrada);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            // Procesar las líneas del archivo
            while ((line = br.readLine()) != null) {
                procesarLinea(line, br, tabla);
            }

            // Añadir el cierre con "…"
            tabla.add(new String[]{"...", "...", "..."});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void procesarTexto(String textoEntrada, List<String[]> tabla) {
        try (BufferedReader br = new BufferedReader(new StringReader(textoEntrada))) {
            String line;
            while ((line = br.readLine()) != null) {
                procesarLinea(line, br, tabla);
            }
            tabla.add(new String[]{"...", "...", "..."});
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
        String temporalActual = "T1"; // Usamos T1 como el temporal principal
    
        while (matcher.find()) {
            String token = matcher.group();
            if (token.matches("[+\\-*/]")) {
                operadores.add(token);
            } else {
                operandos.add(token);
            }
        }
    
        if (operandos.size() == 1) {
            tabla.add(new String[]{temporalActual, operandos.get(0), "="});
            return temporalActual;
        }
    
        for (int i = 1; i < operandos.size(); i++) {
            String operando1 = i == 1 ? operandos.get(0) : temporalActual;
            String operando2 = operandos.get(i);
            String operador = operadores.get(i - 1);
    
            tabla.add(new String[]{"T2", operando1, operador});
            tabla.add(new String[]{temporalActual, operando2, "="});
        }
    
        return temporalActual;
    }
    
    private static void procesarWhile(String line, BufferedReader br, List<String[]> tabla) throws IOException {
        String condiciones = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
        String[] condicionesArray = condiciones.split("&&|\\|\\|");
        boolean esAND = condiciones.contains("&&");
        
        int indiceInicioCiclo = tabla.size() + 1;
        List<int[]> saltosPendientes = new ArrayList<>();
    
        // Manejar las condiciones
        for (int i = 0; i < condicionesArray.length; i++) {
            String condicion = condicionesArray[i].trim();
            String[] partes = condicion.split(" ");
            String izq = partes[0].trim();
            String op = partes[1].trim();
            String der = partes[2].trim();
    
            String tempIzq = obtenerTemporalLibre();
            tabla.add(new String[]{tempIzq, izq, "="});
    
            String tempDer = obtenerTemporalLibre();
            tabla.add(new String[]{tempDer, der, "="});
    
            String tempComp = obtenerTemporalLibre();
            tabla.add(new String[]{tempComp, tempIzq, op});
    
            if (esAND) {
                int saltoFalso = 0; // Salto fuera del ciclo
                int siguienteCond = tabla.size() + 3; // Salto para la siguiente condición
    
                // Si es `AND`, fallo en una condición significa salida del ciclo.
                saltosPendientes.add(new int[]{tabla.size(), siguienteCond, 1});
                tabla.add(new String[]{"TR" + (temporalID++), "TRUE", Integer.toString(siguienteCond)});
                
                saltosPendientes.add(new int[]{tabla.size(), saltoFalso, 2});
                tabla.add(new String[]{"TR" + (temporalID++), "FALSE", Integer.toString(saltoFalso)});
    
            } else { // OR
                // Si es TRUE, saltar directamente al cuerpo del ciclo
                int saltoTrue = indiceInicioCiclo + 10; // Salta al contenido del while
                saltosPendientes.add(new int[]{tabla.size(), saltoTrue, 1});
                tabla.add(new String[]{"TR" + (temporalID++), "TRUE", Integer.toString(saltoTrue)});
    
                // Si es FALSE, saltar a la primera línea de la siguiente condición (si existe)
                int saltoFalse = (i < condicionesArray.length - 1) ? tabla.size() + 2 : 0;
                saltosPendientes.add(new int[]{tabla.size(), saltoFalse, 2});
                tabla.add(new String[]{"TR" + (temporalID++), "FALSE", Integer.toString(saltoFalse)});
            }
        }
    
        // Procesar el contenido dentro del ciclo while
        String innerLine;
        while ((innerLine = br.readLine()) != null) {
            innerLine = innerLine.trim();
            if (innerLine.equals("}")) {
                break;
            } else if (innerLine.contains("=")) {
                procesarAsignacion(innerLine, tabla);
            }
        }
    
        int indiceFinCiclo = tabla.size() + 2;
    
        // Actualizar saltos
        for (int[] salto : saltosPendientes) {
            int saltoPos = salto[0];
            tabla.get(saltoPos)[2] = (salto[1] == 0) ? Integer.toString(indiceFinCiclo) : Integer.toString(salto[1]);
        }
    
        // Añadir salto incondicional al inicio del ciclo
        tabla.add(new String[]{"vacio", Integer.toString(indiceInicioCiclo), "JR"});
    }
    
    

    private static String obtenerTemporalLibre() {
        return "T" + temporalID++;
    }

    public static void imprimirTabla(List<String[]> tabla) {
        System.out.println("ID\tDato Objeto\tDato Fuente\tOperador");
        int id = 1;
        for (String[] fila : tabla) {
            System.out.printf("%d\t%s\t\t%s\t\t%s%n", id++, fila[0], fila[1], fila[2]);
        }
    }

    
}