package com.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * The ClientHandler class manages individual client connections and their
 * interactions with the chat server.
 * It provides functionality for users to:
 * - Create and join chat rooms
 * - Send messages in chat rooms
 * - Start private chats with other users
 * - View online users and available chat rooms
 */
public class ClientHandler implements Runnable {
  // Socket for client connection
  protected Socket socket;
  // Input stream to receive messages from client
  protected BufferedReader in;
  // Output stream to send messages to client
  protected PrintWriter out;
  // Username of the connected client
  protected String username;
  // Current chat room the client is in
  protected ChatRoom chatRoom;

  /**
   * Creates a new ClientHandler for a connected client
   * 
   * @param socket The client's socket connection
   */
  public ClientHandler(Socket socket) throws IOException {
    this.socket = socket;
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.out = new PrintWriter(socket.getOutputStream(), true);
  }

  /**
   * Empty constructor for testing purposes
   */
  public ClientHandler() {
    // No initialization required for testing
  }

  /**
   * Main loop that handles client interaction:
   * 1. Gets username from client
   * 2. Shows main menu
   * 3. Processes client commands
   * 4. Handles disconnection
   */
  @Override
  public void run() {
    try {
      out.println("\n=== Welcome to the Main Menu ===\n");
      out.println("Please enter your username:");
      username = in.readLine();

      // Register user with the chat server
      ChatServer.registerUser(username, this);

      while (true) {
        displayMainMenu();
        String choice = in.readLine();

        switch (choice) {
          case "1":
            createChatRoom();
            break;
          case "2":
            listChatRooms();
            break;
          case "3":
            joinChatRoom();
            break;
          case "4":
            listOnlineUsers();
            break;
          case "5":
            startPrivateChat();
            break;
          case "/exit":
            return;
          default:
            out.println("\nInvalid choice. Please try again.\n");
        }
      }

    } catch (IOException e) {
      System.err.println("Error handling client (" + username + "): " + e.getMessage());
    } finally {
      // Unregister user and close resources
      ChatServer.unregisterUser(username);
      if (chatRoom != null) {
        chatRoom.removeMember(this);
      }
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Gets the username of this client
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sends a message to this client
   */
  public void sendMessage(String message) {
    out.println(message);
  }

  /**
   * Shows the main menu options to the client
   */
  private void displayMainMenu() {
    out.println("\n----------------------------");
    out.println("          Main Menu          ");
    out.println("----------------------------");
    out.println(" 1. Create a new chatroom");
    out.println(" 2. List available chatrooms");
    out.println(" 3. Join an existing chatroom");
    out.println(" 4. List online users");
    out.println(" 5. Start a private chat with a user");
    out.println("/exit - Leave the chatroom");
    out.println("----------------------------");
    out.println("Enter your choice:\n");
  }

  /**
   * Handles creation of a new chat room
   * Allows setting room name, privacy settings, and allowed users
   */
  private void createChatRoom() throws IOException {
    out.println("\n--- Create a New Chatroom ---\n");
    out.println("Enter a name for your chatroom:");
    String roomName = in.readLine();

    out.println("Is this a private chatroom? (yes/no):");
    String privateChoice = in.readLine();
    boolean isPrivate = privateChoice.equalsIgnoreCase("yes");

    List<String> allowedUsers = new ArrayList<>();
    String password = "";

    if (isPrivate) {
      out.println("Enter the username(s) of the user(s) you want to invite (separated by commas):");
      String users = in.readLine();
      String[] userArray = users.split(",");
      for (String user : userArray) {
        allowedUsers.add(user.trim());
      }
      allowedUsers.add(username);
    } else {
      out.println("Set a password for your chatroom:");
      password = in.readLine();
    }

    ChatRoom chatRoom = ChatServer.createChatRoom(roomName, password, isPrivate, allowedUsers);
    out.println("\nChatroom created!");
    out.println("Chatroom ID: " + chatRoom.getName() + "\n");
  }

  /**
   * Shows list of available chat rooms to the client
   */
  private void listChatRooms() throws IOException {
    out.println("\n--- Available Chatrooms ---");
    for (Map.Entry<String, ChatRoom> entry : ChatServer.listChatRooms().entrySet()) {
      ChatRoom room = entry.getValue();
      if (!room.isPrivate() || room.getAllowedUsers().contains(username)) {
        out.println("ID: " + entry.getKey() + " | Name: " + room.getName());
      }
    }
    out.println("\n----------------------------\n");
  }

  /**
   * Handles joining an existing chat room
   * Verifies room ID and password if required
   */
  private void joinChatRoom() throws IOException {
    out.println("\n--- Join a Chatroom ---");
    out.println("Enter the chatroom ID:");
    String roomID = in.readLine();

    String password = "";
    ChatRoom chatRoom = ChatServer.getChatRoomByID(roomID);

    if (chatRoom == null) {
      out.println("\nInvalid chatroom ID. Please try again.\n");
      return;
    }

    if (!chatRoom.isPrivate()) {
      out.println("Enter the chatroom password:");
      password = in.readLine();
    }

    chatRoom = ChatServer.joinChatRoom(roomID, password, username);
    if (chatRoom != null) {
      out.println("\nSuccessfully joined the chatroom: " + chatRoom.getName() + " (ID: " + roomID + ")");
      this.chatRoom = chatRoom;
      chatRoom.addMember(this);

      handleChatRoomMessages();
    } else {
      out.println(
          "\nInvalid chatroom ID or password, or you are not allowed to join this chatroom. Please try again.\n");
    }
  }

  /**
   * Shows list of currently online users
   */
  private void listOnlineUsers() {
    out.println("\n--- Online Users ---");
    for (String user : ChatServer.getOnlineUsers().keySet()) {
      out.println(user);
    }
    out.println("\n---------------------\n");
  }

  /**
   * Starts a private chat with another user
   * Creates a private room and invites the target user
   */
  private void startPrivateChat() throws IOException {
    out.println("\n--- Start a Private Chat ---");
    out.println("Enter the username of the user you want to chat with:");
    String targetUsername = in.readLine();

    if (ChatServer.isUserOnline(targetUsername)) {
      String roomName = username + " & " + targetUsername + "'s Private Chat";
      List<String> allowedUsers = Arrays.asList(username, targetUsername);
      ChatRoom chatRoom = ChatServer.createChatRoom(roomName, "", true, allowedUsers);

      out.println("\nPrivate chatroom created!");
      out.println("Chatroom ID: " + chatRoom.getName() + "\n");

      ClientHandler targetUserHandler = ChatServer.getUserHandler(targetUsername);
      targetUserHandler.sendMessage("\nUser '" + username + "' invites you to a private chat.");
      targetUserHandler.sendMessage("Type '/join " + roomName + "' to join the chatroom.");

      this.chatRoom = chatRoom;
      chatRoom.addMember(this);

      handleChatRoomMessages();
    } else {
      out.println("\nUser '" + targetUsername + "' is not online.\n");
    }
  }

  /**
   * Handles message exchange within a chat room
   * Processes special commands like /exit and /join
   */
  private void handleChatRoomMessages() throws IOException {
    out.println("\n--- Chatroom ---\n");
    String message;
    while ((message = in.readLine()) != null) {
      if (message.equalsIgnoreCase("/exit")) {
        out.println("\nExiting the chatroom...\n");
        break;
      } else if (message.startsWith("/join ")) {
        String roomName = message.substring(6).trim();
        joinChatRoomByName(roomName);
        break;
      }
      if (message.trim().isEmpty()) {
        continue;
      }
      chatRoom.broadcastMessage(username + ": " + message, this);
    }
    chatRoom.removeMember(this);
  }

  /**
   * Joins a chat room using its name instead of ID
   * Used primarily for private chat invitations
   */
  private void joinChatRoomByName(String roomName) {
    for (ChatRoom room : ChatServer.listChatRooms().values()) {
      if (room.getName().equals(roomName) && room.isPrivate() && room.getAllowedUsers().contains(username)) {
        this.chatRoom = room;
        chatRoom.addMember(this);
        out.println("\nSuccessfully joined the chatroom: " + chatRoom.getName());
        try {
          handleChatRoomMessages();
        } catch (IOException e) {
          e.printStackTrace();
        }
        return;
      }
    }
    out.println("\nChatroom not found or you are not allowed to join.\n");
  }
}
