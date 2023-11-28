import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.*;

public class CheckInResultsHandler extends JFrame {
    JFrame checkInFrame;
    JPanel checkInControlPanel;
    Connection connection;
    JList<String> searchResultList;
    JButton checkInButton;

    CheckInResultsHandler(String isbn, String card, String borrower) throws SQLException {
       this.connection = connection;
       performCheckIn(isbn, card, borrower);
    }
    
    private void performCheckIn(String isbn, String card, String borrower) throws SQLException {
        // Validate the input and check in the book
        if (isbn.isEmpty() && card.isEmpty() && borrower.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Enter a value for any search criteria.");
        } else {
            // Use the provided search criteria to locate the book loans
            ArrayList<String> bookLoans = searchBookLoans(isbn, card, borrower);

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
    
    private ArrayList<String> searchBookLoans(String isbn, String card, String borrower) throws SQLException {
        ArrayList<String> bookLoans = new ArrayList<>();
        String query = "SELECT LoanId, ISBN, CardNo, BorrowerName FROM BOOK_LOANS " +
                "WHERE ISBN LIKE ? AND CardNo LIKE ? AND BorrowerName LIKE ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, "%" + isbn + "%");
            preparedStatement.setString(2, "%" + card + "%");
            preparedStatement.setString(3, "%" + borrower + "%");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String loanId = resultSet.getString("LoanId");
                    String loanInfo = String.format("Loan ID: %s, ISBN: %s, Card No: %s, Borrower: %s",
                            loanId, resultSet.getString("ISBN"), resultSet.getString("CardNo"), resultSet.getString("BorrowerName"));
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
        	String updateQuery = "UPDATE BOOK_LOANS SET DateIn = CURDATE() WHERE LoanId = ?";
        	try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
        		preparedStatement.setString(1, loanId);
        		preparedStatement.executeUpdate();
        	}
        }
    }
    
    private boolean isBookCheckedIn(String loanId) throws SQLException {
        String checkInStatusQuery = "SELECT DateIn FROM BOOK_LOANS WHERE LoanId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(checkInStatusQuery)) {
            preparedStatement.setString(1, loanId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // If DateIn is not null, the book is checked in
                    return resultSet.getDate("DateIn") != null;
                }
            }
        }
        return false;
    }
}
