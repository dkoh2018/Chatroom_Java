package com.chatapp;

import java.util.*;

/**
 * Represents a chat room where multiple clients can join and communicate.
 * Each chat room has a unique name and port number, and can be configured as
 * either public or private.
 * Private chat rooms require a password and maintain a list of allowed users.
 */
public class ChatRoom {
  private final String name;
  private final List<ClientHandler> members = new ArrayList<>();
  private final int port;
  private final boolean isPrivateChat;
  private final String password;
  private final Set<String> allowedUsers;

  /**
   * Creates a new chat room with the specified parameters.
   *
   * @param name          The unique name of the chat room
   * @param port          The port number this chat room operates on
   * @param isPrivateChat Whether this is a private chat room requiring
   *                      authorization
   * @param password      The password required to join if this is a private chat
   *                      room
   */
  public ChatRoom(String name, int port, boolean isPrivateChat, String password) {
    this.name = name;
    this.port = port;
    this.isPrivateChat = isPrivateChat;
    this.password = password;
    this.allowedUsers = new HashSet<>();
  }

  /**
   * Gets the name of this chat room.
   *
   * @return The chat room's name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the port number this chat room operates on.
   *
   * @return The chat room's port number
   */
  public int getPort() {
    return port;
  }

  /**
   * Checks if this is a private chat room.
   *
   * @return true if this is a private chat room, false otherwise
   */
  public boolean isPrivateChat() {
    return isPrivateChat;
  }

  /**
   * Gets the password required to join this chat room.
   * Only applicable for private chat rooms.
   *
   * @return The chat room's password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Gets the set of usernames allowed to join this private chat room.
   * Only applicable for private chat rooms.
   *
   * @return An unmodifiable set of allowed usernames
   */
  public Set<String> getAllowedUsers() {
    return allowedUsers;
  }

  /**
   * Adds a new client to this chat room. For private chat rooms, verifies the
   * client
   * is in the allowed users list before adding them. Notifies all existing
   * members
   * when a new client joins.
   *
   * This method is synchronized to ensure thread-safe access to the members list.
   *
   * @param client The ClientHandler representing the client to be added
   */
  public synchronized void addMember(ClientHandler client) {
    if (isPrivateChat && !allowedUsers.contains(client.getUsername())) {
      client.sendMessage("You are not allowed to join this private chatroom.");
      return;
    }
    members.add(client);
    broadcastMessage(client.getUsername() + " has joined the chatroom " + name, client);
  }

  /**
   * Removes a client from this chat room and notifies remaining members of their
   * departure.
   * This method is synchronized to ensure thread-safe access to the members list.
   *
   * @param client The ClientHandler representing the client to be removed
   */
  public synchronized void removeMember(ClientHandler client) {
    members.remove(client);
    broadcastMessage(client.getUsername() + " has left the chatroom " + name, client);
  }

  /**
   * Broadcasts a message to all members in the chat room except the sender.
   * If sender is null, the message is sent to all members.
   * This method is synchronized to ensure thread-safe access to the members list.
   *
   * @param message The message to broadcast
   * @param sender  The ClientHandler of the message sender (can be null to send
   *                to everyone)
   * @throws NullPointerException if the message parameter is null
   */
  public synchronized void broadcastMessage(String message, ClientHandler sender) {
    if (message == null) {
      throw new NullPointerException("Message cannot be null");
    }
    for (ClientHandler member : members) {
      if (sender == null || member != sender) {
        member.sendMessage(message);
      }
    }
  }

  /**
   * Returns a list of usernames of all current members in the chat room.
   * The returned list is unmodifiable to prevent external modifications.
   *
   * @return An unmodifiable List containing the usernames of all members
   */
  public List<String> listMembers() {
    List<String> usernames = new ArrayList<>();
    for (ClientHandler member : members) {
      usernames.add(member.getUsername());
    }
    return Collections.unmodifiableList(usernames);
  }
}
