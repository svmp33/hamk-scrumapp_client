
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the capitalization server. It has a main
 * frame window with a text field for entering strings and a textarea to see the
 * results of capitalizing them.
 */
public class ScrumApp_Client {

    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Temperature Monitor v0.02c");
    private JTextField dataField = new JTextField(60);
    private JTextArea messageArea = new JTextArea(9, 1);
    private JTextArea imageArea = new JTextArea();
    private static BufferedImage image;

    // Method for loading the image
    private static void loadImage() throws IOException {
        image = ImageIO.read(
                ScrumApp_Client.class.getResource("/res/floorplan.jpg"));
    }

    public ScrumApp_Client() throws IOException {

        // Go to image loading method
        loadImage();

        // Build up GUI
        messageArea.setEditable(false);
        imageArea.setEditable(false);
        final JLabel imageLabel = new JLabel(new ImageIcon(image));
        final JScrollPane menuImage = new JScrollPane(imageLabel);

        frame.getContentPane().add(new JScrollPane(menuImage), "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        // frame.getContentPane().add(messageArea, "Center");

        frame.getContentPane().add(dataField, "South");
        messageArea.setFont(new java.awt.Font("Sans-Serif", 0, 18));

        // Add Listeners
        dataField.addActionListener(new ActionListener() {
            /**
             * Responds to pressing the enter key in the textfield by sending
             * the contents of the text field to the server and displaying the
             * response from the server in the text area. If the response is
             * "quit" we exit the whole application, which closes all sockets,
             * streams and windows.
             */

            public void actionPerformed(ActionEvent e) {

                out.println(dataField.getText());
                String response = "";
                try {
                    response = in.readLine();
                    // do {

                    if ("quit".equals(response)) {
                        System.exit(0);
                    }

                    //   } while (!"".equals(response));
                    // } while (!response.equals(""));
                } catch (IOException ex) {
                    Logger.getLogger(ScrumApp_Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                messageArea.setText("");
                //messageArea.selectAll();
                //messageArea.append("\n");
                messageArea.append(response + "\n");
                dataField.selectAll();
            }

        });
    }

    /**
     * Implements the connection logic by prompting the end user for the
     * server's IP address, connecting, setting up streams, and consuming the
     * welcome messages from the server. The Capitalizer protocol says that the
     * server sends three lines of text to the client immediately after
     * establishing a connection.
     */
    public void connectToServer() throws IOException {

        // Server address input box
        String serverAddress = JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Temperature Monitor v0.02c",
                JOptionPane.QUESTION_MESSAGE);

        // Make connection and initialize streams
        Socket socket = new Socket(serverAddress, 9898);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        // Username input box
        String userInput = JOptionPane.showInputDialog(
                frame,
                "Please enter username:",
                "Temperature Monitor v0.02c",
                JOptionPane.QUESTION_MESSAGE);
        
        // Password input box
        JPasswordField pwd = new JPasswordField(10);
        int result = JOptionPane.showConfirmDialog(
                frame, 
                pwd, 
                "Please enter password:", 
                JOptionPane.OK_CANCEL_OPTION);
        String passInput = pwd.getText();

        // Pass user information to the server
        out.println(userInput);
        out.println(passInput);

        // Fetch response message from server
        String welcomeMsg = in.readLine();

        // If user/pass doesnt match the ones in the server database,
        // the client will quit.
        if ("quit".equals(welcomeMsg)) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Wrong User or Password!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);

        }
        // If we have successful login, show GUI
        frame.setVisible(true);
        
        
        // Print welcome message from server
        for (int i = 0; i < 8; i++) {
            welcomeMsg = in.readLine();
            messageArea.append(welcomeMsg + "\n");

        }
    }

    /**
     * Runs the client application
     */
    public static void main(String[] args) throws Exception {

        ScrumApp_Client client = new ScrumApp_Client();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.pack();
        client.frame.setVisible(false);
        client.connectToServer();
    }
}
