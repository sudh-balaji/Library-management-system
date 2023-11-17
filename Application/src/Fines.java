/**
 * The Fines class calculates the fines based on certain schedules
 * @createdBy: Aileen Mata
 * @createdDate: 11/16/23
 */
import java.sql.*;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fines {
    private Connection connection;

    public Fines(Connection connection) {
        this.connection = connection; //get connection
    }

    public void calculateAndSetFines() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            //calculating fines query
            String calculateFinesQuery = "UPDATE BOOK_LOANS " +
                    "SET fine_amt = CASE " +
                    "WHEN Date_in IS NOT NULL THEN ROUND(DATEDIFF(Date_in, Due_date) * 0.25, 2) " +
                    "ELSE ROUND(DATEDIFF(CURRENT_DATE, Due_date) * 0.25, 2) " +
                    "END " +
                    "WHERE (Date_in IS NOT NULL AND Date_in > Due_date) OR (Date_in IS NULL AND CURRENT_DATE > Due_date)";

            statement.executeUpdate(calculateFinesQuery);
        }
    }

    public void payFine(int loanId) throws SQLException {
        try (Statement statement = connection.createStatement()) { //paying fines query
            String payFineQuery = "UPDATE FINES SET paid = TRUE WHERE Loan_id = " + loanId;

            statement.executeUpdate(payFineQuery);
        }
    }

    public String createDisplayFinesQuery(boolean filterPaidFines) {
        //if true it gets filter conditon to get unpaid fines
        //but if false it will get all fines
        String filterCondition = filterPaidFines ? "AND F.paid = FALSE " : "";
        //gets the sql data from BORROWER, BOOK_LOANS, and FINES
        return "SELECT B.Card_id, B.Bname, SUM(F.fine_amt) AS Total_Fine_Amount " +
                "FROM BORROWER B " +
                "LEFT JOIN BOOK_LOANS BL ON B.Card_id = BL.Card_id " +
                "LEFT JOIN FINES F ON BL.Loan_id = F.Loan_id " +
                "WHERE 1=1 " + filterCondition +
                "GROUP BY B.Card_id HAVING Total_Fine_Amount > 0";
    }

    public void displayFines(boolean filterPaidFines) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String displayFinesQuery = createDisplayFinesQuery(filterPaidFines);
            //gets fines and shows total from card_id and Bname
            ResultSet resultSet = statement.executeQuery(displayFinesQuery);
            while (resultSet.next()) {
                int cardId = resultSet.getInt("Card_id");
                String borrowerName = resultSet.getString("Bname");
                double totalFineAmount = resultSet.getDouble("Total_Fine_Amount");

                System.out.println("Card ID: " + cardId + ", Borrower Name: " + borrowerName + ", Total Fine Amount: $" + totalFineAmount);
            }
        }
    }

    public void startDailyUpdates() { //updates the fines in a table 
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        //sets a schedule time to recalculate and update the fines
        try {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    calculateAndSetFines();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, calculatesFinesDelay(), 24, TimeUnit.HOURS);
        } catch (RejectedExecutionException ex) {
            ex.printStackTrace();
        }
    }

    private long calculatesFinesDelay() { //calculates fines based on library hours (9am-5pm)
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

    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost:3306/library";
            String user = "root";
            String password = "Narutoget05?"; //add your password here

            Connection connection = DriverManager.getConnection(url, user, password);
            Fines finesManager = new Fines(connection);

            finesManager.calculateAndSetFines(); //get the calculates fines
            finesManager.displayFines(false); //display the fines

            finesManager.payFine(123); // Example of paying a fine for loan ID 123

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}



