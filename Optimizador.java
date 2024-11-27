import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Optimizador {

    public static List<String> optimizarCodigo(List<String> codigo) {
        List<String> codigoOptimizado = new ArrayList<>();
        HashSet<String> variablesDeclaradas = new HashSet<>();
        HashSet<String> variablesUtilizadas = new HashSet<>();
        boolean dentroDeBloque = false;
        List<String> bloqueActual = new ArrayList<>();
        String encabezadoBloque = "";

        for (String linea : codigo) {
            // Detectar inicio de bloque (while)
            if (linea.startsWith("while")) {
                dentroDeBloque = true;
                encabezadoBloque = linea;

                // Detectar y registrar variables utilizadas en la condición del while
                variablesUtilizadas.addAll(extraerVariablesDeCondicion(linea));
                bloqueActual.clear();
            } else if (dentroDeBloque && linea.startsWith("}")) {
                // Detectar fin de bloque
                dentroDeBloque = false;
                // Optimizar el bloque
                List<String> bloqueOptimizado = optimizarBloque(encabezadoBloque, bloqueActual, variablesDeclaradas, variablesUtilizadas);
                codigoOptimizado.addAll(bloqueOptimizado);
            } else if (dentroDeBloque) {
                // Agregar línea al bloque actual
                bloqueActual.add(linea);
                // Registrar variables utilizadas dentro del bloque
                variablesUtilizadas.addAll(extraerVariablesDeLinea(linea));
            } else {
                // Procesar línea independiente (fuera de bloques)
                if (linea.contains("=")) {
                    String[] partes = linea.split("=");
                    String variable = partes[0].trim();
                    variablesDeclaradas.add(variable); // Marcar la variable como declarada

                    if (variablesUtilizadas.contains(variable)) {
                        codigoOptimizado.add(linea); // Mantener si la variable es usada
                    }
                } else {
                    // Mantener líneas que no son asignaciones
                    codigoOptimizado.add(linea);
                }
                // Registrar variables utilizadas en líneas independientes
                variablesUtilizadas.addAll(extraerVariablesDeLinea(linea));
            }
        }

        return codigoOptimizado;
    }

    private static List<String> optimizarBloque(String encabezado, List<String> bloque, HashSet<String> variablesDeclaradas, HashSet<String> variablesUtilizadas) {
        List<String> bloqueOptimizado = new ArrayList<>();
        bloqueOptimizado.add(encabezado); // Mantener el encabezado del bloque (ejemplo: while)

        // Procesar líneas dentro del bloque
        for (String linea : bloque) {
            if (linea.contains("=")) {
                String[] partes = linea.split("=");
                String variable = partes[0].trim();

                if (variablesUtilizadas.contains(variable) || variablesDeclaradas.contains(variable)) {
                    bloqueOptimizado.add(linea); // Mantener si se usa o está declarada
                }
            } else {
                bloqueOptimizado.add(linea); // Mantener líneas que no sean asignaciones
            }
        }

        bloqueOptimizado.add("}"); // Cerrar el bloque
        return bloqueOptimizado;
    }

    private static HashSet<String> extraerVariablesDeCondicion(String linea) {
        HashSet<String> variables = new HashSet<>();
        // Extraer variables en la condición del while (entre paréntesis)
        if (linea.contains("(") && linea.contains(")")) {
            String condicion = linea.substring(linea.indexOf("(") + 1, linea.indexOf(")"));
            variables.addAll(extraerVariablesDeLinea(condicion));
        }
        return variables;
    }

    private static HashSet<String> extraerVariablesDeLinea(String linea) {
        HashSet<String> variables = new HashSet<>();
        // Dividir la línea por operadores comunes y detectar nombres de variables
        String[] tokens = linea.split("[\\s+*/=<>!&|;,-]+");
        for (String token : tokens) {
            if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // Identificar nombres válidos de variables
                variables.add(token.trim());
            }
        }
        return variables;
    }
}
