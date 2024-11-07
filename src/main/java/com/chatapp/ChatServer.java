package com.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The ChatServer class manages the core functionality of the chat application.
 * It handles:
 * - Starting and stopping the server
 * - Creating and managing chat rooms
 * - Tracking online users
 * - Managing user connections
 * 
 * Key Features:
 * - Supports both public and private chat rooms
 * - Allows users to join multiple chat rooms
 * - Tracks online users in real-time
 * - Graceful shutdown handling
 */
public class ChatServer {
  // Stores all active chat rooms, mapped by their unique IDs
  private static final Map<String, ChatRoom> chatRooms = new HashMap<>();
  // Base port number for chat rooms
  private static final int BASE_PORT = 20000;
  // Next available port for new chat rooms
  private static int nextPort = BASE_PORT;
  // Tracks all online users and their handlers
  private static final Map<String, ClientHandler> onlineUsers = new HashMap<>();
  // Server status flag
  private static volatile boolean running = true;

  /**
   * Starts the chat server on port 12345 and listens for incoming client
   * connections
   */
  public static void startServer() throws IOException {
    System.out.println("Chat server (Main Menu) started on port 20000...");
    ServerSocket serverSocket = new ServerSocket(12345);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        System.out.println("Shutting down the server...");
        running = false;
        serverSocket.close();
      } catch (IOException e) {
        System.err.println("Error closing server socket: " + e.getMessage());
      }
    }));

    while (running) {
      try {
        Socket socket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(socket);
        new Thread(clientHandler).start();
      } catch (IOException e) {
        if (running) { // Only report the error if the server is still running
          System.err.println("Error accepting connection: " + e.getMessage());
        } else {
          System.out.println("Server socket closed, exiting...");
        }
        break;
      }
    }
  }

  /**
   * Creates a new chat room with the specified settings
   */
  public static synchronized ChatRoom createChatRoom(String roomName, String password, boolean isPrivate,
      List<String> allowedUsers) {
    String chatRoomID = generateChatRoomID();
    int port = nextPort++;
    ChatRoom chatRoom = new ChatRoom(roomName, port, isPrivate);
    chatRooms.put(chatRoomID, chatRoom);

    if (isPrivate) {
      chatRoom.getAllowedUsers().addAll(allowedUsers);
    }

    System.out.println("Created chatroom '" + roomName + "' with ID: " + chatRoomID + " on port: " + port);
    return chatRoom;
  }

  /**
   * Returns a list of all active chat rooms
   */
  public static synchronized Map<String, ChatRoom> listChatRooms() {
    return new HashMap<>(chatRooms);
  }

  /**
   * Allows a user to join a chat room if they have permission
   */
  public static synchronized ChatRoom joinChatRoom(String chatRoomID, String password, String username) {
    ChatRoom chatRoom = chatRooms.get(chatRoomID);
    if (chatRoom != null) {
      if (chatRoom.isPrivate()) {
        if (chatRoom.getAllowedUsers().contains(username)) {
          return chatRoom;
        } else {
          return null;
        }
      } else {
        return chatRoom;
      }
    }
    return null;
  }

  /**
   * Generates a unique 6-digit ID for new chat rooms
   */
  private static String generateChatRoomID() {
    Random random = new Random();
    String chatRoomID;
    do {
      chatRoomID = String.format("%06d", random.nextInt(1000000));
    } while (chatRooms.containsKey(chatRoomID));
    return chatRoomID;
  }

  /**
   * Retrieves a chat room by its ID
   */
  public static synchronized ChatRoom getChatRoomByID(String chatRoomID) {
    return chatRooms.get(chatRoomID);
  }

  /**
   * Registers a new online user
   */
  public static synchronized void registerUser(String username, ClientHandler clientHandler) {
    onlineUsers.put(username, clientHandler);
  }

  /**
   * Removes a user when they go offline
   */
  public static synchronized void unregisterUser(String username) {
    onlineUsers.remove(username);
  }

  /**
   * Checks if a user is currently online
   */
  public static synchronized boolean isUserOnline(String username) {
    return onlineUsers.containsKey(username);
  }

  /**
   * Gets the handler for a specific user
   */
  public static synchronized ClientHandler getUserHandler(String username) {
    return onlineUsers.get(username);
  }

  /**
   * Returns a list of all online users
   */
  public static synchronized Map<String, ClientHandler> getOnlineUsers() {
    return new HashMap<>(onlineUsers);
  }

  /**
   * Resets the server state (used for testing)
   */
  public static synchronized void reset() {
    chatRooms.clear();
    onlineUsers.clear();
    nextPort = BASE_PORT;
  }

  /**
   * Stops the server gracefully
   */
  public static void stopServer() {
    running = false;
    System.out.println("Server stop requested.");
  }
}
