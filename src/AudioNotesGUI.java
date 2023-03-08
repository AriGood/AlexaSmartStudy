import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;


public class AudioNotesGUI extends JFrame implements ActionListener {

    private JButton chooseFileButton;
    private JButton runButton;
    private JLabel selectedFileLabel;
    private JLabel statusLabel;
    private JFileChooser fileChooser;
    private boolean isRunning;

    public AudioNotesGUI() {

        super("Audio Notes");
        // Create a button to choose a file
        chooseFileButton = new JButton("Choose Audio File");
        chooseFileButton.addActionListener(this);

        // Create a button to run the program
        runButton = new JButton("Run");
        runButton.addActionListener(this);
        runButton.setEnabled(false);

        // Create a label to display the selected file path
        selectedFileLabel = new JLabel("No file selected.");

        // Create a label to display the status
        statusLabel = new JLabel("Ready");

        // Add the buttons and labels to the frame
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(chooseFileButton, BorderLayout.NORTH);
        panel.add(selectedFileLabel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(runButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        // Create a file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Set the size and visibility of the frame
        setSize(400, 150);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == chooseFileButton) {
            // Show the file chooser dialog box
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedFileLabel.setText(selectedFile.getAbsolutePath());
                runButton.setEnabled(true);
            }
        } else if (e.getSource() == runButton) {
            if (!isRunning) {
                // Disable the buttons and display the loading indicator
                chooseFileButton.setEnabled(false);
                statusLabel.setText("Running...");
                isRunning = true;

                // Run the Python program to generate HTML notes
                String command = "C:/Users/Ari/AppData/Local/Programs/Python/Python311/python.exe summary_test.py " + selectedFileLabel.getText();
                try {
                    Process p = Runtime.getRuntime().exec(command);

                    // Create a thread to read the output of the Python program
                    Thread outputReader = new Thread(new OutputReader(p.getInputStream()));
                    outputReader.start();

                    // Create a thread to read the error output of the Python program
                    Thread errorReader = new Thread(new OutputReader(p.getErrorStream()));
                    errorReader.start();

                    // Wait for the Python program to finish
                    int exitValue = p.waitFor();

                    // Display the status message and open the generated HTML file
                    if (p.exitValue() == 0) {
                        statusLabel.setText("Done.");
                        File htmlFile = new File("output.html");

                        // Open the HTML file in a new Java window
                        try {
                            JEditorPane editorPane = new JEditorPane();
                            editorPane.setPage(htmlFile.toURI().toURL());
                            JScrollPane scrollPane = new JScrollPane(editorPane);
                            JFrame frame = new JFrame("Output HTML");
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
                            frame.setSize(800, 600);
                            frame.getContentPane().setBackground( new Color(26,26,26) );
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                        } catch (IOException ex) {
                            statusLabel.setText("Error.");
                            ex.printStackTrace();
                        }
                    } else {
                        statusLabel.setText("Error.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                // Enable the buttons and clear the loading indicator
                chooseFileButton.setEnabled(true);
                runButton.setText("Run");
            } else {
                // Cancel the Python program
                statusLabel.setText("Canceled.");
            }
            isRunning = false;
        }
    }

    /**
     * A class to read the output of a process in a separate thread.
     */
    class OutputReader implements Runnable {
        private InputStream inputStream;

        public OutputReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The main method to start the program.
     */
    public static void main(String[] args) {
        new AudioNotesGUI();
    }
}
