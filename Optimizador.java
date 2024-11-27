import java.util.ArrayList;
import java.util.List;

public class Optimizador {
    private String codigoOptimizado;
    private List<String[]> instrucciones;

    public Optimizador() {
        this.instrucciones = new ArrayList<>();
    }

    public String optimizarCodigo(List<String> codigo) {
        StringBuilder codigoSinEspacios = new StringBuilder();
        for (String linea : codigo) {
            codigoSinEspacios.append(linea.replace(" ", "")).append("\n");
        }
        String codigoOriginal = codigoSinEspacios.toString();
        String[] lineasCodigo = codigoOriginal.split("\n");

        for (String instruccion : lineasCodigo) {
            if (instruccion.contains("=")) {
                obtenerExpresion(instruccion);
            }
        }

        this.codigoOptimizado = codigoOriginal;
        analizarExpresiones();
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
                    cambiarVariables(actual, siguiente);
                }
            }
        }
    }

    private void cambiarVariables(String[] linea1, String[] linea2) {
        String instruccion2 = linea2[0] + "=" + linea2[1];
        StringBuilder codigo = new StringBuilder(codigoOptimizado);
        codigo = eliminarInstruccion(codigo, instruccion2);

        if (linea1.length > 0 && linea2.length > 0) {
            String variable = linea2[0];
            String nuevoValor = linea1[0];
            int index = codigo.indexOf(variable);
            while (index != -1) {
                codigo.replace(index, index + variable.length(), nuevoValor);
                index = codigo.indexOf(variable, index + nuevoValor.length());
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
                .replaceAll("\\|\\|", " || ")
                // Aseguramos que <= y >= no se separen
                .replaceAll("<\\s*=", "<=")
                .replaceAll(">\\s*=", ">=")
                .replaceAll("<", " <")
                .replaceAll(">", " >")
                .replaceAll("\\(", " ( ")
                .replaceAll("\\)", " ) ")
                .replaceAll("\\{", " { ")
                .replaceAll("\\}", " } ")
                .replaceAll("\\s+", " ") // Elimina espacios extra
                .trim();
    }
    
}
