import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, String> expresionesUnicas = new HashMap<>(); // Mapea expresiones a variables
        int contadorTemporales = 0; // Contador para variables temporales (_z_)
        List<String> instruccionesAEliminar = new ArrayList<>(); // Lista de instrucciones a eliminar
    
        for (int i = 0; i < instrucciones.size(); i++) {
            String[] actual = instrucciones.get(i);
            String variableActual = actual[0];
            String expresionActual = actual[1];
    
            // Si la expresión ya existe, reutilizamos la variable temporal
            if (expresionesUnicas.containsValue(expresionActual)) {
                String variableExistente = obtenerVariablePorExpresion(expresionActual, expresionesUnicas);
    
                // Reemplazar la variable actual con la variable temporal existente
                reemplazarVariableEnCodigo(variableActual, variableExistente);
    
                // Marcar la instrucción redundante para eliminar
                String instruccionRedundante = variableActual + " = " + expresionActual;
                instruccionesAEliminar.add(instruccionRedundante);
            } else {
                // Si la expresión es nueva y no pertenece a una variable original
                if (!expresionesUnicas.containsKey(variableActual)) {
                    contadorTemporales++;
                    String nuevaVariable = "_z" + contadorTemporales + "_";
                    expresionesUnicas.put(nuevaVariable, expresionActual);
    
                    // Reemplazar en el código solo si es redundante
                    if (esRedundante(variableActual, expresionActual)) {
                        reemplazarVariableEnCodigo(variableActual, nuevaVariable);
                    }
                } else {
                    // Guardamos directamente si es la primera aparición
                    expresionesUnicas.put(variableActual, expresionActual);
                }
            }
        }
    
        // Eliminar todas las instrucciones redundantes marcadas
        for (String instruccion : instruccionesAEliminar) {
            eliminarInstruccion(instruccion);
        }
    }

    private boolean esRedundante(String variable, String expresion) {
        // Define si una variable es redundante (aparece más de una vez con la misma expresión)
        int count = 0;
        for (String[] instruccion : instrucciones) {
            if (instruccion[0].equals(variable) && instruccion[1].equals(expresion)) {
                count++;
            }
        }
        return count > 1;
    }

    private String obtenerVariablePorExpresion(String expresion, Map<String, String> mapa) {
        for (Map.Entry<String, String> entrada : mapa.entrySet()) {
            if (entrada.getValue().equals(expresion)) {
                return entrada.getKey();
            }
        }
        return null;
    }

    private void reemplazarVariableEnCodigo(String variable, String nuevoValor) {
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
