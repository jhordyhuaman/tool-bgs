/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.innobyte.toosanalizer.utils;

import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author nik_1
 */
public class ExelTableModel extends AbstractTableModel{

    private final List<ExelTable> exelTable;

    public ExelTableModel(List<ExelTable> exelTable) {
        this.exelTable = exelTable;
    }
    
    // table values----------------------
    
    private final String[] columnNames = new String[] {
      "Date", "Hours", "Intencity", "Raw-Kind", "Heart", "Steps"
    };
    private final Class[] columnClass = new Class[] {
        String.class, String.class, String.class,ExelTable.class,String.class,String.class
    };
    
    @Override
    public String getColumnName(int column)
    {
        return columnNames[column];
    }
 
    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
        return columnClass[columnIndex];
    }

    @Override
    public int getRowCount() {
        return  exelTable.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ExelTable row = exelTable.get(rowIndex);
        switch(columnIndex){
            case 0:
                return row.getDate();
            case 1:
                return row.getHours();            
            case 2:
                return row.getIntencity();
            case 3:
                return row.getKind();
            case 4:
                return row.getHeart();
            case 5:
                return row.getSteps();
        }
        return null;
    }
    
}
