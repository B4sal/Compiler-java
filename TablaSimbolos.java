import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, String> tabla; // Para almacenar los símbolos y sus tipos
    private List<Elemento> elementos; // Para almacenar la descripción

    public TablaSimbolos() {
        tabla = new HashMap<>();
        elementos = new ArrayList<>();
    }

    // Agregar elemento a la tabla de símbolos
    public void agregarElemento(String nombre, String tipo) {
        tabla.put(nombre, tipo);
        String descripcion = "Sin valor asignado"; // Descripción por defecto

        // Actualizar la descripción según el tipo
        if (tipo.equals("Entero")) {
            descripcion = "Asignado con un valor entero " + nombre;
        } else if (tipo.equals("Real")) {
            descripcion = "Asignado con un valor real " + nombre;
        } else if (tipo.equals("Cadena")) {
            descripcion = "Asignado con una cadena " + nombre;
        } else if (tipo.equals("Vacio")) {
            descripcion = "Sin valor asignado";
        } else if (tipo.equals("Boolean")) {
            descripcion = "Asignado con un valor booleano " + nombre;
        }

        elementos.add(new Elemento(nombre, tipo, descripcion));
    }

    // Verificar si un elemento ya existe
    public boolean existeElemento(String nombre) {
        return tabla.containsKey(nombre);
    }

    // Obtener tipo de un elemento
    public String obtenerTipo(String nombre) {
        return tabla.getOrDefault(nombre, "Vacio");
    }

    // Mostrar la tabla de símbolos
    public void mostrarTabla() {
        System.out.printf("%-15s %-10s %-40s%n", "Lexema", "Tipo de dato", "Descripción");
        for (Elemento elem : elementos) {
            System.out.printf("%-15s %-10s %-40s%n", elem.getNombre(), elem.getTipo(), elem.getDescripcion());
        }
    }

    public List<Elemento> getElementos() {
        return elementos;
    }
    
}
