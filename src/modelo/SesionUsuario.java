package modelo;

public class SesionUsuario {
    private static Usuario usuarioLogueado;

    public static void setUsuario(Usuario usuario) {
        usuarioLogueado = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioLogueado;
    }

    public static void limpiarSesion() {
        usuarioLogueado = null;
    }
}