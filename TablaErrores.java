import java.util.ArrayList;
import java.util.List;

public class TablaErrores {
    private List<ErrorSemantico> errores;
    private int contadorErrores = 1;

    public TablaErrores() {
        this.errores = new ArrayList<>();
    }

    public void agregarError(String lexema, int renglon, String descripcion) {
        String token = "ErrSem" + contadorErrores++;
        errores.add(new ErrorSemantico(token, lexema, renglon, descripcion));
    }

    public void mostrarErrores() {
        System.out.printf("%-10s %-20s %-10s %-20s%n", "Token", "Lexema", "Renglón", "Descripción");
        System.out.println("------------------------------------------------------------");
        for (ErrorSemantico error : errores) {
            System.out.printf("%-10s %-20s %-10d %-20s%n", error.getToken(), error.getLexema(), error.getRenglon(), error.getDescripcion());
        }
    }
}
