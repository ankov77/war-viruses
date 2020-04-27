package server;

public class ClientGameCommand {
    private final String TICS = "Tic";
    private final String TOES = "Toe";
    
    public String command;
    public Object mutex;
    public String group_name;
    public int num_moves = 0;
    public String status;
    
    public ClientGameCommand() {
        mutex = new Object();
    }
}
