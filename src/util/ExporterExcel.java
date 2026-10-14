package util;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ExporterExcel {

    public static void exportarTabla(JTable tabla, String nombreSugerido) {
        if (tabla == null || tabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "No hay datos en la tabla para exportar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar reporte para Excel");
        fileChooser.setSelectedFile(new File(nombreSugerido + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV de Excel (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File archivoGuardar = fileChooser.getSelectedFile();
            
            // Asegura la extensión .csv
            if (!archivoGuardar.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                archivoGuardar = new File(archivoGuardar.getAbsolutePath() + ".csv");
            }

            try (PrintWriter pw = new PrintWriter(archivoGuardar, StandardCharsets.UTF_8.name())) {
                // BOM para que Excel detecte acentos y eñes correctamente
                pw.write("\uFEFF");

                // 1. Encabezados
                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    pw.print("\"" + tabla.getColumnName(i) + "\"");
                    if (i < tabla.getColumnCount() - 1) pw.print(";");
                }
                pw.println();

                // 2. Filas de datos
                for (int row = 0; row < tabla.getRowCount(); row++) {
                    for (int col = 0; col < tabla.getColumnCount(); col++) {
                        Object val = tabla.getValueAt(row, col);
                        String celda = (val != null) ? val.toString().replace("\"", "\"\"") : "";
                        pw.print("\"" + celda + "\"");
                        if (col < tabla.getColumnCount() - 1) pw.print(";");
                    }
                    pw.println();
                }

                JOptionPane.showMessageDialog(null, "¡Reporte guardado correctamente en:\n" + archivoGuardar.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al generar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}