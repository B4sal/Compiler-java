public class ErrorSemantico {
    private String token; // Token del error
    private String lexema; // Lexema asociado al error
    private int renglon; // Línea donde ocurrió el error
    private String descripcion; // Descripción del error

    public ErrorSemantico(String token, String lexema, int renglon, String descripcion) {
        this.token = token; // Inicializar el token
        this.lexema = lexema; // Inicializar el lexema
        this.renglon = renglon; // Inicializar el renglón
        this.descripcion = descripcion; // Inicializar la descripción
    }

    public String getToken() {
        return token; // Obtener el token
    }

    public String getLexema() {
        return lexema; // Obtener el lexema
    }

    public int getRenglon() {
        return renglon; // Obtener el renglón
    }

    public String getDescripcion() {
        return descripcion; // Obtener la descripción
    }
}
