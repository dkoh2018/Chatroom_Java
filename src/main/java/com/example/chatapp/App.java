package com.example;

import java.util.Scanner;

public class App {
  private Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) {
    App app = new App();
    app.showMainMenu();
  }

  private void showMainMenu() {
    int choice = -1;

    while (choice != 4) {
      System.out.println("\nMain Menu:");
      System.out.println("1) Join Chatroom");
      System.out.println("2) Direct Message");
      System.out.println("3) Show Active Users");
      System.out.println("4) Exit");
      System.out.print("Please select an option: ");

      choice = getValidIntegerInput();

      switch (choice) {
        case 1:
          showJoinChatroomMenu();
          break;
        case 2:
          System.out.println("Direct Message functionality is under development.");
          break;
        case 3:
          System.out.println("Show Active Users functionality is under development.");
          break;
        case 4:
          System.out.println("Exiting the application. Goodbye!");
          break;
        default:
          System.out.println("Invalid option. Please try again.");
      }
    }
  }

  private void showJoinChatroomMenu() {
    int choice = -1;

    while (choice != 3) {
      System.out.println("\nJoin Chatroom Menu:");
      System.out.println("1) Create Chatroom");
      System.out.println("2) Join Existing Chatrooms");
      System.out.println("3) Back to Main Menu");
      System.out.print("Please select an option: ");

      choice = getValidIntegerInput();

      switch (choice) {
        case 1:
          System.out.println("Create Chatroom functionality is under development.");
          break;
        case 2:
          System.out.println("Join Existing Chatrooms functionality is under development.");
          break;
        case 3:
          System.out.println("Returning to Main Menu.");
          break;
        default:
          System.out.println("Invalid option. Please try again.");
      }
    }
  }

  private int getValidIntegerInput() {
    while (!scanner.hasNextInt()) {
      System.out.println("Invalid input. Please enter a number.");
      scanner.next(); // Discard invalid input
      System.out.print("Please select an option: ");
    }
    int input = scanner.nextInt();
    scanner.nextLine(); // Consume newline
    return input;
  }
}
