package com.chatapp;

/**
 * Test double for ClientHandler that captures messages for verification.
 * Used to simulate client behavior in unit tests.
 */
public class TestClientHandler extends ClientHandler {

  private final String username;
  private String lastMessage;

  /**
   * Creates a test client with the given username.
   */
  public TestClientHandler(String username) {
    super();
    this.username = username;
  }

  /**
   * Returns the test client's username.
   */
  @Override
  public String getUsername() {
    return username;
  }

  /**
   * Captures a message sent to this test client.
   */
  @Override
  public void sendMessage(String message) {
    lastMessage = message;
  }

  /**
   * Gets the most recently sent message.
   */
  public String getLastMessage() {
    return lastMessage;
  }

  /**
   * Resets the last message to null.
   */
  public void clearLastMessage() {
    lastMessage = null;
  }
}
