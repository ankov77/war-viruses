package LogTools;

import javax.swing.JTextArea;

public class Log {
    public static void TagToLog(String info,
                         JTextArea jTextArea,
                         String my_name) {
        
        String curr_info = jTextArea.getText();
        curr_info += my_name + ": " + info + "\n";
        jTextArea.setText(curr_info);
    }
}