package com.chatapp;

public class TestClientHandler extends ClientHandler {

  private final String username;
  private String lastMessage;

  public TestClientHandler(String username) {
    super(); // Call the default constructor
    this.username = username;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void sendMessage(String message) {
    // For testing, store the last message sent to the client
    lastMessage = message;
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public void clearLastMessage() {
    lastMessage = null;
  }
}
