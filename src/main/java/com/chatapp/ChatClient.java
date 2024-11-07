package com.chatapp;

import java.io.*;
import java.net.*;

/**
 * A simple chat client that connects to the chat server.
 * 
 * How it works:
 * 1. Connects to the chat server running on localhost port 12345
 * 2. Creates two main components:
 * - A thread that listens for and displays messages from the server
 * - A main thread that reads user input and sends it to the server
 * 3. Handles basic commands like "/exit" to disconnect
 *
 * Key Features:
 * - Real-time message display from other users
 * - Simple command-line interface
 * - Clean disconnection with /exit command
 * - Error handling for connection issues
 */
public class ChatClient {
  public static void startClient() throws IOException {

    try (Socket socket = new Socket("localhost", 12345);
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
        if (userInput.equalsIgnoreCase("/exit")) {
          break;
        }
      }

    } catch (IOException e) {
      System.err.println("Unable to connect to the server: " + e.getMessage());
    }
  }
}
