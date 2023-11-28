import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;

public class CheckOut extends JFrame {
	JFrame homePageFrame;
    JPanel checkOutPagePanel;
    JList<String> searchResultList;
    JButton checkoutButton;
    JTextField cardNo;
    private Connection connection; //added connection to access database
    private HomePage homePage;

    /**
     * Constructor for CheckOut class.
     *
     * @throws SQLException if a database access error occurs
     */
    public CheckOut(Connection connection) throws SQLException {
    	buildUserInterface();
        this.connection = connection;
        this.homePage = homePage;
        setTitle("Check Out");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
  /*
    private Calendar calcCheckoutDate() {
    	Calendar date_out = Calendar.getInstance();
    	return date_out;
    }
    
    private Calendar calcDueDate() {
    	Calendar due_date = Calendar.getInstance();
        due_date.setTime(due_date.getTime());
        due_date.add(Calendar.DAY_OF_YEAR, 14);
    	return due_date;
    } 
    */
    
    private void initComponents() {
        checkOutPagePanel = new JPanel();
        searchResultList = new JList<>();
        checkoutButton = new JButton("Check Out");
        cardNo = new JTextField(20);

        // Add action listener to the checkoutButton
        checkoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performCheckOut();
            }
        });
        // Layout setup
        checkOutPagePanel.setLayout(new BorderLayout());
        checkOutPagePanel.add(new JScrollPane(searchResultList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Borrower Card No:"));
        bottomPanel.add(cardNo);
        bottomPanel.add(checkoutButton);

        checkOutPagePanel.add(bottomPanel, BorderLayout.SOUTH);

        add(checkOutPagePanel);
    }
        
    private void performCheckOut() {
        String[] selectedBooks = searchResultList.getSelectedValuesList().toArray(new String[0]);
        String cardNumber = cardNo.getText().trim();

        // Validate input 
        if (selectedBooks.length == 0 || cardNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select books and enter borrower card number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate the number of active BOOK_LOANS for the borrower
        if (!validateActiveBookLoans(cardNumber)) {
            JOptionPane.showMessageDialog(this, "Borrower already has 3 active book loans.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Perform checkout for each selected book
        for (String bookId : selectedBooks) {
        	boolean checkoutSuccess = checkOutBook(bookId, cardNumber);

            if (checkoutSuccess) {
                JOptionPane.showMessageDialog(this, "Checkout successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Checkout failed. Please check the inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean validateActiveBookLoans(String cardNo) {
        // Check the number of active book loans for the borrower
        String countQuery = "SELECT COUNT(*) FROM BOOK_LOANS WHERE CardNo = ? AND DateIn IS NULL";

        try (PreparedStatement countStatement = connection.prepareStatement(countQuery)) {
            countStatement.setString(1, cardNo);
            ResultSet resultSet = countStatement.executeQuery();

            if (resultSet.next()) {
                int activeLoans = resultSet.getInt(1);
                return activeLoans < 3; // Maximum of 3 active book loans allowed
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    private boolean checkOutBook(String bookId, String cardNo) {
    	// Check if the book is already checked out
        if (isBookCheckedOut(bookId)) {
            JOptionPane.showMessageDialog(this, "Book is already checked out.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
    	// Do the checkout
    	String insertQuery = "INSERT INTO BOOK_LOANS (BookId, CardNo, DateOut, DueDate) VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 14 DAY))";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, bookId);
            preparedStatement.setString(2, cardNo);

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exception 
            return false;
        }
    }
    
    private boolean isBookCheckedOut(String bookId) {
        // Check if the book is already checked out
        String checkQuery = "SELECT COUNT(*) FROM BOOK_LOANS WHERE BookId = ? AND DateIn IS NULL";

        try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
            checkStatement.setString(1, bookId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
        
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library",
                            "root", "AddUrPassWord"); // add your password here
                    new CheckOut(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    void buildUserInterface()
    {
	    // Home Page Frame
	    homePageFrame = new JFrame("Library Management System");
	    homePageFrame.setSize(1000, 500);
	    homePageFrame.setLocation(20, 50);
	    homePageFrame.setLayout(new GridLayout(2, 1));
	    homePageFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	
	    // Panel for visibility
	    checkOutPagePanel = new JPanel();
	    checkOutPagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
	    setContentPane(checkOutPagePanel);
	
	    // Create Layout: Primary Layout for Check-In page
	    GridBagLayout checkOutPagePanel_layout = new GridBagLayout();
	    checkOutPagePanel_layout.columnWidths = new int[] { 0, 0 };
	    checkOutPagePanel_layout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
	    checkOutPagePanel_layout.columnWeights = new double[] { Double.MIN_VALUE, Double.MIN_VALUE };
	    checkOutPagePanel_layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	    checkOutPagePanel.setLayout(checkOutPagePanel_layout);
	
	    // Add space for design
	    JLabel labelBlank = new JLabel("  ", JLabel.CENTER);
	
	    GridBagConstraints addDistance = new GridBagConstraints();
	    addDistance.insets = new Insets(0, 0, 5, 0);
	    addDistance.gridx = 0;
	    addDistance.gridy = 1;
	    addDistance.gridwidth = 2;
	    checkOutPagePanel.add(labelBlank, addDistance);
	    
	    // ISBN's of Books, allow user to enter a value for a search later
        JLabel ISBN_Label = new JLabel("ISBN ->                  ", JLabel.LEFT);
        ISBN_Label.setFont(new Font("Times New Roman", Font.BOLD, 14));
        GridBagConstraints ISBN_label_constraint = new GridBagConstraints();
        ISBN_label_constraint.insets = new Insets(0, 0, 5, 0);
        ISBN_label_constraint.gridx = 0;
        ISBN_label_constraint.gridy = 2;
        checkOutPagePanel.add(ISBN_Label, ISBN_label_constraint);

        // Input field for ISBN
        JTextField ISBN_Input_Value = new JTextField();
        ISBN_Input_Value.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        ISBN_Input_Value.setForeground(Color.black);

        GridBagConstraints ISBN_Input_Value_constraint = new GridBagConstraints();
        ISBN_Input_Value_constraint.fill = GridBagConstraints.HORIZONTAL;
        ISBN_Input_Value_constraint.insets = new Insets(0, 0, 5, 0);
        ISBN_Input_Value_constraint.gridx = 1;
        ISBN_Input_Value_constraint.gridy = 2;
        checkOutPagePanel.add(ISBN_Input_Value, ISBN_Input_Value_constraint);
        ISBN_Input_Value.setColumns(15);
        
     // -------------------------------------------------------------------------------------
        // Search field using Borrower's Card Number
        JLabel Card_Input_Label = new JLabel("Card No. ->           ", JLabel.RIGHT);
        Card_Input_Label.setFont(new Font("Times New Roman", Font.BOLD, 14));
        GridBagConstraints Card_Input_constraint = new GridBagConstraints();
        Card_Input_constraint.insets = new Insets(0, 0, 5, 0);
        Card_Input_constraint.gridx = 0;
        Card_Input_constraint.gridy = 3;
        checkOutPagePanel.add(Card_Input_Label, Card_Input_constraint);

        // Text input field for borrower's card number
        JTextField Card_Input_Value = new JTextField();
        Card_Input_Value.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        Card_Input_Value.setForeground(Color.black);

        GridBagConstraints Card_Input_Value_Constraint = new GridBagConstraints();
        Card_Input_Value_Constraint.fill = GridBagConstraints.HORIZONTAL;
        Card_Input_Value_Constraint.insets = new Insets(0, 0, 5, 0);
        Card_Input_Value_Constraint.gridx = 1;
        Card_Input_Value_Constraint.gridy = 3;
        checkOutPagePanel.add(Card_Input_Value, Card_Input_Value_Constraint);
        Card_Input_Value.setColumns(15);

        // -------------------------------------------------------------------------------------
        // Check In Button and its functionality
        JButton Check_Out_Button = new JButton("Check Out");

        Check_Out_Button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Validate input fields and handle check-in
                if (ISBN_Input_Value.getText().equals("") && 
                    Card_Input_Value.getText().equals("")){
                    JOptionPane.showMessageDialog(null, "Enter a value for ISBN and Card Number");
                }
               
            }
       
        });
        GridBagConstraints Check_In_Button_Constraint = new GridBagConstraints();
        Check_In_Button_Constraint.fill = GridBagConstraints.HORIZONTAL;
        Check_In_Button_Constraint.insets = new Insets(0, 0, 5, 0);
        Check_In_Button_Constraint.gridx = 0;
        Check_In_Button_Constraint.gridy = 5;
        Check_In_Button_Constraint.gridwidth = 2;
        checkOutPagePanel.add(Check_Out_Button, Check_In_Button_Constraint);

        JLabel additional_space = new JLabel("  ", JLabel.CENTER);

        GridBagConstraints additional_space_constraint = new GridBagConstraints();
        additional_space_constraint.insets = new Insets(0, 0, 5, 0);
        additional_space_constraint.gridx = 0;
        additional_space_constraint.gridy = 6;
        additional_space_constraint.gridwidth = 2;
        checkOutPagePanel.add(additional_space, additional_space_constraint);

        // -------------------------------------------------------------------------------------
        // Close Button
        JButton close = new JButton("Back to Home Page");
        close.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Close the current frame and return to the home page
                homePageFrame.setVisible(false);
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
        checkOutPagePanel.add(close, Home_Page_Return_Button_Constraint);

        // Set up the layout and make the frame visible
        homePageFrame.add(checkOutPagePanel);
        homePageFrame.setVisible(true);
    }
    
}
    
    

