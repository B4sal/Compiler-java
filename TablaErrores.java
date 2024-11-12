import java.util.ArrayList;
import java.util.List;

public class TablaErrores {
    private List<ErrorSemantico> errores; // Lista de errores semánticos
    private int contadorErrores = 1; // Contador de errores

    public TablaErrores() {
        this.errores = new ArrayList<>(); // Inicializar la lista de errores
    }

    public void agregarError(String lexema, int renglon, String descripcion) {
        String token = "ErrSem" + contadorErrores++; // Generar un token para el error
        errores.add(new ErrorSemantico(token, lexema, renglon, descripcion)); // Agregar un nuevo error a la lista
    }

    public void mostrarErrores() {
        System.out.printf("%-10s %-20s %-10s %-20s%n", "Token", "Lexema", "Renglón", "Descripción"); // Imprimir encabezados de la tabla de errores
        System.out.println("------------------------------------------------------------");
        for (ErrorSemantico error : errores) {
            System.out.printf("%-10s %-20s %-10d %-20s%n", error.getToken(), error.getLexema(), error.getRenglon(), error.getDescripcion()); // Imprimir cada error
        }
    }
}
