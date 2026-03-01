import java.sql.*;
import java.util.Scanner;

public class BankingSystem {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        createTables();

        while (true) {
            System.out.println("\n==== BANK MENU ====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.exit(0);
                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }

    // ================= TABLE CREATION =================
    public static void createTables() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS USERS(" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY," +
                    "NAME VARCHAR(100)," +
                    "EMAIL VARCHAR(100) UNIQUE," +
                    "PASSWORD VARCHAR(100)," +
                    "BALANCE DOUBLE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS TRANSACTIONS(" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY," +
                    "USER_ID INT," +
                    "TYPE VARCHAR(20)," +
                    "AMOUNT DOUBLE," +
                    "DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (USER_ID) REFERENCES USERS(ID))");

            System.out.println("Tables Ready!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= REGISTER =================
    public static void register() {
        try (Connection conn = DBConnection.getConnection()) {

            sc.nextLine();
            System.out.print("Enter Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Email: ");
            String email = sc.nextLine();

            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            System.out.print("Enter Initial Balance: ");
            double balance = sc.nextDouble();

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO USERS(NAME, EMAIL, PASSWORD, BALANCE) VALUES (?, ?, ?, ?)");
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setDouble(4, balance);

            ps.executeUpdate();
            System.out.println("Account Registered Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOGIN =================
    public static void login() {
        try (Connection conn = DBConnection.getConnection()) {

            sc.nextLine();
            System.out.print("Enter Email: ");
            String email = sc.nextLine();

            System.out.print("Enter Password: ");
            String password = sc.nextLine();

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM USERS WHERE EMAIL=? AND PASSWORD=?");
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("ID");
                System.out.println("Login Successful!");
                userMenu(userId);
            } else {
                System.out.println("Invalid Credentials!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= USER MENU =================
    public static void userMenu(int userId) {
        while (true) {
            System.out.println("\n---- USER MENU ----");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Check Balance");
            System.out.println("4. Transaction History");
            System.out.println("5. Logout");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    deposit(userId);
                    break;
                case 2:
                    withdraw(userId);
                    break;
                case 3:
                    checkBalance(userId);
                    break;
                case 4:
                    transactionHistory(userId);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid Choice!");
            }
        }
    }

    // ================= DEPOSIT =================
    public static void deposit(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            System.out.print("Enter amount to deposit: ");
            double amount = sc.nextDouble();

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE USERS SET BALANCE = BALANCE + ? WHERE ID = ?");
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();

            PreparedStatement tx = conn.prepareStatement(
                    "INSERT INTO TRANSACTIONS(USER_ID, TYPE, AMOUNT) VALUES (?, ?, ?)");
            tx.setInt(1, userId);
            tx.setString(2, "DEPOSIT");
            tx.setDouble(3, amount);
            tx.executeUpdate();

            System.out.println("Amount Deposited Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= WITHDRAW =================
    public static void withdraw(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            System.out.print("Enter amount to withdraw: ");
            double amount = sc.nextDouble();

            PreparedStatement check = conn.prepareStatement(
                    "SELECT BALANCE FROM USERS WHERE ID=?");
            check.setInt(1, userId);
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("BALANCE");

                if (balance >= amount) {

                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE USERS SET BALANCE = BALANCE - ? WHERE ID=?");
                    ps.setDouble(1, amount);
                    ps.setInt(2, userId);
                    ps.executeUpdate();

                    PreparedStatement tx = conn.prepareStatement(
                            "INSERT INTO TRANSACTIONS(USER_ID, TYPE, AMOUNT) VALUES (?, ?, ?)");
                    tx.setInt(1, userId);
                    tx.setString(2, "WITHDRAW");
                    tx.setDouble(3, amount);
                    tx.executeUpdate();

                    System.out.println("Withdrawal Successful!");
                } else {
                    System.out.println("Insufficient Balance!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CHECK BALANCE =================
    public static void checkBalance(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT BALANCE FROM USERS WHERE ID=?");
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Current Balance: " + rs.getDouble("BALANCE"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= TRANSACTION HISTORY =================
    public static void transactionHistory(int userId) {
        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM TRANSACTIONS WHERE USER_ID=?");
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n---- Transaction History ----");
            while (rs.next()) {
                System.out.println(
                        rs.getString("TYPE") + " | " +
                        rs.getDouble("AMOUNT") + " | " +
                        rs.getTimestamp("DATE"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}