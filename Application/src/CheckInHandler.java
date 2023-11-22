
/**
 * CheckInHandler class handles the user interface for the check-in functionality in a Library Management System.
 * It allows users to input information such as ISBN, Card No., and Borrower Name to perform check-in operations.
 * The class utilizes Swing components for GUI and interacts with a database to process check-in results.
 * 
 * @createdBy: Alper Duru
 * @createdDate: 11/21/2023
 */
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class CheckInHandler extends JFrame
{
    JFrame homePageFrame;
    JPanel checkInPagePanel;

    CheckInHandler()
    {
        buildUserInterface();
    }

    public static void main(String[] args)
    {
        new CheckInHandler();
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
        // Search field using Borrower's name
        JLabel Borrower_Label = new JLabel("Borrower Name ->           ", JLabel.RIGHT);
        Borrower_Label.setFont(new Font("Times New Roman", Font.BOLD, 14));

        GridBagConstraints Borrower_Label_Constraint = new GridBagConstraints();
        Borrower_Label_Constraint.insets = new Insets(0, 0, 5, 0);
        Borrower_Label_Constraint.gridx = 0;
        Borrower_Label_Constraint.gridy = 4;
        checkInPagePanel.add(Borrower_Label, Borrower_Label_Constraint);

        // Input text field for borrower's name
        JTextField Borrower_Input_Value = new JTextField();
        Borrower_Input_Value.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        Borrower_Input_Value.setForeground(Color.black);

        GridBagConstraints Borrower_Input_Value_Constraint = new GridBagConstraints();
        Borrower_Input_Value_Constraint.fill = GridBagConstraints.HORIZONTAL;
        Borrower_Input_Value_Constraint.insets = new Insets(0, 0, 5, 0);
        Borrower_Input_Value_Constraint.gridx = 1;
        Borrower_Input_Value_Constraint.gridy = 4;
        checkInPagePanel.add(Borrower_Input_Value, Borrower_Input_Value_Constraint);
        ISBN_Input_Value.setColumns(15);

        // -------------------------------------------------------------------------------------
        // Check In Button and its functionality
        JButton Check_In_Button = new JButton("Check In");

        Check_In_Button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // Validate input fields and handle check-in
                if (ISBN_Input_Value.getText().equals("") && 
                    Card_Input_Value.getText().equals("") && 
                    Borrower_Input_Value.getText().equals(""))
                {
                    JOptionPane.showMessageDialog(null, "Enter a value for any text box");
                }
                else
                {
                    try
                    {
                        // Pass input values to CheckInResultsHandler for processing
                        new CheckInResultsHandler(ISBN_Input_Value.getText(), 
                                                  Card_Input_Value.getText(),
                                                  Borrower_Input_Value.getText());
                    } catch (SQLException e1)
                    {
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
        checkInPagePanel.add(close, Home_Page_Return_Button_Constraint);

        // Set up the layout and make the frame visible
        homePageFrame.add(checkInPagePanel);
        homePageFrame.setVisible(true);
    }
}