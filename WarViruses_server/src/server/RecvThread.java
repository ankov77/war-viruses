package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import LogTools.Log;
import java.util.Hashtable;

public class RecvThread extends Thread{
    final String MY_NAME = "RecvThread";
    
    Socket cs = null;
    InputStream sis = null;
    OutputStream sos = null;
    
    JTextArea Logs;
    boolean IsClientDisconnect = false;
    ClientGameCommand CGC;
    
    String group_name = "";
    
    Hashtable<String, Socket> Players;
    
    public RecvThread(Socket _cs,
                      JTextArea _Logs,
                      Hashtable<String, Socket> _Players,
                      ClientGameCommand _CGC) {
        cs = _cs;
        Logs = _Logs;
        Players = _Players;
        CGC = _CGC;

        if (cs != null) {
            try {
                sis = cs.getInputStream();
                sos = cs.getOutputStream();
            } catch (IOException ex) {
                Logger.getLogger(RecvThread.class.getName()).log(Level.SEVERE, "Error of getting intput stream", ex);
            }
        }
    }
    
    private void SendReplyToPlayer(String reply) {
        if (cs != null) {
            DataOutputStream sdos = new DataOutputStream(sos);

            try {
                sdos.writeUTF(reply);
            } catch (IOException ex) {
                Logger.getLogger(RecvThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void Disconnect() {
        IsClientDisconnect = true;
        
        String log_info = group_name.concat(" exited from game!");
        Log.TagToLog(log_info, Logs, MY_NAME);
        
        // Отправить противнику сообщение о победе
    }
    
    private void WrongCommand() {
        SendReplyToPlayer("WC");
    }
    
      /*
       1. Commands of server:
       
           1.5 "SO" - Receive file is OK
           1.6 "SN" - Receive file is not OK
           1.7 "DO" - Disconnect from server is OK
           1.8 "DN" - Disconnect from server is not OK
           1.9 "WC" - Wrong Command
     */
    
    private void HandleServiceCommand(String command) {
        if(command.equals("CNG")) {
            Players.put(group_name, cs);
        } else if(command.equals("JG")) {
            Players.put(group_name, cs);
                    CGC.group_name = group_name;
        } else if(command.equals("DG")) {
            Players.remove(group_name);
            Disconnect();
        }
    }

    private void HandleGameCommand(String command) {
        CGC.command = command;
        CGC.group_name = group_name;
         synchronized (CGC.mutex) {
        CGC.mutex.notify();
         }
    }

        
    private void HandlerOfClient() {
        DataInputStream sdis = new DataInputStream(sis);
        
        String command_info = "";
        String command = "";
        
        try {
            command_info = sdis.readUTF();
            command = sdis.readUTF();
            
            if(command_info.equals("SI")) {
                group_name = sdis.readUTF();
                HandleServiceCommand(command);
            } else if(command_info.equals("GI")) {
                HandleGameCommand(command);
            } else {
                WrongCommand();
            }
        } catch (IOException ex) {
            Logger.getLogger(RecvThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        while (true) {
            HandlerOfClient();
            if (IsClientDisconnect) {
                break;
            }
        }
    }

}
