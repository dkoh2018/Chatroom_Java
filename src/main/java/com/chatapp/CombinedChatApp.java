package com.chatapp;

import java.net.ServerSocket;

/**
 * Combined chat server and client application that provides a single entry
 * point
 * for running both components. Starts a server if needed, then launches a
 * client.
 */
public class CombinedChatApp {

  /**
   * Main entry point that:
   * 1. Checks for existing server on port 12345
   * 2. Starts new server if needed
   * 3. Launches client
   *
   * @param args Command line arguments (unused)
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
   * Checks if a server is running on the specified port.
   * Attempts to create a ServerSocket - if it fails, port is in use.
   *
   * @param port Port to check
   * @return true if server running, false otherwise
   */
  private static boolean isServerRunning(int port) {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      return false;
    } catch (Exception e) {
      return true;
    }
  }
}
