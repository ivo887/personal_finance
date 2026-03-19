
import repository.FinanceRepo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class FinanceGUI extends JFrame {

    private final FinanceRepo repo = new FinanceRepo();
    private int currentUserId = -1;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    // Dashboard Components
    private JLabel balanceLabel = new JLabel("Balance: $0.00");
    private JTable historyTable = new JTable();
    private String[] columnNames = {"ID", "Date", "Type", "Amount", "Description"};


    public FinanceGUI() {
        setTitle("Personal Finance Manager Pro");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }

        // Add all three screens to the CardLayout manager
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(createRegisterPanel(), "REGISTER");
        mainPanel.add(createDashboardPanel(), "DASHBOARD");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    /**
     * Builds the Login Screen
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Welcome to Finance Pro");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(50, 150, 250));
        loginBtn.setForeground(Color.black);
        loginBtn.setFocusPainted(false);

        JButton goToRegisterBtn = new JButton("Create an Account");
        goToRegisterBtn.setContentAreaFilled(false);
        goToRegisterBtn.setBorderPainted(false);
        goToRegisterBtn.setForeground(Color.BLUE);
        goToRegisterBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Logic for Login
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            int userId = repo.loginUser(username, password);
            if (userId != -1) {
                currentUserId = userId;
                refreshDashboard();
                cardLayout.show(mainPanel, "DASHBOARD");
                // Clear fields for security after login
                userField.setText("");
                passField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Logic to switch to Register Screen
        goToRegisterBtn.addActionListener(e -> {
            userField.setText("");
            passField.setText("");
            cardLayout.show(mainPanel, "REGISTER");
        });

        gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(userField, gbc);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passField, gbc);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginBtn, gbc);

        gbc.gridy = 4; panel.add(goToRegisterBtn, gbc);

        return panel;
    }

    /**
     * Builds the Registration Screen
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 245, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Register New Account");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JTextField userField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);

        JButton registerBtn = new JButton("Submit Registration");
        registerBtn.setBackground(new Color(34, 139, 34)); // Green
        registerBtn.setForeground(Color.black);
        registerBtn.setFocusPainted(false);

        JButton backBtn = new JButton("Back to Login");
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setForeground(Color.BLUE);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Logic for Registration
        registerBtn.addActionListener(e -> {
            String user = userField.getText();
            String email = emailField.getText();
            String pass = new String(passField.getPassword());

            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = repo.registerUser(user, email, pass);

            if (success) {
                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                userField.setText("");
                emailField.setText("");
                passField.setText("");
                cardLayout.show(mainPanel, "LOGIN"); // Send them back to login
            } else {
                JOptionPane.showMessageDialog(this, "Username or Email already exists.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Logic to cancel and go back
        backBtn.addActionListener(e -> {
            userField.setText("");
            emailField.setText("");
            passField.setText("");
            cardLayout.show(mainPanel, "LOGIN");
        });

        gbc.gridy = 0; gbc.gridwidth = 2; panel.add(title, gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; panel.add(userField, gbc);
        gbc.gridy = 2; gbc.gridx = 0; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridy = 3; gbc.gridx = 0; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passField, gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        panel.add(registerBtn, gbc);

        gbc.gridy = 5; panel.add(backBtn, gbc);

        return panel;
    }

    /**
     * Builds the Main Dashboard Screen
     */
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        balanceLabel.setForeground(new Color(34, 139, 34));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            currentUserId = -1;
            cardLayout.show(mainPanel, "LOGIN");
        });

        headerPanel.add(new JLabel("Financial Overview"), BorderLayout.NORTH);
        headerPanel.add(balanceLabel, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        historyTable.setFillsViewportHeight(true);
        historyTable.setRowHeight(25);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only allow one selection at a time
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // --- NEW: Delete Button Panel ---
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteBtn = new JButton("Remove Selected Transaction");
        deleteBtn.setBackground(new Color(220, 53, 69)); // Red danger color
        deleteBtn.setForeground(Color.BLACK);
        tableActionsPanel.add(deleteBtn);

        deleteBtn.addActionListener(e -> {
            int selectedRow = historyTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a transaction from the table first.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to remove this transaction?", "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Fetch the ID from the hidden first column (Index 0)
                int transactionId = (int) historyTable.getValueAt(selectedRow, 0);

                repo.deleteTransaction(transactionId, currentUserId);
                refreshDashboard(); // Instantly update UI
            }
        });

        JPanel formPanel = new JPanel(new FlowLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Quick Add Transaction"));

        JTextField amountField = new JTextField(8);
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"INCOME", "EXPENSE"});
        JTextField descField = new JTextField(15);
        JButton addBtn = new JButton("Save Transaction");

        formPanel.add(new JLabel("Amount: $"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeBox);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                String type = (String) typeBox.getSelectedItem();
                String desc = descField.getText();

                repo.recordTransaction(currentUserId, amount, type, desc);

                amountField.setText("");
                descField.setText("");
                refreshDashboard();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(tableActionsPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER); // Replaces the old scrollPane line
        panel.add(formPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshDashboard() {
        BigDecimal balance = repo.getUserBalance(currentUserId);
        balanceLabel.setText("Balance: $" + balance);

        Object[][] data = repo.getTransactionHistory(currentUserId);
        historyTable.setModel(new DefaultTableModel(data, columnNames));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FinanceGUI().setVisible(true);
        });
    }
}