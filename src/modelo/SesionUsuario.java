package modelo;

//Definición de la Clase y Atributos

public class SesionUsuario {
    private static Usuario usuarioLogueado;

    //Definición de los constructores

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