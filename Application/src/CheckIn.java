
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.*;

public class CheckIn extends JFrame {
    JFrame checkInFrame;
    JPanel checkInPagePanel;
    private Connection connection;
    JList<String> searchResultList;
    JButton checkInButton;
    private HomePage homePage;

    public CheckIn(Connection connection) throws SQLException {
    	buildUserInterface();
        this.connection = connection;
        this.homePage = homePage;
        setTitle("Check In");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void performCheckIn(String isbn, String card) throws SQLException {
        // Validate the input and check in the book
        if (isbn.isEmpty() && card.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter a value for any search criteria.");
        } else {
            // Use the provided search criteria to locate the book loans
            ArrayList<String> bookLoans = searchBookLoans(isbn, card);

            if (bookLoans.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No matching book loans found.");
            } else {
                // Display a dialog to select one of the book loans
                Object selectedLoan = JOptionPane.showInputDialog(null,
                        "Select a book loan to check in:", "Book Loan Selection",
                        JOptionPane.QUESTION_MESSAGE, null, bookLoans.toArray(), bookLoans.get(0));

                if (selectedLoan != null) {
                    // Perform the check-in for the selected book loan
                    checkInBook(selectedLoan.toString());
                    JOptionPane.showMessageDialog(null, "Book checked in successfully!");
                }
            }
        }
    }
    
    private ArrayList<String> searchBookLoans(String isbn, String card) throws SQLException {
        ArrayList<String> bookLoans = new ArrayList<>();
        String query = "SELECT Loan_id, Isbn, Card_id FROM BOOK_LOANS " +
                "WHERE Isbn LIKE ? AND Card_id LIKE ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + isbn + "%");
            preparedStatement.setString(2, "%" + card + "%");
            

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String loanId = resultSet.getString("Loan_id");
                    String loanInfo = String.format("Loan ID: %s, ISBN: %s, Card No: %s",
                            loanId, resultSet.getString("ISBN"), resultSet.getString("Card_id"));
                    bookLoans.add(loanInfo);
                }
            }
        }

        return bookLoans;
    }
    
    private void checkInBook(String selectedLoan) throws SQLException {
        String loanId = selectedLoan.split(",")[0].replace("Loan ID: ", "").trim();
        
        // Check if the book is already checked in
        if (isBookCheckedIn(loanId)) {
            JOptionPane.showMessageDialog(null, "This book loan is already checked in.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
        	// Update the corresponding BOOK_LOANS tuple with today's date as date_in
        	String updateQuery = "UPDATE BOOK_LOANS SET Date_in = CURDATE() WHERE Loan_Id = ?";
        	try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
        		preparedStatement.setString(1, loanId);
        		preparedStatement.executeUpdate();
        	}
        }
    }
    
    private boolean isBookCheckedIn(String loanId) throws SQLException {
        String checkInStatusQuery = "SELECT Date_in FROM BOOK_LOANS WHERE Loan_Id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkInStatusQuery)) {
            preparedStatement.setString(1, loanId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // If DateIn is not null, the book is checked in
                    return resultSet.getDate("Date_in") != null;
                }
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library",
                            "root", "AddUrPassword"); // add your password here
                    CheckIn checkInHandler = new CheckIn(connection);
                    //CheckInResultsHandler checkInResults = new CheckInResultsHandler();
                    //bookOut.performCheckOut();
                    //connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void buildUserInterface()
    {
        // Home Page Frame
        checkInFrame = new JFrame("Library Management System");
        checkInFrame.setSize(1000, 500);
        checkInFrame.setLocation(20, 50);
        checkInFrame.setLayout(new GridLayout(2, 1));
        checkInFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Panel for visibility
        checkInPagePanel = new JPanel();
        checkInPagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(checkInPagePanel);

        // Create Layout: Primary Layout for Check-In page
        GridBagLayout checkInPagePanel_layout = new GridBagLayout();
        checkInPagePanel_layout.columnWidths = new int[] { 0, 0 };
        checkInPagePanel_layout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
        checkInPagePanel_layout.columnWeights = new double[] { Double.MIN_VALUE, Double.MIN_VALUE };
        checkInPagePanel_layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        checkInPagePanel.setLayout(checkInPagePanel_layout);

        // Add space for design
        JLabel labelBlank = new JLabel("  ", JLabel.CENTER);

        GridBagConstraints addDistance = new GridBagConstraints();
        addDistance.insets = new Insets(0, 0, 5, 0);
        addDistance.gridx = 0;
        addDistance.gridy = 1;
        addDistance.gridwidth = 2;
        checkInPagePanel.add(labelBlank, addDistance);

        // -------------------------------------------------------------------------------------
        // ISBN's of Books, allow user to enter a value for a search later
        JLabel ISBN_Label = new JLabel("ISBN ->                  ", JLabel.LEFT);
        ISBN_Label.setFont(new Font("Times New Roman", Font.BOLD, 14));
        GridBagConstraints ISBN_label_constraint = new GridBagConstraints();
        ISBN_label_constraint.insets = new Insets(0, 0, 5, 0);
        ISBN_label_constraint.gridx = 0;
        ISBN_label_constraint.gridy = 2;
        checkInPagePanel.add(ISBN_Label, ISBN_label_constraint);

        // Input field for ISBN
        JTextField ISBN_Input_Value = new JTextField();
        ISBN_Input_Value.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        ISBN_Input_Value.setForeground(Color.black);

        GridBagConstraints ISBN_Input_Value_constraint = new GridBagConstraints();
        ISBN_Input_Value_constraint.fill = GridBagConstraints.HORIZONTAL;
        ISBN_Input_Value_constraint.insets = new Insets(0, 0, 5, 0);
        ISBN_Input_Value_constraint.gridx = 1;
        ISBN_Input_Value_constraint.gridy = 2;
        checkInPagePanel.add(ISBN_Input_Value, ISBN_Input_Value_constraint);
        ISBN_Input_Value.setColumns(15);

        // -------------------------------------------------------------------------------------
        // Search field using Borrower's Card Number
        JLabel Card_Input_Label = new JLabel("Card No. ->           ", JLabel.RIGHT);
        Card_Input_Label.setFont(new Font("Times New Roman", Font.BOLD, 14));
        GridBagConstraints Card_Input_constraint = new GridBagConstraints();
        Card_Input_constraint.insets = new Insets(0, 0, 5, 0);
        Card_Input_constraint.gridx = 0;
        Card_Input_constraint.gridy = 3;
        checkInPagePanel.add(Card_Input_Label, Card_Input_constraint);

        // Text input field for borrower's card number
        JTextField Card_Input_Value = new JTextField();
        Card_Input_Value.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        Card_Input_Value.setForeground(Color.black);

        GridBagConstraints Card_Input_Value_Constraint = new GridBagConstraints();
        Card_Input_Value_Constraint.fill = GridBagConstraints.HORIZONTAL;
        Card_Input_Value_Constraint.insets = new Insets(0, 0, 5, 0);
        Card_Input_Value_Constraint.gridx = 1;
        Card_Input_Value_Constraint.gridy = 3;
        checkInPagePanel.add(Card_Input_Value, Card_Input_Value_Constraint);
        Card_Input_Value.setColumns(15);


        // -------------------------------------------------------------------------------------
        // Check In Button and its functionality
        JButton Check_In_Button = new JButton("Check In");

        Check_In_Button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Validate input fields and handle check-in
                if (ISBN_Input_Value.getText().equals("") && 
                    Card_Input_Value.getText().equals(""))
                {
                    JOptionPane.showMessageDialog(null, "Enter a value for any text box");
                }
                else
                {
                    try
                    {
                    	performCheckIn(ISBN_Input_Value.getText(), Card_Input_Value.getText());
                    } catch (SQLException e1)
                    {
                    	JOptionPane.showMessageDialog(null, "Error performing check-in: " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            }
        });

        GridBagConstraints Check_In_Button_Constraint = new GridBagConstraints();
        Check_In_Button_Constraint.fill = GridBagConstraints.HORIZONTAL;
        Check_In_Button_Constraint.insets = new Insets(0, 0, 5, 0);
        Check_In_Button_Constraint.gridx = 0;
        Check_In_Button_Constraint.gridy = 5;
        Check_In_Button_Constraint.gridwidth = 2;
        checkInPagePanel.add(Check_In_Button, Check_In_Button_Constraint);

        JLabel additional_space = new JLabel("  ", JLabel.CENTER);

        GridBagConstraints additional_space_constraint = new GridBagConstraints();
        additional_space_constraint.insets = new Insets(0, 0, 5, 0);
        additional_space_constraint.gridx = 0;
        additional_space_constraint.gridy = 6;
        additional_space_constraint.gridwidth = 2;
        checkInPagePanel.add(additional_space, additional_space_constraint);

        // -------------------------------------------------------------------------------------
        // Close Button
        JButton close = new JButton("Back to Home Page");
        close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Close the current frame and return to the home page
                checkInFrame.setVisible(false);
                new HomePage();
            }
        });

        GridBagConstraints Home_Page_Return_Button_Constraint = new GridBagConstraints();
        Home_Page_Return_Button_Constraint.fill = GridBagConstraints.HORIZONTAL;
        Home_Page_Return_Button_Constraint.insets = new Insets(0, 0, 5, 0);
        Home_Page_Return_Button_Constraint.gridx = 0;
        Home_Page_Return_Button_Constraint.gridy = 7;
        Home_Page_Return_Button_Constraint.anchor = GridBagConstraints.PAGE_END;
        Home_Page_Return_Button_Constraint.gridwidth = 2;
        checkInPagePanel.add(close, Home_Page_Return_Button_Constraint);

        // Set up the layout and make the frame visible
        //checkInFrame.add(checkInPagePanel);
        //checkInFrame.setVisible(true);
    }
}

