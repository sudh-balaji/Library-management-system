import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class BorrowerManagement extends JFrame {
    JFrame homePageFrame;
    JPanel homePagePanel;
    private Connection connection;

    BorrowerManagement(Connection connection) throws SQLException {
        this.connection = connection;
        buildUserInterface();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library",
                            "root", "placeholder");
                    new BookSearch(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int generateNewCardId() {
        int maxCardId = 0;
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT MAX(Card_id) FROM Borrower");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                maxCardId = rs.getInt(1);
            }
            pstmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return maxCardId + 1;
    }

    void buildUserInterface() throws SQLException {
        homePageFrame = new JFrame("Library Management System");
        homePageFrame.setSize(1000, 500);
        homePageFrame.setLocation(20, 50);
        homePageFrame.setMinimumSize(homePageFrame.getSize());
        homePageFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

        homePagePanel = new JPanel();
        homePagePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(homePagePanel);

        GridBagLayout homePagePanel_component = new GridBagLayout();
        homePagePanel_component.columnWidths = new int[]{0, 0};
        homePagePanel_component.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        homePagePanel_component.columnWeights = new double[]{0.0, Double.MIN_VALUE};
        homePagePanel_component.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        homePagePanel.setLayout(homePagePanel_component);

        // Adding components to the control panel
        JLabel ssnLabel = new JLabel("SSN:");
        JLabel nameLabel = new JLabel("Name:");
        JLabel addressLabel = new JLabel("Address:");
        JLabel phoneLabel = new JLabel("Phone:");

        JTextField ssnField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField phoneField = new JTextField(20);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        homePagePanel.add(ssnLabel, gbc);
        gbc.gridx = 1;
        homePagePanel.add(ssnField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        homePagePanel.add(nameLabel, gbc);
        gbc.gridx = 1;
        homePagePanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        homePagePanel.add(addressLabel, gbc);
        gbc.gridx = 1;
        homePagePanel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        homePagePanel.add(phoneLabel, gbc);
        gbc.gridx = 1;
        homePagePanel.add(phoneField, gbc);

        // Space Labels
        JLabel space1 = new JLabel("  ", JLabel.CENTER);
        GridBagConstraints gbc_space1 = new GridBagConstraints();
        gbc_space1.insets = new Insets(0, 0, 10, 0);
        gbc_space1.gridx = 0;
        gbc_space1.gridy = 4;
        gbc_space1.gridwidth = 2;
        homePagePanel.add(space1, gbc_space1);

        // Add Borrower Button
        JButton addBorrowerButton = new JButton("Add Borrower");
        addBorrowerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ssn = ssnField.getText();
                String name = nameField.getText();
                String address = addressField.getText();
                String phone = phoneField.getText();
                if (ssn.length() != 9) {
                    JOptionPane.showMessageDialog(homePageFrame, "SSN should be 9 digits");
                    return;
                }
                if (phone.length() != 10) {
                    JOptionPane.showMessageDialog(homePageFrame, "phone number should be 10 digits");
                    return;
                }
                try {
                    int newCardID = generateNewCardId();
                    PreparedStatement pstmt = connection.prepareStatement(
                            "INSERT INTO Borrower (Card_id, Ssn, Bname, Address, Phone) VALUES (?, ?, ?, ?, ?)"
                    );
                    pstmt.setInt(1, newCardID);
                    pstmt.setString(2, ssn);
                    pstmt.setString(3, name);
                    pstmt.setString(4, address);
                    pstmt.setString(5, phone);
                    pstmt.executeUpdate();
                    pstmt.close();

                    JOptionPane.showMessageDialog(homePageFrame, "Borrower added successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(homePageFrame, "Error adding borrower!");
                }
            }
        });

        GridBagConstraints gbc_addBorrowerButton = new GridBagConstraints();
        gbc_addBorrowerButton.fill = GridBagConstraints.HORIZONTAL;
        gbc_addBorrowerButton.insets = new Insets(0, 0, 10, 0);
        gbc_addBorrowerButton.gridx = 1;
        gbc_addBorrowerButton.gridy = 5;
        gbc_addBorrowerButton.anchor = GridBagConstraints.PAGE_END;
        homePagePanel.add(addBorrowerButton, gbc_addBorrowerButton);

        // Go Back Button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
