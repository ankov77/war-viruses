package client;

import GameTools.GameAreaParameters;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import LogTools.Log;

public class RecvThread extends Thread {
    final String MY_NAME = "RecvThread";
    final int MAX_ROW_NUM = 10;
    final int MAX_COLUMN_NUM = 10;
    
    JTextArea Logs = null;
    Socket cs = null;
    InputStream cis = null;
    boolean IsConnect = true;
    JTable table;
    String group_name;

    public RecvThread(Socket _cs,
                      JTextArea _Logs,
                      JTable _table,
                      String _group_name) {
        
        cs = _cs;
        Logs = _Logs;
        table = _table;
        group_name = _group_name;
        
        if (cs != null) {
            try {
                cis = cs.getInputStream(); // Get the intput stream. Now we may receive the data from server
            } catch (IOException ex) {
                Logger.getLogger(RecvThread.class.getName()).log(Level.SEVERE, "Error of getting intput stream", ex);
            }
        }
    }
    
    private String getSubstringOfGameCommand(String command, String delimeter, int action) {
        // если в строке command есть delimeter и он не является первым символом в строке command и action == 1 (берем то, что после delimeter стоит)
        if (command.lastIndexOf(delimeter) != -1 && command.lastIndexOf(delimeter) != 0 && action == 1) // то вырезаем все знаки после delimeter в command, то есть <first_part><delimeter><second_part> -> <second_part>
        {
            return command.substring(command.lastIndexOf(delimeter) + 1);
        } // -//- action == 0 (берем то, что перед delimeter стоит)
        else if (command.lastIndexOf(delimeter) != -1 && command.lastIndexOf(delimeter) != 0 && action == 0) { // то вырезаем все знаки после delimeter в command, то есть <first_part><delimeter><second_part> -> <first_part>
            return command.substring(0, command.lastIndexOf(delimeter));
        }

        return ""; // Bad case
    }
    
    public static int LetterToNumber(String column) {
        for(int i = 0; i < GameAreaParameters.NUM_OF_COLUMNS; i++)
            if(GameAreaParameters.Column[i].equals(column)) {
                return i + 1;
            }
        
        return 0;
    }
    
    private void HandleCommandServer() {
        DataInputStream cdis = new DataInputStream(cis);

        String command = "";
        String info_turn;

        try {
            command = cdis.readUTF();

            if (command.equals("CA") || command.equals("KTI") || command.equals("KTO")) {
                String my_command_coord = cdis.readUTF();
                String row = getSubstringOfGameCommand(my_command_coord, ":", 0);
                String col = getSubstringOfGameCommand(my_command_coord, ":", 1);

                info_turn = cdis.readUTF();
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                if (command.equals("CA")) {
                    if (group_name.equals("Tic")) {
                        model.setValueAt("X", Integer.valueOf(row) - 1, LetterToNumber(col));
                    } else {
                        model.setValueAt("O", Integer.valueOf(row) - 1, LetterToNumber(col));
                    }
                } else if (command.equals("KTI")) {
                    model.setValueAt("KX", Integer.valueOf(row) - 1, LetterToNumber(col));
                } else {
                    model.setValueAt("KO", Integer.valueOf(row) - 1, LetterToNumber(col));
                }
                table.setModel(model);
                if (info_turn.equals("YT")) {
                    TurnPermit.MoveIsPermit = true;
                } else if (info_turn.equals("ET")) {
                    TurnPermit.MoveIsPermit = false;
                }
            } else if(command.equals("YT")) {
                System.out.println("1");
                TurnPermit.MoveIsPermit = true;
            } else if (command.equals("CN")) {
                Log.TagToLog("Cell is not available!", Logs, MY_NAME);
            } else if (command.equals("EM")) { // Receive enemy info
                int num_commands = Integer.valueOf(cdis.readUTF());
                System.out.println(num_commands);
                
                for (int i = 0; i < num_commands; i++) {
                    String comm = cdis.readUTF();
                    String status = cdis.readUTF();
                    
                    System.out.println(comm);
                    System.out.println(status);
                    
                    String row = getSubstringOfGameCommand(comm, ":", 0);
                    String col = getSubstringOfGameCommand(comm, ":", 1);
                    
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    model.setValueAt(status, Integer.valueOf(row) - 1, LetterToNumber(col));
                }
            } else if (command.equals("WIN")) {
                Log.TagToLog("You win!", Logs, MY_NAME);
                IsConnect = false;
            } else if (command.equals("LOSE")) {
                Log.TagToLog("You lose!", Logs, MY_NAME);
                IsConnect = false;
            } else if (command.equals("DRAW")) {
                Log.TagToLog("Draw!", Logs, MY_NAME);
                IsConnect = false;
            } else if (command.equals("SC")) {
                Log.TagToLog("Disconnect from game! Reason: server off", Logs, MY_NAME);
                IsConnect = false; // как то нужно сообщить об этом классу выше
            } else if (command.equals("WC")) {
                Log.TagToLog("Wrong command!", Logs, MY_NAME);
            }
        } catch (IOException ex) {
            Logger.getLogger(RecvThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        Log.TagToLog("Start!", Logs, MY_NAME);
            
        while (IsConnect) {
            HandleCommandServer();
        }

    }

}
