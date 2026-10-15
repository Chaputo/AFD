package persistencia;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Período de fechas que filtra las consultas del dashboard:
 * todo el año actual, un mes del año actual, o un rango desde/hasta (ambos días incluidos).
 */
public final class Periodo {

    private final Integer mes;
    private final LocalDate desde;
    private final LocalDate hasta;

    private Periodo(Integer mes, LocalDate desde, LocalDate hasta) {
        this.mes = mes;
        this.desde = desde;
        this.hasta = hasta;
    }

    public static Periodo anioActual() {
        return new Periodo(null, null, null);
    }

    public static Periodo mes(int mes) {
        return new Periodo(mes, null, null);
    }

    public static Periodo rango(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            LocalDate aux = desde;
            desde = hasta;
            hasta = aux;
        }
        return new Periodo(null, desde, hasta);
    }

    /** Condición SQL sobre la columna fecha_pedido (con el alias de tabla, p. ej. "pe."). */
    String condicion(String alias) {
        String fecha = alias + "fecha_pedido";
        if (desde != null) {
            return fecha + " >= ? AND " + fecha + " < ?";
        }
        String anio = "YEAR(" + fecha + ") = YEAR(CURRENT_DATE())";
        return (mes != null) ? "MONTH(" + fecha + ") = ? AND " + anio : anio;
    }

    /** Carga los parámetros de la condición a partir de idx y devuelve el próximo índice libre. */
    int cargarParametros(PreparedStatement ps, int idx) throws SQLException {
        if (desde != null) {
            ps.setTimestamp(idx++, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(idx++, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
        } else if (mes != null) {
            ps.setInt(idx++, mes);
        }
        return idx;
    }

    /** Texto para mostrar el filtro aplicado (p. ej. en la exportación). */
    public String descripcion() {
        if (desde != null) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return desde.format(f) + " al " + hasta.format(f);
        }
        if (mes != null) {
            return java.time.Month.of(mes).getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "AR"))
                    + " " + LocalDate.now().getYear();
        }
        return "Todo el año " + LocalDate.now().getYear();
    }
}
