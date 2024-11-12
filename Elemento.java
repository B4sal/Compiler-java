public class Elemento {
    private String nombre; // Nombre del elemento
    private String tipo; // Tipo del elemento
    private String descripcion; // Descripción del elemento

    public Elemento(String nombre, String tipo, String descripcion) {
        this.nombre = nombre; // Inicializar el nombre
        this.tipo = tipo; // Inicializar el tipo
        this.descripcion = descripcion; // Inicializar la descripción
    }

    public String getNombre() {
        return nombre; // Obtener el nombre
    }

    public String getTipo() {
        return tipo; // Obtener el tipo
    }

    public String getDescripcion() {
        return descripcion; // Obtener la descripción
    }
}
