public class Elemento {
    private String nombre;  // Nombre del elemento (lexema)
    private String tipo;    // Tipo de dato (Entero, Cadena, Real, Operador, Vacio)
    private String valor;   // Valor asociado (puede ser utilizado para otros propósitos)

    // Constructor
    public Elemento(String nombre, String tipo, String valor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    // Setters
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
