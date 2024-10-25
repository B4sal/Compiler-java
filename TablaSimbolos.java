import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, Elemento> tabla;

    public TablaSimbolos() {
        this.tabla = new HashMap<>();
    }

    public void agregarElemento(String nombre, String tipo) {
        // Si ya existe un elemento con el mismo nombre, no lo agregamos nuevamente
        if (!tabla.containsKey(nombre) && !nombre.isEmpty()) {
            Elemento elemento = new Elemento(nombre, tipo);
            tabla.put(nombre, elemento);
        } else if (tabla.containsKey(nombre)) {
            // Si ya existe, no hacemos nada
            tabla.get(nombre).setTipo(tipo);
        }
    }

    public void mostrarTabla() {
        System.out.println("Lexema\t\tTipo de dato");
        System.out.println("---------------------------");
        for (Elemento elemento : tabla.values()) {
            // Cambiar la impresión para mostrar "Vacio" correctamente
            System.out.printf("%s\t\t%s\n", elemento.getNombre().isEmpty() ? " " : elemento.getNombre(), elemento.getTipo().equals("Identificador") ? "Vacio" : elemento.getTipo());
        }
    }
}
