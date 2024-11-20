import java.io.*;
import java.util.*;

class GeneradorTriploDinamico {
    static int temporalID = 1; // ID para los temporales
    static List<String> temporalesLibres = new ArrayList<>(); // Lista de temporales libres

    private static final Map<String, Integer> OPERADOR_PRECEDENCIA = new HashMap<>() {{
        put("+", 1);
        put("-", 1);
        put("*", 2);
        put("/", 2);
        put("(", 0);
    }};

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
        line = line.trim();

        if (line.startsWith("while")) {
            procesarWhile(line, br, tabla); // Procesar bloque while
        } else if (line.contains("=")) {
            procesarAsignacion(line, tabla); // Procesar asignaciones estándar
        } else if (line.matches("\\w+\\s*\\+\\s*1")) { // Manejar incrementos del tipo var + 1
            procesarIncremento(line, tabla);
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

    private static void procesarIncremento(String line, List<String[]> tabla) {
        String variable = line.split("\\+")[0].trim(); // Extraer la variable antes del "+"
        String temporal = obtenerTemporalLibre();

        // Generar triplos para el incremento
        tabla.add(new String[]{temporal, variable, "="}); // T1 = variable
        tabla.add(new String[]{temporal, "1", "+"});     // T1 + 1
        tabla.add(new String[]{variable, temporal, "="}); // variable = T1

        liberarTemporal(temporal); // Liberar temporal usado
    }

    private static String evaluarExpresion(String expresion, List<String[]> tabla) {
        // Convertir a notación postfija usando Shunting Yard
        List<String> postfija = convertirAPostfija(expresion);

        Stack<String> pilaTemporales = new Stack<>();

        for (String token : postfija) {
            if (OPERADOR_PRECEDENCIA.containsKey(token)) {
                // Es un operador, procesa los operandos
                String operandoDerecho = pilaTemporales.pop();
                String operandoIzquierdo = pilaTemporales.pop();
                obtenerTemporalLibre();

                // Generar un único triplo para la operación
                tabla.add(new String[]{operandoDerecho, operandoIzquierdo, token});
                pilaTemporales.push(operandoDerecho);
            } else {
                // Es un operando, añadir directamente a la pila
                String temporal = obtenerTemporalLibre();
                tabla.add(new String[]{temporal, token, "="});
                pilaTemporales.push(temporal);
            }
        }
        
        // El resultado final está en el tope de la pila
        return pilaTemporales.pop();
    }

    private static List<String> convertirAPostfija(String expresion) {
        List<String> salida = new ArrayList<>();
        Stack<String> operadores = new Stack<>();
        String[] tokens = expresion.split("\\s+");

        for (String token : tokens) {
            if (OPERADOR_PRECEDENCIA.containsKey(token)) {
                // Es un operador
                while (!operadores.isEmpty() &&
                    OPERADOR_PRECEDENCIA.get(operadores.peek()) >= OPERADOR_PRECEDENCIA.get(token)) {
                    salida.add(operadores.pop());
                }
                operadores.push(token);
            } else if (token.equals("(")) {
                operadores.push(token);
            } else if (token.equals(")")) {
                while (!operadores.isEmpty() && !operadores.peek().equals("(")) {
                    salida.add(operadores.pop());
                }
                operadores.pop(); // Eliminar el "(" de la pila
            } else {
                // Es un operando
                salida.add(token);
            }
        }

        // Vaciar los operadores restantes
        while (!operadores.isEmpty()) {
            salida.add(operadores.pop());
        }

        return salida;
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
            } else if (innerLine.matches("\\w+\\s*\\+\\s*1")) { // Manejar incrementos del tipo var + 1
                procesarIncremento(innerLine, tabla);
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

