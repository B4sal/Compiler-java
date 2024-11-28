import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimizador {

    private String codigoOptimizado;
    private List<String[]> instrucciones;
    private Map<String, String> mapaVariables; // Mapeo de variables redundantes a sus equivalentes

    public Optimizador() {
        this.instrucciones = new ArrayList<>();
        this.mapaVariables = new HashMap<>();
    }

    public String optimizarCodigo(List<String> codigo) {
        StringBuilder codigoSinEspacios = new StringBuilder();
        for (String linea : codigo) {
            codigoSinEspacios.append(linea.replace(" ", "")).append("\n");
        }
        String codigoOriginal = codigoSinEspacios.toString();
        String[] lineasCodigo = codigoOriginal.split("\n");

        // Procesar cada instrucción para obtener expresiones
        for (String instruccion : lineasCodigo) {
            if (instruccion.contains("=")) {
                obtenerExpresion(instruccion);
            }
        }

        // Inicializar con el código original
        this.codigoOptimizado = codigoOriginal;

        // Analizar y optimizar expresiones
        analizarExpresiones();

        // Eliminar líneas vacías y formatear
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
    for (int i = 0; i < instrucciones.size(); i++) {
        String[] actual = instrucciones.get(i);
        String expresionActual = actual[1];

        for (int j = i + 1; j < instrucciones.size(); j++) {
            String[] siguiente = instrucciones.get(j);
            String expresionSiguiente = siguiente[1];

            if (expresionActual.equals(expresionSiguiente)) {
                if (!dependenciaAlteradaEntre(i, j, expresionSiguiente)) {
                    cambiarVariables(actual, siguiente);
                }
            }
        }
    }
}

// Método para verificar si alguna variable involucrada en la expresión fue alterada
private boolean dependenciaAlteradaEntre(int indiceInicio, int indiceFin, String expresion) {
    List<String> variablesEnExpresion = extraerVariablesDeExpresion(expresion);

    for (int i = indiceInicio + 1; i < indiceFin; i++) {
        String[] instruccion = instrucciones.get(i);
        String variableAsignada = instruccion[0];

        if (variablesEnExpresion.contains(variableAsignada)) {
            return true; // Dependencia alterada
        }
    }
    return false; // No se alteraron las dependencias
}

// Método para extraer variables de una expresión usando expresiones regulares
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

        int index = codigo.indexOf(variable);
        while (index != -1) {
            // Verificar que no estamos reemplazando una subcadena de otra variable
            if ((index == 0 || !Character.isLetterOrDigit(codigo.charAt(index - 1))) &&
                (index + variable.length() == codigo.length() || !Character.isLetterOrDigit(codigo.charAt(index + variable.length())))) {
                // Reemplazar variable por su nuevo valor
                codigo.replace(index, index + variable.length(), nuevoValor);
            }
            index = codigo.indexOf(variable, index + nuevoValor.length());
        }

        this.codigoOptimizado = codigo.toString();
    }

    private void eliminarInstruccion(String instruccion) {
        int index = codigoOptimizado.indexOf(instruccion);
        while (index != -1) {
            int end = codigoOptimizado.indexOf("\n", index);
            codigoOptimizado = codigoOptimizado.substring(0, index) + codigoOptimizado.substring(end + 1);
            index = codigoOptimizado.indexOf(instruccion);
        }
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
