public class Elemento {
    private String nombre;
    private String tipo; // Puede ser "Entero", "Cadena", "Operador", etc.

    public Elemento(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return nombre + " es un " + tipo;
    }
}
