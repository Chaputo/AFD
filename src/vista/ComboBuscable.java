package vista;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Convierte un JComboBox en un desplegable buscable: se escribe para filtrar
 * (sin distinguir mayúsculas ni tildes) y se elige de la lista con el mouse o las flechas + Enter.
 */
public final class ComboBuscable<T> {

    private final JComboBox<T> combo;
    private final Class<T> tipo;
    private final List<T> todos = new ArrayList<>();
    private boolean filtrando = false;

    private ComboBuscable(JComboBox<T> combo, Class<T> tipo) {
        this.combo = combo;
        this.tipo = tipo;
        combo.setEditable(true);
        JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE,
                         KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_TAB, KeyEvent.VK_SHIFT -> { return; }
                    default -> SwingUtilities.invokeLater(() -> filtrar(editor.getText()));
                }
            }
        });
    }

    /** Hace buscable el combo y le carga los ítems. */
    public static <T> ComboBuscable<T> aplicar(JComboBox<T> combo, Class<T> tipo) {
        return new ComboBuscable<>(combo, tipo);
    }

    public void setItems(List<T> items) {
        todos.clear();
        todos.addAll(items);
        filtrando = true;
        combo.setModel(new DefaultComboBoxModel<>(todos.toArray(newArray(todos.size()))));
        combo.setSelectedIndex(todos.isEmpty() ? -1 : 0);
        filtrando = false;
    }

    /** Muestra la lista completa y deja elegido el ítem indicado (o el primero que cumpla la condición). */
    public void seleccionar(java.util.function.Predicate<T> condicion) {
        filtrando = true;
        try {
            combo.setModel(new DefaultComboBoxModel<>(todos.toArray(newArray(todos.size()))));
            for (T item : todos) {
                if (condicion.test(item)) {
                    combo.setSelectedItem(item);
                    return;
                }
            }
            combo.setSelectedIndex(todos.isEmpty() ? -1 : 0);
        } finally {
            filtrando = false;
        }
    }

    /** El ítem elegido de la lista, o null si lo escrito no corresponde a ninguno. */
    public T getSeleccionado() {
        Object sel = combo.getSelectedItem();
        if (tipo.isInstance(sel)) return tipo.cast(sel);
        // Lo escrito coincide exactamente con un ítem: se toma ese
        String texto = sel == null ? "" : sel.toString().trim();
        for (T item : todos) {
            if (item.toString().equalsIgnoreCase(texto)) return item;
        }
        return null;
    }

    private void filtrar(String texto) {
        if (filtrando) return;
        filtrando = true;
        try {
            String buscado = normalizar(texto);
            List<T> coincidencias = new ArrayList<>();
            for (T item : todos) {
                if (normalizar(item.toString()).contains(buscado)) coincidencias.add(item);
            }
            JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
            int cursor = editor.getCaretPosition();
            combo.setModel(new DefaultComboBoxModel<>(coincidencias.toArray(newArray(coincidencias.size()))));
            combo.setSelectedIndex(-1);
            editor.setText(texto);
            editor.setCaretPosition(Math.min(cursor, texto.length()));
            if (combo.isShowing()) {
                combo.hidePopup();
                if (!coincidencias.isEmpty()) combo.showPopup();
            }
        } finally {
            filtrando = false;
        }
    }

    @SuppressWarnings("unchecked")
    private T[] newArray(int n) {
        return (T[]) java.lang.reflect.Array.newInstance(tipo, n);
    }

    private static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}
