public class ErrorSemantico {
    private String token;        // El token de error
    private String lexema;       // Lexema asociado
    private int renglon;         // Número de línea donde ocurrió el error
    private String descripcion;   // Descripción del error

    // Constructor
    public ErrorSemantico(String token, String lexema, int renglon, String descripcion) {
        this.token = token;
        this.lexema = lexema;
        this.renglon = renglon;
        this.descripcion = descripcion;
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getLexema() {
        return lexema;
    }

    public int getRenglon() {
        return renglon;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
