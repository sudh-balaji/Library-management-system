/**
 * The BookSearch class represents the GUI for searching books in the Library Management System.
 * Users can search for books by entering the ISBN, Author name, or Title in the provided text field.
 * The class uses Java Swing for creating a graphical user interface and interacts with the underlying database.
 *
 * @createdBy: Alper Duru
 * @createdDate: 11/11/2023
 */

// Import libraries
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;

/**
 * The BookSearch class represents the user interface for searching books
 */
public class BookSearch extends JFrame
{
    // Class variables
    JFrame homePageFrame;
    JPanel homePagePanel;
    private Connection connection; //added connection to access database


    /**
     * Constructor for BookSearch class.
     *
     * @throws SQLException if a database access error occurs
     */
    BookSearch(Connection connection) throws SQLException {
        this.connection = connection;
        buildUserInterface();
    }


    /**
     * The main method to launch the BookSearch application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library",
                            "root", "Narutoget05?"); //add your password
                    new BookSearch(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Prepares the graphical user interface for book search.
     *
     * @throws SQLException if a database access error occurs
     */
    void buildUserInterface() throws SQLException
    {
        // Setting up the main frame
        homePageFrame = new JFrame("Library Management System");
        homePageFrame.setSize(1000, 500);
        homePageFrame.setLocation(20, 50);
        homePageFrame.setMinimumSize(homePageFrame.getSize());
        homePageFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Setting up the control panel
        homePagePanel = new JPanel();
        homePagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(homePagePanel);

        GridBagLayout homePagePanel_component = new GridBagLayout();
        homePagePanel_component.columnWidths = new int[] { 0, 0 };
        homePagePanel_component.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        homePagePanel_component.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
        homePagePanel_component.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
        homePagePanel.setLayout(homePagePanel_component);

        // Adding components to the control panel
        JLabel BookSearchLabel = new JLabel("Search book", JLabel.CENTER);
        BookSearchLabel.setFont(new Font("Calibri", Font.BOLD, 20));

        GridBagConstraints gbc_BookSearchLabel = new GridBagConstraints();
        gbc_BookSearchLabel.insets = new Insets(0, 0, 10, 0);
        gbc_BookSearchLabel.gridx = 0;
        gbc_BookSearchLabel.gridy = 1;
        gbc_BookSearchLabel.gridwidth = 2;
        homePagePanel.add(BookSearchLabel, gbc_BookSearchLabel);

        // Text Box
        JLabel textLabel = new JLabel("Enter the ISBN, Title or Author name", JLabel.LEFT);
        textLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));

        GridBagConstraints gbc_textLabel = new GridBagConstraints();
        gbc_textLabel.insets = new Insets(0, 0, 10, 0);
        gbc_textLabel.gridx = 0;
        gbc_textLabel.gridy = 2;
        gbc_textLabel.gridwidth = 2;
        homePagePanel.add(textLabel, gbc_textLabel);

        JLabel space1 = new JLabel("  ", JLabel.CENTER);
        GridBagConstraints gbc_space1 = new GridBagConstraints();
        gbc_space1.insets = new Insets(0, 0, 10, 0);
        gbc_space1.gridx = 0;
        gbc_space1.gridy = 3;
        gbc_space1.gridwidth = 2;
        homePagePanel.add(space1, gbc_space1);

        JTextField BookSearchText = new JTextField();

        BookSearchText.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        BookSearchText.setForeground(Color.black);

        GridBagConstraints gbc_BookSearchText = new GridBagConstraints();
        gbc_BookSearchText.fill = GridBagConstraints.HORIZONTAL;
        gbc_BookSearchText.insets = new Insets(0, 0, 10, 0);
        gbc_BookSearchText.gridx = 1;
        gbc_BookSearchText.gridy = 4;
        gbc_BookSearchText.gridwidth = 2;
        homePagePanel.add(BookSearchText, gbc_BookSearchText);
        BookSearchText.setColumns(20);

        JButton BookSearch = new JButton("Search...");
        BookSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            String searchText = BookSearchText.getText(); // Get the text from the search field
            //BookSearch QUERY
                try {
            String query = "SELECT BOOKS.Isbn, BOOKS.Title, GROUP_CONCAT(AUTHORS.Name) AS Authors, " +
                    "CASE WHEN BOOK_LOANS.Isbn IS NULL THEN 'Available' ELSE 'Checked Out' END AS Availability " +
                    "FROM BOOKS " +
                    "LEFT JOIN BOOK_AUTHORS ON BOOKS.Isbn = BOOK_AUTHORS.Isbn " +
                    "LEFT JOIN AUTHORS ON BOOK_AUTHORS.Author_id = AUTHORS.Author_id " +
                    "LEFT JOIN BOOK_LOANS ON BOOKS.Isbn = BOOK_LOANS.Isbn " +
                    "WHERE LOWER(BOOKS.Title) LIKE LOWER(?) OR LOWER(AUTHORS.Name) LIKE LOWER(?) OR BOOKS.Isbn = ? " +
                    "GROUP BY BOOKS.Isbn";

            PreparedStatement input = connection.prepareStatement(query); //safely handles input
            input.setString(1, "%" + searchText + "%");
                    input.setString(2, "%" + searchText + "%");
            try {
                long isbn = Long.parseLong(searchText);
                input.setLong(3, isbn);
            } catch (NumberFormatException ex) {
                input.setLong(3, -1); // Set an invalid ISBN if not numeric
            }

            ResultSet resultSet = input.executeQuery(); //sets input as result and display
            ArrayList<String[]> searchResults = new ArrayList<>(); //hold search result
            while (resultSet.next()) {
                String[] bookInfo = new String[4];
                bookInfo[0] = String.valueOf(resultSet.getLong("Isbn"));
                bookInfo[1] = resultSet.getString("Title");
                bookInfo[2] = resultSet.getString("Authors");
                bookInfo[3] = resultSet.getString("Availability");
                searchResults.add(bookInfo);
            }
            input.close();

            //Displays the search results
            //you can modify here to change format
            JTextArea searchResultArea = new JTextArea();
            for (String[] bookInfo : searchResults) {
                searchResultArea.append("ISBN: " + bookInfo[0] + "\n");
                searchResultArea.append("Title: " + bookInfo[1] + "\n");
                searchResultArea.append("Authors: " + bookInfo[2] + "\n");
                searchResultArea.append("Availability: " + bookInfo[3] + "\n\n");
            }

            //Show results window
            JFrame resultFrame = new JFrame("Search Results");
            resultFrame.add(new JScrollPane(searchResultArea));
            resultFrame.setSize(600, 400);
            resultFrame.setLocationRelativeTo(null);
            resultFrame.setVisible(true);

        } catch(SQLException ex) {
            ex.printStackTrace();
            }
        }
        });

        GridBagConstraints gbc_btnBookSearch = new GridBagConstraints();
        gbc_btnBookSearch.fill = GridBagConstraints.HORIZONTAL;
        gbc_btnBookSearch.insets = new Insets(0, 0, 10, 0);
        gbc_btnBookSearch.gridx = 1;
        gbc_btnBookSearch.gridy = 5;
        homePagePanel.add(BookSearch, gbc_btnBookSearch);

        JLabel space2 = new JLabel("  ", JLabel.CENTER);
        GridBagConstraints gbc_space2 = new GridBagConstraints();
        gbc_space2.insets = new Insets(0, 0, 10, 0);
        gbc_space2.gridx = 0;
        gbc_space2.gridy = 6;
        gbc_space2.gridwidth = 2;
        homePagePanel.add(space2, gbc_space2);

        // Go Back
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                homePageFrame.setVisible(false);
                new HomePage();
            }
        });

        GridBagConstraints gbc_backButton = new GridBagConstraints();
        gbc_backButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_backButton.insets = new Insets(0, 0, 10, 0);
        gbc_backButton.gridx = 1;
        gbc_backButton.gridy = 7;
        gbc_backButton.anchor = GridBagConstraints.PAGE_END;
        homePagePanel.add(backButton, gbc_backButton);

        // Adding components to the main frame
        homePageFrame.add(homePagePanel);
        homePageFrame.setVisible(true);
    }
}
