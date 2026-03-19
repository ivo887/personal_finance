
import repository.FinanceRepo;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FinanceRepo repo = new FinanceRepo();
        boolean running = true;

        System.out.println("=== Personal Finance Manager ===");

        while (running) {
            System.out.println("\nSelect an option:");
            System.out.println("1. Register New User");
            System.out.println("2. Record Transaction");
            System.out.println("3. Exit");
            System.out.print("> ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter username: ");
                    String user = scanner.nextLine();
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String pass = scanner.nextLine();

                    // Note: In production, hash 'pass' using BCrypt here before passing to repo
                    repo.registerUser(user, email, pass);
                    break;

                case "2":
                    System.out.print("Enter User ID: ");
                    int userId = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter Amount: ");
                    BigDecimal amount = new BigDecimal(scanner.nextLine());
                    System.out.print("Enter Type (INCOME / EXPENSE): ");
                    String type = scanner.nextLine().toUpperCase();
                    System.out.print("Enter Description: ");
                    String desc = scanner.nextLine();

                    repo.recordTransaction(userId, amount, type, desc);
                    break;

                case "3":
                    running = false;
                    System.out.println("Shutting down... Goodbye!");
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
        scanner.close();
    }
}