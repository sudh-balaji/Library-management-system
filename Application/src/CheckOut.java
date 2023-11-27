package libraryProject;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;

public class CheckOut extends JFrame {
	JFrame homePageFrame;
    JPanel homePagePanel;
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
        homePagePanel = new JPanel();
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
        homePagePanel.setLayout(new BorderLayout());
        homePagePanel.add(new JScrollPane(searchResultList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JLabel("Borrower Card No:"));
        bottomPanel.add(cardNo);
        bottomPanel.add(checkoutButton);

        homePagePanel.add(bottomPanel, BorderLayout.SOUTH);

        add(homePagePanel);
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
       
}
    
    

    