import java.io.*;
import java.util.*;

public class LibrarySystem {
    private static List<User> users = new ArrayList<>();
    private static List<Book> books = new ArrayList<>();
    private static List<Transaction> transactions = new ArrayList<>();
    private static User loggedInUser = null;

    public static void main(String[] args) {
        System.out.println("Welcome to the Library Management System\n");

        loadUsers();
        loadBooks();
        loadTransactions();

        login();

        if (loggedInUser != null) {
            displayMenu();
        }

        saveBooks();
        saveTransactions();

        System.out.println("Exiting... All data saved.");
    }

    // Load users from users.txt
    private static void loadUsers() {
        try (BufferedReader br = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 4) {
                    users.add(new User(parts[0], parts[1], parts[2], parts[3]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading users.txt: " + e.getMessage());
        }
    }

    // Load books from books.txt
    private static void loadBooks() {
        try (BufferedReader br = new BufferedReader(new FileReader("books.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 4) {
                    books.add(new Book(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3])));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading books.txt: " + e.getMessage());
        }
    }

    // Load transactions from transactions.txt
    private static void loadTransactions() {
        try (BufferedReader br = new BufferedReader(new FileReader("transactions.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length >= 4) {
                    String dateReturned = parts.length == 5 ? parts[4] : "";
                    transactions.add(new Transaction(parts[0], parts[1], parts[2], parts[3], dateReturned));
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading transactions.txt: " + e.getMessage());
        }
    }

    // Login system with 3 attempts
    private static void login() {
        Scanner scanner = new Scanner(System.in);
        int attempts = 3;

        while (attempts > 0) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            for (User user : users) {
                if (user.getName().equalsIgnoreCase(username) && user.getPassword().equals(password)) {
                    loggedInUser = user;
                    System.out.println("\nLogin successful! Welcome, " + user.getName() + "\n");
                    return;
                }
            }

            attempts--;
            System.out.println("Invalid username or password. Attempts left: " + attempts);
        }

        System.out.println("Too many failed attempts. Exiting...");
        System.exit(0);
    }

    // Display main menu
    private static void displayMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. View All Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
                System.out.println("4. View Transactions");
                System.out.println("5. Exit");
            } else {
                System.out.println("4. Exit");
            }

            System.out.print("Enter choice: ");
            int choice;

            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1 -> viewAllBooks();
                case 2 -> borrowBook(scanner);
                case 3 -> returnBook(scanner);
                case 4 -> {
                    if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
                        viewTransactions();
                    } else {
                        return;
                    }
                }
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // View books
    private static void viewAllBooks() {
        System.out.println("\nAvailable Books:");
        for (Book book : books) {
            book.displayBookDetails();
        }
    }

    // Borrow book
    private static void borrowBook(Scanner scanner) {
        System.out.print("Enter Book ID to borrow: ");
        String bookId = scanner.nextLine();

        for (Book book : books) {
            if (book.getBookId().equalsIgnoreCase(bookId)) {
                if (!book.isAvailable()) {
                    System.out.println("Book is not available.");
                    return;
                }

                if (!loggedInUser.canBorrowMore()) {
                    System.out.println("Borrow limit reached (3 books max).");
                    return;
                }

                // Borrow
                book.setAvailable(false);
                loggedInUser.borrowBook(bookId);

                String txnId = "T" + String.format("%03d", transactions.size() + 1);
                String today = java.time.LocalDate.now().toString();
                transactions.add(new Transaction(txnId, loggedInUser.getId(), bookId, today, ""));

                System.out.println("Book borrowed successfully!");
                return;
            }
        }

        System.out.println("Book ID not found.");
    }

    // Return book
    private static void returnBook(Scanner scanner) {
        System.out.print("Enter Book ID to return: ");
        String bookId = scanner.nextLine();

        for (Transaction t : transactions) {
            if (t.getBookId().equalsIgnoreCase(bookId)
                    && t.getUserId().equals(loggedInUser.getId())
                    && !t.isReturned()) {

                t.setDateReturned(java.time.LocalDate.now().toString());

                for (Book book : books) {
                    if (book.getBookId().equalsIgnoreCase(bookId)) {
                        book.setAvailable(true);
                        break;
                    }
                }

                loggedInUser.returnBook(bookId);
                System.out.println("Book returned successfully!");
                return;
            }
        }

        System.out.println("No active borrow found for that book.");
    }

    // Admin-only view transactions
    private static void viewTransactions() {
        System.out.println("\n--- Transaction Logs ---");
        for (Transaction t : transactions) {
            t.displayTransaction();
        }
    }

    // Save updated book availability
    private static void saveBooks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("books.txt"))) {
            for (Book book : books) {
                pw.println(book.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving books: " + e.getMessage());
        }
    }

    // Save updated transactions
    private static void saveTransactions() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("transactions.txt"))) {
            for (Transaction t : transactions) {
                pw.println(t.toFileString());
            }
        } catch (IOException e) {
            System.out.println("Error saving transactions: " + e.getMessage());
        }
    }
}
