package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Hassan
 */
public class GomokuLogPanel extends JPanel {

    private JTextArea textBox;
    private JLabel statusLabel;    
    
    private final Handler TextBoxHandler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            String recordFormatted = this.getFormatter().formatMessage(record);
            appendText(recordFormatted);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    };
    
    public GomokuLogPanel(GomokuApplication app) {
        init();
        TextBoxHandler.setFormatter(new SimpleFormatter());
        Logger.getGlobal().addHandler(TextBoxHandler);
        Logger.getGlobal().setLevel(Level.ALL);
    }
    
    private void init() {
        Border loweredetched = BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED);
        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(loweredetched, "Log"), 
                new EmptyBorder(10, 10, 10, 10)));
        this.setLayout(new BorderLayout());
        
        this.textBox = new JTextArea();
        textBox.setLineWrap(true);
        DefaultCaret caret = (DefaultCaret) textBox.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textBox.setEditable(false);
        textBox.setFont(new Font("Sans Serif", Font.PLAIN, 11));
        textBox.setBackground(new Color(240, 240, 240));

        this.add(textBox, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane (textBox);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.add(scroll);
        
        this.statusLabel = new JLabel("Status: N/A");
        statusLabel.setBorder(new EmptyBorder(0, 0, 10, 10));
        this.add(statusLabel, BorderLayout.NORTH);
    }
    
    private void appendText(String text) {
        textBox.append(text + "\n");
    }
    
    public void setStatus(String text) {
        this.statusLabel.setText(text);
    }
    
    public void clear() {
        textBox.setText("");
    }
    
}
