package server;

import LogTools.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class Sender {

    final String MY_NAME = "Sender";
    private final String TICS = "Tic";
    private final String TOES = "Toe";
    
    JTextArea Logs;
    Socket cs;
    String group_name = "";
    
    OutputStream cos = null; // Client Output Stream

    public Sender(JTextArea _Logs, Socket _cs, String _group_name) {
        cs = _cs;
        Logs = _Logs;
        group_name = _group_name;

        if (cs != null) {
            try {
                cos = cs.getOutputStream();
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public int SendCommand(String command) {
        if (group_name.equals(TICS)) {
            SendCommandToTics(command);
        } else if(group_name.equals(TOES)) {
            SendCommandToToes(command);
        } else {
            return -1;
        }
        
        return 0;
    }
    
    /*
      Messages
     Service info:
     1. EM - enemy info
     2. YT - your turn (if your turn then you can do the move)
     3. ET - enemy turn. Wait... (if enemy turn then you can't do the move. Wait msgs "EM")

     Game info:
     1. CA - Cell Accesible
     2. CN - Cell Not Accesible
     3. WIN - You win
     4. LOSE - You lose
     5. DRAW 
    
     For tics:
     6. KTO - Kill Toe (instead CA)
    
     For toes:
     7. KTI - Kill Tic (instead CA)
    
     And messages of this type <row:col> - enemy moves (if service info == EM)
     1. <Count_steps>
     2. Steps (<row:col>)
      
     */
    public void SendCommandToTics(String command) {
        if (cs != null) {
            DataOutputStream cdos = new DataOutputStream(cos);
            try {
                cdos.writeUTF(command);
                String info = "Game command " + command + " send to tics";
                Log.TagToLog(info, Logs, MY_NAME);
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void SendCommandToToes(String command) {
        if (cs != null) {
            DataOutputStream cdos = new DataOutputStream(cos);
            try {
                cdos.writeUTF(command);
                String info = "Game command " + command + " send to toes";
                Log.TagToLog(info, Logs, MY_NAME);
            } catch (IOException ex) {
                Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
