package pe.innobyte.toosanalizer.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;
import java.util.*;

public class FormValidateUI {


    public static final String[] COLUMNS = new String[]{
            "Date", "Z", "Heart", "G", "F", "R", "S", "T", "W", "X", "AA", "AB"
    };
    public static final String[] COLUMNS2 = new String[]{
            "Date", "AC", "AD", "AE"
    };
    public static final String[] COLUMNS3 = new String[]{
            "Date", "AH", "AJ", "AK", "AL", "AM", "AN", "AO", "AQ"
    };
    public void showCombinedTable(Object[][] data, Object[][] data2) {
        // Crear el marco (JFrame)
        JFrame frame = new JFrame("Data Table");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 600); // Ajustar tamaño para acomodar ambas tablas

        frame.setLayout(new GridLayout(1, 2)); // Una fila, dos columnas

        // Crear el modelo de la tabla con los datos y columnas
        DefaultTableModel tableModel = new DefaultTableModel(data, COLUMNS);
        DefaultTableModel tableModel2 = new DefaultTableModel(data2, COLUMNS2);

        // Crear las tablas con los modelos
        JTable table = new JTable(tableModel);
        JTable table2 = new JTable(tableModel2);

        // Ajustar el ancho de las columnas de fecha en ambas tablas
        TableColumn dateColumn = table.getColumnModel().getColumn(0); // Columna de fecha
        dateColumn.setPreferredWidth(150);
        dateColumn.setMinWidth(150);
        dateColumn.setMaxWidth(150);

        TableColumn dateColumn2 = table2.getColumnModel().getColumn(0); // Columna de fecha
        dateColumn2.setPreferredWidth(150);
        dateColumn2.setMinWidth(150);
        dateColumn2.setMaxWidth(150);

        // Agregar las tablas a scroll panes
        JScrollPane scrollPane = new JScrollPane(table);
        JScrollPane scrollPane2 = new JScrollPane(table2);

        // Agregar los scroll panes al marco
        frame.getContentPane().add(scrollPane); // Primera tabla
        frame.getContentPane().add(scrollPane2); // Segunda tabla

        // Hacer el marco visible
        frame.setVisible(true);
    }

}
