package com.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles communication between the server and a connected client.
 * This class manages all aspects of client interaction including user
 * registration,
 * chat room management, private messaging, and message handling.
 *
 * The handler runs as a separate thread for each connected client, allowing
 * concurrent
 * handling of multiple users. It maintains the client's socket connection,
 * input/output
 * streams, and current chat room state.
 */
public class ClientHandler implements Runnable {
  protected Socket socket;
  protected BufferedReader in;
  protected PrintWriter out;
  protected String username;
  protected ChatRoom chatRoom;
  private Map<String, String> pendingInvitations = new HashMap<>();

  /**
   * Creates a new ClientHandler for a connected socket.
   * Initializes the input and output streams for communication.
   *
   * @param socket The client's connected socket
   * @throws IOException If there is an error creating the I/O streams
   */
  public ClientHandler(Socket socket) throws IOException {
    this.socket = socket;
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.out = new PrintWriter(socket.getOutputStream(), true);
  }

  /**
   * Default constructor used for testing purposes.
   * Does not initialize any connection-related fields.
   */
  public ClientHandler() {
    // Empty constructor for testing
  }

  /**
   * Main execution method that handles the client's entire session.
   * Manages user registration, displays the main menu, and processes
   * all client commands until disconnection.
   *
   * The method handles:
   * - Initial user registration
   * - Main menu display and command processing
   * - Chat invitation responses
   * - Cleanup on disconnection
   */
  @Override
  public void run() {
    try {
      // User registration
      out.println("\n=== Welcome to the Main Menu ===\n");
      out.println("Please enter your username:");
      username = in.readLine();

      if (username == null || username.trim().isEmpty()) {
        out.println("Invalid username. Connection closing.");
        return;
      }

      ChatServer.registerUser(username, this);

      // Main menu loop
      while (true) {
        displayMainMenu();
        String choice = in.readLine();

        if (choice == null) {
          // Client disconnected
          break;
        }

        if (choice.startsWith("/accept ")) {
          String fromUser = choice.substring(8).trim();
          acceptInvitation(fromUser);
          continue;
        } else if (choice.startsWith("/decline ")) {
          String fromUser = choice.substring(9).trim();
          declineInvitation(fromUser);
          continue;
        }

        switch (choice) {
          case "1":
            createChatRoom();
            break;
          case "2":
            joinChatRoom();
            break;
          case "3":
            listOnlineUsers();
            break;
          case "4":
            startPrivateChat();
            break;
          case "/exit":
            out.println("Goodbye!");
            return; // Exit the run method
          default:
            out.println("\nInvalid choice. Please try again.\n");
        }
      }

    } catch (IOException e) {
      System.err.println("Error handling client (" + username + "): " + e.getMessage());
    } finally {
      // Cleanup on disconnection
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
   * Returns the username of this client.
   *
   * @return The client's username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sends a message to this client.
   *
   * @param message The message to send
   */
  public void sendMessage(String message) {
    out.println(message);
  }

  /**
   * Displays the main menu options to the client.
   * Shows available commands and navigation options.
   */
  private void displayMainMenu() {
    out.println("\n----------------------------");
    out.println("          Main Menu          ");
    out.println("----------------------------");
    out.println(" 1. Create a new chatroom");
    out.println(" 2. Join an existing chatroom");
    out.println(" 3. List online users");
    out.println(" 4. Start a private chat with a user");
    out.println("/exit - Exit the application");
    out.println("----------------------------");
    out.println("Enter your choice:\n");
  }

  /**
   * Handles the creation of a new chat room.
   * Prompts for room name and password, then creates the room.
   *
   * @throws IOException If there is an error reading client input
   */
  private void createChatRoom() throws IOException {
    out.println("\n--- Create a New Chatroom ---\n");
    out.println("Enter a name for your chatroom:");
    String roomName = in.readLine();

    out.println("Set a password for your chatroom:");
    String password = in.readLine();

    ChatRoom chatRoom = ChatServer.createChatRoom(roomName, password, false);
    out.println("\nChatroom created!");
    out.println("Chatroom ID: " + ChatServer.getChatRoomID(chatRoom) + "\n");
  }

  /**
   * Handles the process of joining an existing chat room.
   * Validates room existence, password, and user permissions.
   *
   * @throws IOException If there is an error reading client input
   */
  private void joinChatRoom() throws IOException {
    out.println("\n--- Join an Existing Chatroom ---");
    listAvailableChatRooms();
    out.println("Enter the chatroom ID or name:");
    String roomIdentifier = in.readLine();

    if (roomIdentifier == null || roomIdentifier.trim().isEmpty()) {
      out.println("\nInvalid input. Please try again.\n");
      return;
    }

    ChatRoom chatRoom = ChatServer.getChatRoomByID(roomIdentifier);

    if (chatRoom == null) {
      chatRoom = ChatServer.getChatRoomByName(roomIdentifier);
    }

    if (chatRoom == null) {
      out.println("\nChatroom not found. Please try again.\n");
      return;
    }

    if (chatRoom.isPrivateChat()) {
      if (!chatRoom.getAllowedUsers().contains(username)) {
        out.println("\nYou are not allowed to join this private chatroom.\n");
        return;
      }
    } else {
      out.println("Enter the chatroom password:");
      String password = in.readLine();
      if (!chatRoom.getPassword().equals(password)) {
        out.println("\nIncorrect password. Please try again.\n");
        return;
      }
    }

    out.println("\nSuccessfully joined the chatroom: " + chatRoom.getName() + " (ID: "
        + ChatServer.getChatRoomID(chatRoom) + ")");
    this.chatRoom = chatRoom;
    chatRoom.addMember(this);

    handleChatRoomMessages();
  }

  /**
   * Displays a list of all available chat rooms to the client.
   * Shows only rooms that the user has permission to join.
   */
  private void listAvailableChatRooms() {
    out.println("\n--- Available Chatrooms ---");
    boolean roomsAvailable = false;
    for (Map.Entry<String, ChatRoom> entry : ChatServer.listChatRooms().entrySet()) {
      ChatRoom room = entry.getValue();
      if (!room.isPrivateChat() || room.getAllowedUsers().contains(username)) {
        out.println("ID: " + entry.getKey() + " | Name: " + room.getName());
        roomsAvailable = true;
      }
    }
    if (!roomsAvailable) {
      out.println("No available chatrooms at the moment.");
    }
    out.println("\n----------------------------\n");
  }

  /**
   * Displays a list of all currently online users.
   */
  private void listOnlineUsers() {
    out.println("\n--- Online Users ---");
    for (String user : ChatServer.getOnlineUsers().keySet()) {
      out.println(user);
    }
    out.println("\n---------------------\n");
  }

  /**
   * Initiates a private chat with another user.
   * Creates a private chat room and sends an invitation.
   *
   * @throws IOException If there is an error reading client input
   */
  private void startPrivateChat() throws IOException {
    out.println("\n--- Start a Private Chat ---");
    out.println("Enter the username of the user you want to chat with:");
    String targetUsername = in.readLine();

    if (targetUsername == null || targetUsername.trim().isEmpty()) {
      out.println("\nInvalid username. Please try again.\n");
      return;
    }

    if (ChatServer.isUserOnline(targetUsername)) {
      String roomName = username + " & " + targetUsername + "'s Private Chat";
      ChatRoom chatRoom = ChatServer.createChatRoom(roomName, null, true);
      chatRoom.getAllowedUsers().add(username);
      chatRoom.getAllowedUsers().add(targetUsername);

      this.chatRoom = chatRoom;
      chatRoom.addMember(this);

      ClientHandler targetUserHandler = ChatServer.getUserHandler(targetUsername);
      targetUserHandler.receiveInvitation(username, roomName);

      out.println("\nInvitation sent to '" + targetUsername + "'. Waiting for their response...");

      handleChatRoomMessages();
    } else {
      out.println("\nUser '" + targetUsername + "' is not online.\n");
    }
  }

  /**
   * Processes an incoming chat invitation from another user.
   *
   * @param fromUser     The username of the inviting user
   * @param chatRoomName The name of the private chat room
   */
  public void receiveInvitation(String fromUser, String chatRoomName) {
    pendingInvitations.put(fromUser, chatRoomName);
    sendMessage("\nUser '" + fromUser + "' invites you to a private chat.");
    sendMessage("Type '/accept " + fromUser + "' to join or '/decline " + fromUser + "' to decline.");
  }

  /**
   * Handles acceptance of a chat invitation.
   * Joins the private chat room and notifies the inviter.
   *
   * @param fromUser The username of the user who sent the invitation
   */
  private void acceptInvitation(String fromUser) {
    String chatRoomName = pendingInvitations.remove(fromUser);
    if (chatRoomName != null) {
      ChatRoom room = ChatServer.getChatRoomByName(chatRoomName);
      if (room != null) {
        room.getAllowedUsers().add(username);
        this.chatRoom = room;
        chatRoom.addMember(this);
        sendMessage("\nYou have accepted the invitation and joined the chatroom: " + chatRoom.getName());
        ClientHandler inviter = ChatServer.getUserHandler(fromUser);
        if (inviter != null) {
          inviter.sendMessage("\nUser '" + username + "' has accepted your invitation.");
        }
        try {
          handleChatRoomMessages();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        sendMessage("\nChatroom not found.");
      }
    } else {
      sendMessage("\nNo invitation found from '" + fromUser + "'.");
    }
  }

  /**
   * Handles declining of a chat invitation.
   * Removes the invitation and notifies the inviter.
   *
   * @param fromUser The username of the user who sent the invitation
   */
  private void declineInvitation(String fromUser) {
    String chatRoomName = pendingInvitations.remove(fromUser);
    if (chatRoomName != null) {
      sendMessage("\nYou have declined the invitation from '" + fromUser + "'.");
      ClientHandler inviter = ChatServer.getUserHandler(fromUser);
      if (inviter != null) {
        inviter.sendMessage("\nUser '" + username + "' has declined your invitation.");
      }
    } else {
      sendMessage("\nNo invitation found from '" + fromUser + "'.");
    }
  }

  /**
   * Manages the message flow within a chat room.
   * Handles commands and message broadcasting until the user exits.
   *
   * @throws IOException If there is an error reading client input
   */
  private void handleChatRoomMessages() throws IOException {
    out.println("\n--- Chatroom ---\n");
    String message;
    try {
      while ((message = in.readLine()) != null) {
        if (message.equalsIgnoreCase("/exit")) {
          out.println("\nExiting the chatroom...\n");
          break; // Exit the chatroom loop
        } else if (message.startsWith("/join ")) {
          String roomName = message.substring(6).trim();
          joinChatRoomByName(roomName);
          break;
        } else if (message.startsWith("/accept ")) {
          String fromUser = message.substring(8).trim();
          acceptInvitation(fromUser);
          continue;
        } else if (message.startsWith("/decline ")) {
          String fromUser = message.substring(9).trim();
          declineInvitation(fromUser);
          continue;
        }
        if (message.trim().isEmpty()) {
          continue;
        }
        chatRoom.broadcastMessage(username + ": " + message, this);
      }
    } catch (IOException e) {
      System.err.println("Connection lost with user: " + username);
    } finally {
      if (chatRoom != null) {
        chatRoom.removeMember(this);
        this.chatRoom = null; // Reset current chatroom
      }
      out.println("You have returned to the main menu.");
    }
  }

  /**
   * Attempts to join a chat room by its name.
   * Only allows joining private rooms where the user is allowed.
   *
   * @param roomName The name of the chat room to join
   */
  private void joinChatRoomByName(String roomName) {
    for (ChatRoom room : ChatServer.listChatRooms().values()) {
      if (room.getName().equals(roomName) && room.isPrivateChat() && room.getAllowedUsers().contains(username)) {
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
