import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, Elemento> tabla;

    public TablaSimbolos() {
        this.tabla = new LinkedHashMap<>(); // Mantiene el orden de inserción
    }

    public void agregarElemento(String nombre, String tipo) {
        if (!tabla.containsKey(nombre)) {  // Verifica si ya existe
            Elemento elemento = new Elemento(nombre, tipo, "");
            tabla.put(nombre, elemento);
        } else {
            // Si ya existe, actualizar el tipo si es necesario
            tabla.get(nombre).setTipo(tipo);
        }
    }

    public boolean existeElemento(String nombre) {
        return tabla.containsKey(nombre);
    }

    public String obtenerTipo(String nombre) {
        if (existeElemento(nombre)) {
            return tabla.get(nombre).getTipo();
        }
        return "Vacio"; // Si no existe, devuelve vacío
    }

    public void mostrarTabla() {
        System.out.printf("%-20s %s%n", "Lexema", "Tipo de dato");
        System.out.println("---------------------------");

        List<Elemento> elementosOrdenados = new ArrayList<>(tabla.values());
        for (Elemento elemento : elementosOrdenados) {
            System.out.printf("%-20s %s%n", elemento.getNombre(), elemento.getTipo());
        }
    }
}
