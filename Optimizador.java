import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimizador {
    private String codigoOptimizado;
    private List<String[]> instrucciones;

    // Lista de variables que no deben ser optimizadas
    private List<String> variablesNoOptimizables;

    public Optimizador() {
        this.instrucciones = new ArrayList<>();
        this.variablesNoOptimizables = new ArrayList<>();
        // Añadimos _Indefinido_ a la lista de variables no optimizables
        variablesNoOptimizables.add("_Indefinido_");
    }

    public String optimizarCodigo(List<String> codigo) {
        StringBuilder codigoSinEspacios = new StringBuilder();
        for (String linea : codigo) {
            codigoSinEspacios.append(linea.replace(" ", "")).append("\n");
        }
        String codigoOriginal = codigoSinEspacios.toString();
        String[] lineasCodigo = codigoOriginal.split("\n");

        // Almacenamos las instrucciones con la forma [variable, expresión]
        for (String instruccion : lineasCodigo) {
            if (instruccion.contains("=")) {
                obtenerExpresion(instruccion);
            }
        }

        // Empezamos a analizar las expresiones para optimizarlas
        this.codigoOptimizado = codigoOriginal;
        analizarExpresiones();

        // Eliminamos las líneas vacías
        StringBuilder resultado = eliminarLineasVacias(new StringBuilder(codigoOptimizado));

        return formatearCodigoPorLineas(resultado.toString());
    }

    private void obtenerExpresion(String instruccion) {
        String[] partes = instruccion.split("=");
        if (partes.length == 2) {
            String variable = partes[0].trim();
            String expresion = partes[1].trim();
            instrucciones.add(new String[]{variable, expresion});
        }
    }

    private void analizarExpresiones() {
        // Analizamos las expresiones y las optimizamos
        for (int i = 0; i < instrucciones.size(); i++) {
            String[] actual = instrucciones.get(i);
            String expresionActual = actual[1];

            for (int j = i + 1; j < instrucciones.size(); j++) {
                String[] siguiente = instrucciones.get(j);
                String expresionSiguiente = siguiente[1];

                // Si las expresiones son iguales y no están en la lista de no optimizables
                if (expresionActual.equals(expresionSiguiente) && 
                    !variablesNoOptimizables.contains(actual[0])) {
                    
                    // Si no hay alteración de dependencias, reemplazamos la variable
                    if (!dependenciaAlteradaEntre(i, j, expresionSiguiente)) {
                        cambiarVariables(actual, siguiente);
                    }
                }
            }
        }
    }

    private boolean dependenciaAlteradaEntre(int indiceInicio, int indiceFin, String expresion) {
        List<String> variablesEnExpresion = extraerVariablesDeExpresion(expresion);

        for (int i = indiceInicio + 1; i < indiceFin; i++) {
            String[] instruccion = instrucciones.get(i);
            String variableAsignada = instruccion[0];

            // Si la variable está en la lista negra, no la optimizamos
            if (variablesNoOptimizables.contains(variableAsignada)) {
                continue;
            }

            if (variablesEnExpresion.contains(variableAsignada)) {
                return true; // Dependencia alterada
            }
        }
        return false; // No se alteraron las dependencias
    }

    private List<String> extraerVariablesDeExpresion(String expresion) {
        List<String> variables = new ArrayList<>();
        Pattern patron = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
        Matcher matcher = patron.matcher(expresion);

        while (matcher.find()) {
            variables.add(matcher.group());
        }
        return variables;
    }

    private void cambiarVariables(String[] linea1, String[] linea2) {
        String instruccion2 = linea2[0] + "=" + linea2[1];
        StringBuilder codigo = new StringBuilder(codigoOptimizado);
        codigo = eliminarInstruccion(codigo, instruccion2);

        if (linea1.length > 0 && linea2.length > 0) {
            String variable = linea2[0];
            String nuevoValor = linea1[0];

            // Reemplazamos la variable en el código, pero solo si no está en la lista negra
            if (!variablesNoOptimizables.contains(variable)) {
                int index = 0;
                while ((index = codigo.indexOf(variable, index)) != -1) {
                    codigo.replace(index, index + variable.length(), nuevoValor);
                    index += nuevoValor.length();
                }
            }
        }

        this.codigoOptimizado = codigo.toString();
    }

    private StringBuilder eliminarInstruccion(StringBuilder codigo, String instruccion) {
        int index = codigo.indexOf(instruccion);
        if (index != -1) {
            codigo.delete(index, index + instruccion.length());
        }
        return codigo;
    }

    private StringBuilder eliminarLineasVacias(StringBuilder codigo) {
        String[] lineas = codigo.toString().split("\n");
        StringBuilder resultado = new StringBuilder();
        for (String linea : lineas) {
            if (!linea.trim().isEmpty()) {
                resultado.append(linea).append("\n");
            }
        }
        return resultado;
    }

    private String formatearCodigoPorLineas(String codigo) {
        String[] lineas = codigo.split("\n");
        StringBuilder resultado = new StringBuilder();

        for (String linea : lineas) {
            if (!linea.trim().isEmpty()) {
                resultado.append(formatearLinea(linea)).append("\n");
            }
        }

        return resultado.toString();
    }

    private String formatearLinea(String linea) {
        return linea
                .replaceAll("=", " = ")
                .replaceAll("\\+", " + ")
                .replaceAll("-", " - ")
                .replaceAll("\\*", " * ")
                .replaceAll("/", " / ")
                .replaceAll("&&", " && ")
                .replaceAll("%", " % ")
                .replaceAll("\\|\\|", " || ")
                .replaceAll("<\\s*=", "<=")
                .replaceAll(">\\s*=", ">=")
                .replaceAll("<", " < ")
                .replaceAll(">", " > ")
                .replaceAll("\\(", " ( ")
                .replaceAll("\\)", " ) ")
                .replaceAll("\\{", " { ")
                .replaceAll("\\}", " } ")
                .replaceAll("\\s+", " ") // Elimina espacios extra
                .trim();
    }
}
