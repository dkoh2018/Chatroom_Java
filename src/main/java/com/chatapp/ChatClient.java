package com.chatapp;

import java.io.*;
import java.net.*;

/**
 * Client application for connecting to and interacting with the chat server.
 * This class handles all client-side functionality including connecting to the
 * server,
 * sending messages, and receiving responses.
 *
 * The client operates using two main threads:
 * 1. A message listener thread that continuously receives and displays server
 * messages
 * 2. The main thread that handles user input and sends commands to the server
 */
public class ChatClient {

  /**
   * Initiates and manages the client connection to the chat server.
   * This method:
   * - Establishes a socket connection to the server on localhost:12345
   * - Creates input/output streams for server communication
   * - Spawns a separate thread for receiving server messages
   * - Processes user input and sends it to the server
   * - Handles graceful disconnection via the /exit command
   * - Provides error handling for network and I/O issues
   *
   * The method uses try-with-resources to ensure proper cleanup of system
   * resources.
   * All communication streams are automatically closed when the client
   * disconnects.
   */
  public static void startClient() {
    try (
        Socket socket = new Socket("localhost", 12345);
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

      new Thread(() -> {
        try {
          String serverMessage;
          while ((serverMessage = in.readLine()) != null) {
            synchronized (System.out) {
              System.out.println(serverMessage);
            }
          }
        } catch (IOException e) {
          System.err.println("Connection closed.");
        }
      }).start();

      String userInput;
      while ((userInput = consoleReader.readLine()) != null) {
        if (!userInput.trim().isEmpty()) {
          out.println(userInput);
        }
        if (userInput.trim().equalsIgnoreCase("/exit")) {
          System.out.println("Disconnecting from the server...");
          break;
        }
      }

    } catch (IOException e) {
      System.err.println("Unable to connect to the server: " + e.getMessage());
    }
  }
}
