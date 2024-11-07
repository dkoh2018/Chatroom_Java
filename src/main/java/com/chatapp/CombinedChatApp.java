package com.chatapp;

import java.net.ServerSocket;

/**
 * Main application class that combines both server and client functionality.
 * This class allows the chat application to run in a smart way - either as the
 * first instance
 * (which becomes the server) or subsequent instances (which become clients).
 */
public class CombinedChatApp {
  /**
   * Main entry point of the application.
   * First checks if a server is already running:
   * - If no server is running, starts one in a new thread
   * - If a server exists, connects as a client
   * 
   * @param args Command line arguments (not used)
   */
  public static void main(String[] args) {
    if (!isServerRunning(12345)) {
      new Thread(() -> {
        try {
          ChatServer.startServer();
        } catch (Exception e) {
          if (!e.getMessage().contains("Address already in use")) {
            System.err.println("Failed to start server: " + e.getMessage());
          }
        }
      }).start();
    } else {
      System.out.println("Server is already running. Connecting as a client...");
    }

    try {
      ChatClient.startClient();
    } catch (Exception e) {
      System.err.println("Unable to start the client: " + e.getMessage());
    }
  }

  /**
   * Checks if a chat server is already running on the specified port.
   * 
   * @param port The port number to check (default 12345)
   * @return true if a server is running, false otherwise
   */
  private static boolean isServerRunning(int port) {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      return false;
    } catch (Exception e) {
      return true;
    }
  }
}
