package com.chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ChatRoomTest {

  private ChatRoom chatRoom;
  private TestClientHandler client1;
  private TestClientHandler client2;

  @BeforeEach
  public void setUp() {
    chatRoom = new ChatRoom("TestRoom", 12345, false);
    client1 = new TestClientHandler("J4RVIS ");
    client2 = new TestClientHandler("David");
  }

  @Test
  public void testAddMember() {
    chatRoom.addMember(client1);
    List<String> members = chatRoom.listMembers();
    assertTrue(members.contains("J4RVIS "), "J4RVIS  should be in the chat room");
  }

  @Test
  public void testRemoveMember() {
    chatRoom.addMember(client1);
    chatRoom.removeMember(client1);
    List<String> members = chatRoom.listMembers();
    assertFalse(members.contains("J4RVIS "), "J4RVIS  should not be in the chat room after removal");
  }

  @Test
  public void testBroadcastMessage() {
    chatRoom.addMember(client1);
    chatRoom.addMember(client2);

    // Reset lastMessage for both clients
    client1.clearLastMessage();
    client2.clearLastMessage();

    chatRoom.broadcastMessage("Hello, everyone!", client1);

    // Check that client2 received the message
    assertEquals("Hello, everyone!", client2.getLastMessage(), "Client2 should receive the broadcast message");
    // Ensure client1 did not receive the broadcasted message
    assertNull(client1.getLastMessage(), "Client1 should not receive their own message");
  }

  @Test
  public void testListMembers() {
    chatRoom.addMember(client1);
    chatRoom.addMember(client2);
    List<String> members = chatRoom.listMembers();
    assertEquals(2, members.size(), "Chat room should have 2 members");
    assertTrue(members.contains("J4RVIS "), "J4RVIS  should be in the chat room");
    assertTrue(members.contains("David"), "David should be in the chat room");
  }

  @Test
  public void testPrivateChatRoomAccess() {
    ChatRoom privateChatRoom = new ChatRoom("PrivateRoom", 12346, true);
    privateChatRoom.getAllowedUsers().add("J4RVIS ");

    // J4RVIS should be able to join
    privateChatRoom.addMember(client1);
    List<String> members = privateChatRoom.listMembers();
    assertTrue(members.contains("J4RVIS "), "J4RVIS  should be able to join the private chat room");

    // David should not be able to join
    privateChatRoom.addMember(client2);
    members = privateChatRoom.listMembers();
    assertFalse(members.contains("David"), "David should not be able to join the private chat room");
  }
}
