/**
 * The HomePage class represents the main page of the Library Management System GUI.
 * It includes functionalities such as searching for a book, checking in and out, managing borrowers, and handling fines.
 * The class uses Java Swing for creating a graphical user interface.
 *
 * @createdBy: Alper Duru
 * @createdDate: 11/11/2023
 */

// Import libraries
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The HomePage class represents the main page of the Library Management System
 * GUI.
 */
public class HomePage extends JFrame {
    JFrame homePageFrame;
    JPanel homePagePanel;
    JLabel homePageHeader;
    private MyJDBC myJDBC; // added MyJDBC for database connection

    /**
     * Constructor for HomePage class.
     */
    HomePage() {
        myJDBC = new MyJDBC(); // Create an instance of MyJDBC

        buildUserInterface();
    }

    /**
     * The main method to launch the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        new HomePage();
    }

    /**
     * Prepares the graphical user interface.
     */
    void buildUserInterface() {
        // Frame
        homePageFrame = new JFrame("Library Management System");
        homePageFrame.setSize(1000, 300);
        homePageFrame.setLocation(20, 50);
        homePageFrame.setLayout(new GridLayout(2, 1));
        homePageFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel
        homePagePanel = new JPanel();
        homePagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(homePagePanel);

        GridBagLayout gbl_homePagePanel = new GridBagLayout();
        gbl_homePagePanel.columnWidths = new int[] { 0, 0 };
        gbl_homePagePanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gbl_homePagePanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_homePagePanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
                Double.MIN_VALUE };
        homePagePanel.setLayout(gbl_homePagePanel);

        // Heading
        homePageHeader = new JLabel("Library Management System", JLabel.CENTER);
        homePageHeader.setFont(new Font("Calibri", Font.BOLD, 24));
        homePageHeader.setForeground(Color.black);
        homePageHeader.setBackground(Color.yellow);

        // Search Button
        // --------------------------------------------------------------------
        JButton search = new JButton("Search");
        search.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homePageFrame.setVisible(false);
                Connection connection = myJDBC.getConnection(); // get MySQL connection
                try {
                    BookSearch bookSearch = new BookSearch(connection); // Pass the connection
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        GridBagConstraints gbc_btnSearch = new GridBagConstraints();
        gbc_btnSearch.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnSearch.insets = new Insets(0, 0, 10, 0);
        gbc_btnSearch.gridx = 0;
        gbc_btnSearch.gridy = 0;
        gbc_btnSearch.weightx = 0.5;
        // gbc_btnSearch.gridwidth = 2;
        homePagePanel.add(search, gbc_btnSearch);

        // Check Out
        // --------------------------------------------------------------------
        JButton checkOut = new JButton("Check Out");
        checkOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homePageFrame.setVisible(false);
                Connection connection = myJDBC.getConnection(); // Get the connection from MyJDBC
                try {
                	CheckOut checkOut = new CheckOut(connection);
                	checkOut.setVisible(true);            	
                } catch (SQLException ex){
                	ex.printStackTrace(); // Handle SQL exception
                }
            }
        });

        GridBagConstraints gbc_btnCheckOut = new GridBagConstraints();
        gbc_btnCheckOut.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnCheckOut.insets = new Insets(0, 0, 10, 0);
        gbc_btnCheckOut.gridx = 1;
        gbc_btnCheckOut.gridy = 0;
        gbc_btnCheckOut.weightx = 0.5;
        // gbc_btnCheckOut.gridwidth = 2;
        homePagePanel.add(checkOut, gbc_btnCheckOut);

        // Check In
        // --------------------------------------------------------------------
        JButton checkIn = new JButton("Check In");
        checkIn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homePageFrame.setVisible(false);
                // call your function here
                new CheckInHandler();
            }
        });

        GridBagConstraints gbc_btnCheckIn = new GridBagConstraints();
        gbc_btnCheckIn.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnCheckIn.insets = new Insets(0, 0, 10, 0);
        gbc_btnCheckIn.gridx = 2;
        gbc_btnCheckIn.gridy = 0;
        gbc_btnCheckIn.weightx = 0.5;
        // gbc_btnCheckIn.gridwidth = 2;
        homePagePanel.add(checkIn, gbc_btnCheckIn);

        // Add Borrower
        // --------------------------------------------------------------------
        JButton newBorrower = new JButton("New Borrower");
        newBorrower.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homePageFrame.setVisible(false);
                Connection connection = myJDBC.getConnection();
                try {
                    BorrowerManagement borrowerManagement = new BorrowerManagement(connection);
                } catch (SQLException e1){
                    e1.printStackTrace();
                }
                // call your function here
            }
        });

        GridBagConstraints gbc_btnNewBorrower = new GridBagConstraints();
        gbc_btnNewBorrower.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnNewBorrower.insets = new Insets(0, 0, 10, 0);
        gbc_btnNewBorrower.gridx = 3;
        gbc_btnNewBorrower.gridy = 0;
        gbc_btnNewBorrower.weightx = 0.5;
        // gbc_btnNewBorrower.gridwidth = 2;
        homePagePanel.add(newBorrower, gbc_btnNewBorrower);

        // Handle Fines
        // --------------------------------------------------------------------
        JButton fines = new JButton("Fines");
        fines.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homePageFrame.setVisible(false);
                Connection connection = myJDBC.getConnection(); // Get the connection from MyJDBC

                Fines finesManager = new Fines(connection);

                try { // show unpaid fines
                    finesManager.displayFines(false);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        GridBagConstraints gbc_btnFines = new GridBagConstraints();
        gbc_btnFines.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnFines.insets = new Insets(0, 0, 10, 0);
        gbc_btnFines.gridx = 4;
        gbc_btnFines.gridy = 0;
        gbc_btnFines.weightx = 0.5;
        // gbc_btnFines.gridwidth = 2;
        homePagePanel.add(fines, gbc_btnFines);

        // JLabel
        JLabel space = new JLabel(" ");
        GridBagConstraints gbc_lblSpace = new GridBagConstraints();
        gbc_lblSpace.fill = GridBagConstraints.HORIZONTAL;
        gbc_lblSpace.insets = new Insets(0, 0, 10, 0);
        gbc_lblSpace.gridx = 5;
        gbc_lblSpace.gridy = 0;
        homePagePanel.add(space, gbc_lblSpace);

        homePageFrame.add(homePageHeader);
        homePageFrame.add(homePagePanel);
        homePageFrame.setVisible(true);
    }

}
