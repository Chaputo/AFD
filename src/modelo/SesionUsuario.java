package modelo;

/**
 * Gestor global de sesión de usuario (Patrón Singleton/Estado Estático).
 * Mantiene en memoria el usuario autenticado actualmente en la aplicación 
 * para el control de accesos y permisos por rol.
 */
public class SesionUsuario {
    
    private static Usuario usuarioLogueado;

    public static void setUsuario(Usuario usuario) {
        usuarioLogueado = usuario;
    }
    public static Usuario getUsuario() {
        return usuarioLogueado;
    }

    /**
     * ID del usuario en sesión, para registrar quién hace cada acción (p. ej. en la auditoría).
     * Si no hay sesión (procesos sin interfaz), devuelve el valor indicado.
     */
    public static int getIdUsuarioOr(int porDefecto) {
        return (usuarioLogueado != null) ? usuarioLogueado.getId() : porDefecto;
    }

    /**
     * Cierra la sesión activa liberando la referencia del usuario autenticado.
     */
    public static void limpiarSesion() {
        usuarioLogueado = null;
    }
}