/**
 * The Fines class
 * @createdBy: Aileen Mata
 * @createdDate: 11/16/23
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Fines extends JFrame {
    private Connection connection;
    public Fines(Connection connection) {
        this.connection = connection; //get connection
        buildUserInterface();

    }
    public void calculateAndSetFines() {
        try {
            PreparedStatement checkExistingFines = connection.prepareStatement(
                    "SELECT Loan_id, fine_amt, paid FROM FINES WHERE Loan_id = ?"
            );
            PreparedStatement updateFine = connection.prepareStatement(
                    "UPDATE FINES SET fine_amt = ? WHERE Loan_id = ?"
            );
            PreparedStatement insertNewFine = connection.prepareStatement(
                    "INSERT INTO FINES (Loan_id, fine_amt) VALUES (?, ?)"
            );

            String selectBookLoansQuery = "SELECT Loan_id, Date_in, Due_date FROM BOOK_LOANS";

            Statement statement = connection.createStatement();
            ResultSet bookLoans = statement.executeQuery(selectBookLoansQuery);

            while (bookLoans.next()) {
                int loanId = bookLoans.getInt("Loan_id");
                Date dateIn = bookLoans.getDate("Date_in");
                Date dueDate = bookLoans.getDate("Due_date");

                checkExistingFines.setInt(1, loanId);
                ResultSet existingFines = checkExistingFines.executeQuery();

                if (existingFines.next()) {
                    double currentFine = existingFines.getDouble("fine_amt");
                    boolean isPaid = existingFines.getBoolean("paid");

                    if (!isPaid) {
                        double newFine = calculateFine(dateIn, dueDate);
                        if (newFine != currentFine) {
                            updateFine.setDouble(1, newFine);
                            updateFine.setInt(2, loanId);
                            updateFine.executeUpdate();
                        }
                    }
                } else {
                    double newFine = calculateFine(dateIn, dueDate);
                    insertNewFine.setInt(1, loanId);
                    insertNewFine.setDouble(2, newFine);
                    insertNewFine.executeUpdate();
                }
            }
            displayFines(false); // Display updated fines
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double calculateFine(Date dateIn, Date dueDate) {
        if (dateIn != null) {
            // Calculate fine for a book that has been returned
            long diff = dateIn.getTime() - dueDate.getTime(); // Calculate the difference in milliseconds
            double fineAmount = Math.max(0, (double) diff / (1000 * 60 * 60 * 24) * 0.25);

            return fineAmount;
        } else {
            // Calculate fine for a book that is still out
            long todayDiff = Calendar.getInstance().getTime().getTime() - dueDate.getTime(); // Calculate difference till today
            double fineAmount = Math.max(0, (double) todayDiff / (1000 * 60 * 60 * 24) * 0.25);

            return fineAmount;
        }
    }
    public void payFine(int loanId) throws SQLException { //Method to pay fines for returned books
        PreparedStatement checkReturned = connection.prepareStatement(
                "SELECT Date_in FROM BOOK_LOANS WHERE Loan_id = ?"
        );
        checkReturned.setInt(1, loanId);
        ResultSet returnedResult = checkReturned.executeQuery();

        if (returnedResult.next()) {
            Date dateIn = returnedResult.getDate("Date_in");
            if (dateIn != null) {
                // Book has been returned, allow fine payment
                try (Statement statement = connection.createStatement()) {
                    String payFineQuery = "UPDATE FINES SET paid = TRUE WHERE Loan_id = " + loanId + " AND fine_amt > 0"; // Ensuring fine_amt is greater than 0
                    statement.executeUpdate(payFineQuery);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Book associated with the fine is not yet returned. Fine cannot be paid.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Loan ID not found.");
        }
    }

    public String createDisplayFinesQuery(String filterCondition) { //Method to create SQL query for displaying fines

        // Gets the SQL data from BORROWER, BOOK_LOANS, and FINES
        return "SELECT B.Card_id, B.Bname, SUM(F.fine_amt) AS Total_Fine_Amount " +
                "FROM BORROWER B " +
                "LEFT JOIN BOOK_LOANS BL ON B.Card_id = BL.Card_id " +
                "LEFT JOIN FINES F ON BL.Loan_id = F.Loan_id " +
                "WHERE 1=1 " + filterCondition +
                "GROUP BY B.Card_id HAVING Total_Fine_Amount > 0";
    }
    //Method to display fines based on filter condition
    public void displayFines(boolean showUnpaidFines) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String filterCondition = showUnpaidFines ? "AND F.paid = FALSE " : "";
            String displayFinesQuery = createDisplayFinesQuery(filterCondition);

            ResultSet resultSet = statement.executeQuery(displayFinesQuery);
            Map<Integer, Double> finesPerBorrower = new HashMap<>(); // Map to store total fines per borrower

            while (resultSet.next()) {
                int cardId = resultSet.getInt("Card_id");
                double totalFineAmount = resultSet.getDouble("Total_Fine_Amount");

                if (!finesPerBorrower.containsKey(cardId)) {
                    finesPerBorrower.put(cardId, totalFineAmount);
                } else {
                    finesPerBorrower.put(cardId, finesPerBorrower.get(cardId) + totalFineAmount);
                }
            }
            for (Map.Entry<Integer, Double> entry : finesPerBorrower.entrySet()) {
                int cardId = entry.getKey();
                double totalFineAmount = entry.getValue();

                System.out.println("Card ID: " + cardId + ", Total Fine Amount: $" + totalFineAmount);
            }
        }
    }
    private void buildUserInterface() {
        JFrame finesFrame = new JFrame("Fines");
        finesFrame.setSize(800, 600);
        finesFrame.setLocationRelativeTo(null);
        finesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel homePagePanel = new JPanel(new BorderLayout());
        homePagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel searchLabel = new JLabel("Fines Search", JLabel.CENTER);
        searchLabel.setFont(new Font("Calibri", Font.BOLD, 20));
        homePagePanel.add(searchLabel, BorderLayout.NORTH);

        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        JTextArea searchResultsArea = new JTextArea();

        JPanel searchPanel = new JPanel(new GridLayout(1, 2));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        homePagePanel.add(searchPanel, BorderLayout.CENTER);

        homePagePanel.add(new JScrollPane(searchResultsArea), BorderLayout.CENTER);

        //JButton calculateButton = new JButton("Calculate Fines");
        JButton payFineButton = new JButton("Pay Fine");
       // JButton displayButton = new JButton("Display Fines");
        JButton updateFinesButton = new JButton("Update All Current Fines");
        JButton displayAllFinesButton = new JButton("Display All Fines");
        //JButton startUpdatesButton = new JButton("Start Daily Updates");


        // Search Button ActionListener
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText();
            String searchQuery = "SELECT B.Card_id, B.Bname, SUM(F.fine_amt) AS Total_Fine_Amount " +
                    "FROM BORROWER B " +
                    "LEFT JOIN BOOK_LOANS BL ON B.Card_id = BL.Card_id " +
                    "LEFT JOIN FINES F ON BL.Loan_id = F.Loan_id " +
                    "WHERE B.Card_id = '" + searchText + "'" +
                    "GROUP BY B.Card_id";

            try (Statement statement = connection.createStatement()) {
                ResultSet searchResult = statement.executeQuery(searchQuery);
                boolean found = false;

                while (searchResult.next()) {
                    found = true;
                    int cardId = searchResult.getInt("Card_id");
                    String borrowerName = searchResult.getString("Bname");
                    double totalFineAmount = searchResult.getDouble("Total_Fine_Amount");

                    searchResultsArea.setText("Card ID: " + cardId + ", Borrower Name: " + borrowerName + ", Total Fine Amount: $" + totalFineAmount + "\n");

                    if (totalFineAmount > 0) {
                        payFineButton.addActionListener(payEvent -> {
                            try {
                                payFine(cardId);
                            } catch (SQLException ex) {
                                handleException(ex, "Error paying fine");
                            }
                        });
                        searchResultsArea.add(payFineButton);
                    } else {
                        searchResultsArea.add(new JLabel("No pending fines."));
                    }
                }

                if (!found) {
                    searchResultsArea.setText("No results found for Card ID: " + searchText);
                }
            } catch (SQLException ex) {
                handleException(ex, "Error executing search query");
            }
        });

// Display All Fines Button ActionListener
        JCheckBox filterPaidFinesCheckBox = new JCheckBox("Filter Paid Fines");
        // Display All Fines Button ActionListener
        displayAllFinesButton.addActionListener(e -> {
            boolean showPaidFines = filterPaidFinesCheckBox.isSelected();
            try {
                displayFines(showPaidFines); // Show all fines based on the checkbox value
            } catch (SQLException ex) {
                handleException(ex, "Error displaying fines");
            }
        });

        updateFinesButton.addActionListener(e -> {
            try {
                calculateAndSetFines(); // Trigger fines update immediately upon button click
                displayFines(false); // Display updated fines
            } catch (SQLException ex) {
                handleException(ex, "Error updating fines");
            }
        });

        // Go Back Button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homePagePanel.setVisible(false);
                new HomePage();
            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        buttonPanel.add(searchButton);
        buttonPanel.add(updateFinesButton);
        buttonPanel.add(backButton);

        homePagePanel.add(buttonPanel, BorderLayout.SOUTH);

        finesFrame.add(homePagePanel);
        finesFrame.setVisible(true);
    }

    private void handleException(Exception e, String message) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, message);
    }

    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "ADDURPASS"; //add your password here

            Connection connection = DriverManager.getConnection(url, user, password);
            Fines finesManager = new Fines(connection);

            finesManager.calculateAndSetFines(); //get the calculates fines
            finesManager.displayFines(false); //display the fines

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
