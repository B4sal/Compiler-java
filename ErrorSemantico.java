public class ErrorSemantico {
    private String token;
    private String lexema;
    private int renglon;
    private String descripcion;

    public ErrorSemantico(String token, String lexema, int renglon, String descripcion) {
        this.token = token;
        this.lexema = lexema;
        this.renglon = renglon;
        this.descripcion = descripcion;
    }

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
