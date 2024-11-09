package com.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Core server implementation for a multi-room chat application.
 * 
 * This server provides functionality for:
 * - Managing multiple concurrent chat rooms
 * - User authentication and session management
 * - Private and public chat room creation
 * - Real-time message routing between users
 * - Dynamic port allocation for chat rooms
 * 
 * The server maintains thread-safe collections to track:
 * - Active chat rooms and their unique IDs
 * - Online users and their connection handlers
 * - Port assignments for chat room connections
 */
public class ChatServer {
  private static final Map<String, ChatRoom> chatRooms = new HashMap<>();
  private static final Map<ChatRoom, String> chatRoomIDs = new HashMap<>();
  private static final int BASE_PORT = 20000;
  private static int nextPort = BASE_PORT;
  private static final Map<String, ClientHandler> onlineUsers = new HashMap<>();
  private static volatile boolean running = true;

  /**
   * Initializes and starts the chat server on port 12345.
   * 
   * Creates a server socket and begins accepting client connections in a loop.
   * Each connected client is assigned a new ClientHandler running in its own
   * thread.
   * A shutdown hook ensures graceful server termination by closing all
   * connections.
   *
   * @throws IOException If the server socket cannot be created or if there are
   *                     connection errors
   */
  public static void startServer() throws IOException {
    System.out.println("Chat server (Main Menu) started on port 12345...");
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
        if (running) {
          System.err.println("Error accepting connection: " + e.getMessage());
        } else {
          System.out.println("Server socket closed, exiting...");
        }
        break;
      }
    }
  }

  /**
   * Creates a new chat room with specified access controls.
   * 
   * Generates a unique ID and port number for the room.
   * The room can be configured as public (password-protected) or private
   * (invite-only).
   * Thread-safe method to ensure consistent room creation.
   *
   * @param roomName      The display name for the chat room
   * @param password      Password required to join (null for private rooms)
   * @param isPrivateChat If true, room is invite-only; if false,
   *                      password-protected
   * @return The newly created ChatRoom instance
   */
  public static synchronized ChatRoom createChatRoom(String roomName, String password, boolean isPrivateChat) {
    String chatRoomID = generateChatRoomID();
    int port = nextPort++;
    ChatRoom chatRoom = new ChatRoom(roomName, port, isPrivateChat, password);
    chatRooms.put(chatRoomID, chatRoom);
    chatRoomIDs.put(chatRoom, chatRoomID);

    System.out.println("Created chatroom '" + roomName + "' with ID: " + chatRoomID + " on port: " + port);
    return chatRoom;
  }

  /**
   * Retrieves all active chat rooms.
   * 
   * Returns a defensive copy of the chat rooms map to prevent concurrent
   * modification issues.
   * Thread-safe access to the room listing.
   *
   * @return Map of room IDs to ChatRoom objects
   */
  public static synchronized Map<String, ChatRoom> listChatRooms() {
    return new HashMap<>(chatRooms);
  }

  /**
   * Processes a request to join a chat room.
   * 
   * Validates access requirements based on room type:
   * - For private rooms: Checks if user is on allowed users list
   * - For public rooms: Validates provided password
   * Thread-safe to ensure consistent access control.
   *
   * @param chatRoomID Room identifier to join
   * @param password   User-provided password (for public rooms)
   * @param username   Name of user attempting to join
   * @return ChatRoom instance if access granted, null if denied
   */
  public static synchronized ChatRoom joinChatRoom(String chatRoomID, String password, String username) {
    ChatRoom chatRoom = chatRooms.get(chatRoomID);
    if (chatRoom != null) {
      if (chatRoom.isPrivateChat()) {
        if (chatRoom.getAllowedUsers().contains(username)) {
          return chatRoom;
        } else {
          return null;
        }
      } else {
        if (Objects.equals(chatRoom.getPassword(), password)) {
          return chatRoom;
        } else {
          return null;
        }
      }
    }
    return null;
  }

  /**
   * Generates a unique identifier for new chat rooms.
   * 
   * Creates a random 6-digit number and ensures it isn't already in use.
   * Thread-safe to prevent duplicate IDs.
   *
   * @return Unique 6-digit room identifier
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
   * Looks up a chat room by its unique identifier.
   *
   * @param chatRoomID The room's unique ID
   * @return ChatRoom if found, null if not exists
   */
  public static synchronized ChatRoom getChatRoomByID(String chatRoomID) {
    return chatRooms.get(chatRoomID);
  }

  /**
   * Finds a chat room by its display name.
   * 
   * Searches through all rooms to find a name match.
   * Thread-safe room lookup.
   *
   * @param roomName The room's display name
   * @return ChatRoom if found, null if not exists
   */
  public static synchronized ChatRoom getChatRoomByName(String roomName) {
    for (ChatRoom room : chatRooms.values()) {
      if (room.getName().equals(roomName)) {
        return room;
      }
    }
    return null;
  }

  /**
   * Gets the unique identifier for a chat room instance.
   *
   * @param chatRoom The room to look up
   * @return The room's unique ID
   */
  public static synchronized String getChatRoomID(ChatRoom chatRoom) {
    return chatRoomIDs.get(chatRoom);
  }

  /**
   * Registers a new online user.
   * 
   * Associates a username with their connection handler for message routing.
   * Thread-safe user registration.
   *
   * @param username      User's chosen display name
   * @param clientHandler Handler for user's connection
   */
  public static synchronized void registerUser(String username, ClientHandler clientHandler) {
    onlineUsers.put(username, clientHandler);
  }

  /**
   * Removes a user from the online users list.
   * 
   * Called when a user disconnects or times out.
   * Thread-safe user removal.
   *
   * @param username Name of user to remove
   */
  public static synchronized void unregisterUser(String username) {
    onlineUsers.remove(username);
  }

  /**
   * Checks if a user is currently connected.
   *
   * @param username Name to check
   * @return true if user is online, false otherwise
   */
  public static synchronized boolean isUserOnline(String username) {
    return onlineUsers.containsKey(username);
  }

  /**
   * Gets the connection handler for an online user.
   *
   * @param username Name of user to look up
   * @return ClientHandler if user online, null otherwise
   */
  public static synchronized ClientHandler getUserHandler(String username) {
    return onlineUsers.get(username);
  }

  /**
   * Gets all currently connected users.
   * 
   * Returns a defensive copy to prevent concurrent modification.
   * Thread-safe access to user listing.
   *
   * @return Map of usernames to their connection handlers
   */
  public static synchronized Map<String, ClientHandler> getOnlineUsers() {
    return new HashMap<>(onlineUsers);
  }

  /**
   * Resets the server to initial state.
   * 
   * Clears all rooms, users, and resets port counter.
   * Used for testing and server maintenance.
   */
  public static synchronized void reset() {
    chatRooms.clear();
    onlineUsers.clear();
    nextPort = BASE_PORT;
  }

  /**
   * Initiates server shutdown.
   * 
   * Sets running flag to false to trigger graceful shutdown.
   */
  public static void stopServer() {
    running = false;
    System.out.println("Server stop requested.");
  }
}
