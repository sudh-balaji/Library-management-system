/**
 * The Fines class calculates the fines based on certain schedules
 * @createdBy: Aileen Mata
 * @createdDate: 11/16/23
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fines extends JFrame {
    private Connection connection;
    public Fines(Connection connection) {
        this.connection = connection; //get connection
        buildUserInterface();

    }
    public void calculateAndSetFines() { //Method to calculate fines for late books
        if (isLibraryClosed()) {
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
        } else {
            JOptionPane.showMessageDialog(null, "Library is open. Fines cannot be updated.");
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
                    String payFineQuery = "UPDATE FINES SET paid = TRUE WHERE Loan_id = " + loanId;
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
            while (resultSet.next()) {
                int cardId = resultSet.getInt("Card_id");
                String borrowerName = resultSet.getString("Bname");
                double totalFineAmount = resultSet.getDouble("Total_Fine_Amount");

                System.out.println("Card ID: " + cardId + ", Borrower Name: " + borrowerName + ", Total Fine Amount: $" + totalFineAmount);
            }
        }
    }
    public void startDailyUpdates() {  //Scheduling daily updates for fine calculations
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // Sets a schedule time to recalculate and update the fines
        try {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    calculateAndSetFines();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, calculatesFinesDelay(), 24, TimeUnit.HOURS);
        } catch (RejectedExecutionException ex) {
            ex.printStackTrace();
        }
    }
    private long calculatesFinesDelay() { //calculates fines based on library closed hours (9am-5pm)
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        //this ensures that the schedule runs after it closes and not charge during open hours.
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long libraryClose = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 9);
        long libraryOpen = calendar.getTimeInMillis();

        if (now >= libraryClose) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return libraryOpen - now;
        } else if (now < libraryOpen) {
            return libraryOpen - now;
        } else {
            return libraryClose - now;
        }
    }
    private boolean isLibraryClosed() {  //Check if the library is closed

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 9 || hour >= 17; // Library closed from 9 am to 5 pm (24-hour format)
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

        JCheckBox filterPaidFinesCheckBox = new JCheckBox("Filter Paid Fines");
        filterPaidFinesCheckBox.addActionListener(e -> {
            boolean showPaidFines = filterPaidFinesCheckBox.isSelected();
            try {
                displayFines(!showPaidFines); // Pass the opposite of the checkbox value
            } catch (SQLException ex) {
                handleException(ex, "Error displaying fines");
            }
        });

        JButton calculateButton = new JButton("Calculate Fines");
        JButton payFineButton = new JButton("Pay Fine");
        JButton displayButton = new JButton("Display Fines");
        //JButton startUpdatesButton = new JButton("Start Daily Updates");
        JButton updateFinesButton = new JButton("Update Current Fines");

        updateFinesButton.addActionListener(e -> {
            if (isLibraryClosed()) {
                try {
                    calculateAndSetFines();
                    displayFines(false); // Display updated fines
                } catch (SQLException ex) {
                    handleException(ex, "Error updating fines");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Library is open. Fines cannot be updated.");
            }
        });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        buttonPanel.add(calculateButton);
        buttonPanel.add(payFineButton);
        buttonPanel.add(displayButton);
       // buttonPanel.add(startUpdatesButton);
        buttonPanel.add(updateFinesButton);

        homePagePanel.add(buttonPanel, BorderLayout.SOUTH);

        calculateButton.addActionListener(e -> {
            calculateAndSetFines();
        });

        payFineButton.addActionListener(e -> {
            try {
                payFine(123); // Placeholder loan ID
            } catch (SQLException ex) {
                handleException(ex, "Error paying fine");
            }
        });
        displayButton.addActionListener(e -> {
            try {
                displayFines(false); // Placeholder parameter
            } catch (SQLException ex) {
                handleException(ex, "Error displaying fines");
            }
        });
        /*startUpdatesButton.addActionListener(e -> {
            startDailyUpdates();
        });*/
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
            String password = "Narutoget05?"; //add your password here

            Connection connection = DriverManager.getConnection(url, user, password);
            Fines finesManager = new Fines(connection);

            finesManager.calculateAndSetFines(); //get the calculates fines
            finesManager.displayFines(false); //display the fines

            //finesManager.payFine(123); // Example of paying a fine for loan ID 123

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
}
