package com.chatapp;

import java.util.*;

/**
 * ChatRoom represents a chat room where multiple users can join and exchange
 * messages.
 * 
 * Key Features:
 * - Supports both public and private chat rooms
 * - Manages a list of connected members
 * - Handles message broadcasting between members
 * - Controls access for private rooms
 */
public class ChatRoom {

  // The name of the chat room
  private final String name;
  // List of currently connected members
  private final List<ClientHandler> members = new ArrayList<>();
  // The port number this chat room runs on
  private final int port;
  // Whether this is a private room with restricted access
  private final boolean isPrivate;
  // List of users allowed to join if this is a private room
  private final Set<String> allowedUsers;

  /**
   * Creates a new chat room
   * 
   * @param name      The name of the chat room
   * @param port      The port number for this room
   * @param isPrivate Whether this is a private room
   */
  public ChatRoom(String name, int port, boolean isPrivate) {
    this.name = name;
    this.port = port;
    this.isPrivate = isPrivate;
    this.allowedUsers = new HashSet<>();
  }

  /**
   * Gets the name of this chat room
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the port number of this chat room
   */
  public int getPort() {
    return port;
  }

  /**
   * Checks if this is a private chat room
   */
  public boolean isPrivate() {
    return isPrivate;
  }

  /**
   * Gets the list of users allowed in this private room
   */
  public Set<String> getAllowedUsers() {
    return allowedUsers;
  }

  /**
   * Adds a new member to the chat room
   * For private rooms, checks if the user is allowed to join
   */
  public synchronized void addMember(ClientHandler client) {
    if (isPrivate && !allowedUsers.contains(client.getUsername())) {
      client.sendMessage("You are not allowed to join this private chatroom.");
      return;
    }
    members.add(client);
    broadcastMessage(client.getUsername() + " has joined the chatroom " + name, client);
  }

  /**
   * Removes a member from the chat room
   */
  public synchronized void removeMember(ClientHandler client) {
    members.remove(client);
    broadcastMessage(client.getUsername() + " has left the chatroom " + name, client);
  }

  /**
   * Sends a message to all members in the room except the sender
   */
  public synchronized void broadcastMessage(String message, ClientHandler sender) {
    for (ClientHandler member : members) {
      if (sender == null || member != sender) {
        member.sendMessage(message);
      }
    }
  }

  /**
   * Gets a list of all usernames currently in the room
   */
  public List<String> listMembers() {
    List<String> usernames = new ArrayList<>();
    for (ClientHandler member : members) {
      usernames.add(member.getUsername());
    }
    return Collections.unmodifiableList(usernames);
  }
}
