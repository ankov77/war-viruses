package server;

import java.util.Map;
import GameTools.GameRules;
import GameTools.GameAreaParameters;

import LogTools.Log;
import java.net.Socket;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class GameThread extends Thread {

    private Map<String, Map<String, GameAreaParameters.CELL_STATE>> GameState = GameAreaParameters.GAME_STATE_INIT;
    private final String MY_NAME = "GameThread";
    private final String FIRST_MOVE = "Tic";

    private Hashtable<String, Socket> Players;

    JTextArea Logs;

    int num_toes = 1; // Current number of toes in game
    int num_tics = 1; // -//- tics

    String winner = "";

    ClientGameCommand CGC_tic;
    ClientGameCommand CGC_toe;

    ClientGameCommand MyMoves[] = new ClientGameCommand[3];

    int count_turn_tic = 1;
    int count_turn_toe = 1;
    
    int countfornemysend_toe = 1; // delete_me
    int countfornemysend_tic = 1; // delete_me

    boolean IsInit = true;
    
    boolean IsFirstMoveToe = true;

    public GameThread(JTextArea _Logs,
            Hashtable<String, Socket> _Players,
            ClientGameCommand _CGC_tic,
            ClientGameCommand _CGC_toe) {
        Logs = _Logs;
        Players = _Players;
        CGC_tic = _CGC_tic;
        CGC_toe = _CGC_toe;
        
        for(int i = 0; i < 3; i++) {
            MyMoves[i] = new ClientGameCommand();
        }
        
        GameRules.num_turn = 1;
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

    private void HandleTicMoves() {
        String row = getSubstringOfGameCommand(CGC_tic.command, ":", 0);
        String col = getSubstringOfGameCommand(CGC_tic.command, ":", 1);

        if (GameRules.IsCellAcessible(CGC_tic.group_name, row, col, GameState)) {
            GameAreaParameters.CELL_STATE state = GameRules.GetValue(row, col, GameState);
            Sender ST = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name);
            MyMoves[count_turn_tic].command = CGC_tic.command;
            count_turn_tic++;

            if (state.equals(GameAreaParameters.CELL_STATE.CELL_EMPTY)) {
                GameRules.num_turn++;
                
                if (num_tics == -1) {
                    num_tics += 2;
                } else {
                    num_tics++;
                }
                
                GameRules.OccupieCell(row, col, GameState, "Tic");
                ST.SendCommand("CA");
                ST.SendCommand(CGC_tic.command);
                MyMoves[count_turn_tic - 1].status = "X";
            } else if (state.equals(GameAreaParameters.CELL_STATE.TOE_HERE)) {
                GameRules.num_turn++;
                
                GameRules.KillToe(row, col, GameState);
                num_toes--;

                ST.SendCommand("KTO");
                ST.SendCommand(CGC_tic.command);
                MyMoves[count_turn_tic - 1].status = "KO";
            }

            if (count_turn_tic < 3) {
                ST.SendCommand("YT");
            } else {
                ST.SendCommand("ET");
                
                Sender ST_2 = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name); // уведомляем оппонента о начале его хода
                ST_2.SendCommand("YT");
            }
        } else {
            Sender ST = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name);

            ST.SendCommand("CN");
        }
    }

    private void HandleToeMoves() {
        String row = getSubstringOfGameCommand(CGC_toe.command, ":", 0);
        String col = getSubstringOfGameCommand(CGC_toe.command, ":", 1);

        if (GameRules.IsCellAcessible(CGC_toe.group_name, row, col, GameState)) {
            GameAreaParameters.CELL_STATE state = GameRules.GetValue(row, col, GameState);
            Sender ST = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name);
            MyMoves[count_turn_toe].command = CGC_toe.command;
            count_turn_toe++;

            if (state.equals(GameAreaParameters.CELL_STATE.CELL_EMPTY)) {
                GameRules.num_turn++;
                
                if (num_toes == -1) {
                    num_toes += 2;
                } else {
                    num_toes++;
                }

                GameRules.OccupieCell(row, col, GameState, "Toe");
                ST.SendCommand("CA");
                ST.SendCommand(CGC_toe.command);
                MyMoves[count_turn_toe - 1].status = "O";
            } else if (state.equals(GameAreaParameters.CELL_STATE.TIC_HERE)) {
                GameRules.num_turn++;
                
                GameRules.KillTic(row, col, GameState);
                num_tics--;

                ST.SendCommand("KTI");
                ST.SendCommand(CGC_toe.command);
                MyMoves[count_turn_toe - 1].status = "KX";
            }

            if (count_turn_toe < 3) {
                ST.SendCommand("YT");
            } else {
                ST.SendCommand("ET");
                
                Sender ST_2 = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name); // уведомляем оппонента о начале его хода
                ST_2.SendCommand("YT");
            }
        } else {
            Sender ST = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name);

            ST.SendCommand("CN");
        }
    }

    private void Draw() {
        Sender ST_1 = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name);
        ST_1.SendCommand("DRAW");

        Sender ST_2 = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name);
        ST_2.SendCommand("DRAW");
    }

    private void Deliverance(String _winner) {
        if (_winner.equals("Tic")) {
            Sender ST_1 = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name);
            ST_1.SendCommand("WIN");

            Sender ST_2 = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name);
            ST_2.SendCommand("LOSE");
        } else {
            Sender ST_1 = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name);
            ST_1.SendCommand("LOSE");

            Sender ST_2 = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name);
            ST_2.SendCommand("WIN");
        }
    }

    private void SendEnemyMoves(String enemy) {
        if (enemy.equals("Tic")) {
            if (count_turn_tic > 0) {
                Sender ST = new Sender(Logs, Players.get(CGC_toe.group_name), CGC_toe.group_name);
                ST.SendCommand("EM");
                ST.SendCommand(Integer.toString(count_turn_tic - countfornemysend_toe));
                for (int i = countfornemysend_toe; i < count_turn_tic; i++) {
                    ST.SendCommand(MyMoves[i].command);
                    ST.SendCommand(MyMoves[i].status);
                }
                
                countfornemysend_toe = 0;
            }
        } else {
            if (count_turn_toe > 0) {
                Sender ST = new Sender(Logs, Players.get(CGC_tic.group_name), CGC_tic.group_name);
                ST.SendCommand("EM");
                ST.SendCommand(Integer.toString(count_turn_toe - countfornemysend_tic));

                for (int i = countfornemysend_tic; i < count_turn_toe; i++) {
                    ST.SendCommand(MyMoves[i].command);
                    ST.SendCommand(MyMoves[i].status);
                }
                
                countfornemysend_tic = 0;
            }
        }
    }

    @Override
    public void run() {
        while (!GameRules.GameIsEnd(num_tics, num_toes)) {
            if (GameRules.WhoseTurn().equals("Tic")) {
                //System.out.println("I 5");
                if (!IsInit) {
                    SendEnemyMoves("Toe"); // Send to tic info about toe moves
                }
                count_turn_toe = 0;
                IsInit = false;

                synchronized (CGC_tic.mutex) {
                    try {
                        CGC_tic.mutex.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                HandleTicMoves();
            } else if (GameRules.WhoseTurn().equals("Toe")) {
                if (!IsInit) {
                   // System.out.println("I 0");
                    SendEnemyMoves("Tic"); // Send to toe info about tic moves
                }
                
                count_turn_tic = 0;
                
                if(IsFirstMoveToe) {
                    GameRules.num_turn = 1;
                count_turn_toe = 1;
                }
                
                IsFirstMoveToe = false;
                IsInit = false;
                
                //System.out.println("I 1");
                synchronized (CGC_toe.mutex) {
                    try {
                        CGC_toe.mutex.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GameThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                //System.out.println("I 2");

                HandleToeMoves();
            } else {
                Draw();
            }
        }

        winner = GameRules.WhoWin(num_tics, num_toes);
        Deliverance(winner);
    }
}
