/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.innobyte.toosanalizer.utils;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author nik_1
 */
public class ExelTableRender extends JLabel implements TableCellRenderer{

    public ExelTableRender() {
        super.setOpaque(true);
    }
    

    @Override
    public Component getTableCellRendererComponent(
            JTable table, 
            Object value, 
            boolean isSelected, 
            boolean hasFocus, 
            int row, 
            int column) {
    
        int val = (Integer) value; 
        
        if(val == 4){
            super.setBackground(Color.MAGENTA);
        }else if(val == 3){
          super.setBackground(Color.RED);
        }else{
          super.setBackground(Color.CYAN);
        }

       
        return this;
    }
    
}
