package com.chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test Summary:
 * - testAddMember: Verifies adding members to chat room
 * - testRemoveMember: Checks member removal functionality
 * - testBroadcastMessage: Tests message broadcasting to members
 * - testIsPrivateChat: Validates private chat room status checking
 * - testAllowedUsersInPrivateChat: Tests allowed users list management
 * - testJoinPrivateChatWithoutPermission: Verifies private room access control
 * - testBroadcastMessageFromNullSender: Tests system message broadcasting
 * - testRemoveMemberFromEmptyChatRoom: Checks empty room member removal
 * - testGetAllowedUsersInPublicChat: Validates public room user management
 * - testNullMessageBroadcast: Tests null message handling
 */

public class ChatRoomTest {

  private ChatRoom chatRoom;
  private TestClientHandler client1;
  private TestClientHandler client2;

  /**
   * Sets up a test chat room and two test clients before each test.
   */
  @BeforeEach
  public void setUp() {
    chatRoom = new ChatRoom("TestRoom", 12345, false, "password123");
    client1 = new TestClientHandler("J4RVIS");
    client2 = new TestClientHandler("David");
  }

  /**
   * Tests adding a member to the chat room.
   */
  @Test
  public void testAddMember() {
    chatRoom.addMember(client1);
    List<String> members = chatRoom.listMembers();
    assertTrue(members.contains("J4RVIS"), "J4RVIS should be in the chat room");
  }

  /**
   * Tests removing a member from the chat room.
   */
  @Test
  public void testRemoveMember() {
    chatRoom.addMember(client1);
    chatRoom.removeMember(client1);
    List<String> members = chatRoom.listMembers();
    assertFalse(members.contains("J4RVIS"), "J4RVIS should not be in the chat room after removal");
  }

  /**
   * Tests broadcasting a message to chat room members.
   */
  @Test
  public void testBroadcastMessage() {
    chatRoom.addMember(client1);
    chatRoom.addMember(client2);
    client1.clearLastMessage();
    client2.clearLastMessage();

    chatRoom.broadcastMessage("Hello, everyone!", client1);

    assertEquals("Hello, everyone!", client2.getLastMessage(), "Client2 should receive the broadcast message");
    assertNull(client1.getLastMessage(), "Client1 should not receive their own message");
  }

  /**
   * Tests broadcasting to an empty chat room.
   */
  @Test
  public void testBroadcastMessageWithNoMembers() {
    chatRoom.broadcastMessage("Hello!", null);
  }

  /**
   * Tests adding a null member throws exception.
   */
  @Test
  public void testAddNullMember() {
    assertThrows(NullPointerException.class, () -> {
      chatRoom.addMember(null);
    }, "Adding null member should throw NullPointerException");
  }

  /**
   * Tests removing a member that was never added.
   */
  @Test
  public void testRemoveNonMember() {
    chatRoom.addMember(client1);
    chatRoom.removeMember(client2);
    List<String> members = chatRoom.listMembers();
    assertTrue(members.contains("J4RVIS"), "J4RVIS should still be in the chat room");
    assertFalse(members.contains("David"), "David was never in the chat room");
  }

  /**
   * Tests removing a null member throws exception.
   */
  @Test
  public void testRemoveNullMember() {
    assertThrows(NullPointerException.class, () -> {
      chatRoom.removeMember(null);
    }, "Removing null member should throw NullPointerException");
  }

  /**
   * Tests listing members of an empty chat room.
   */
  @Test
  public void testListMembersWhenEmpty() {
    List<String> members = chatRoom.listMembers();
    assertTrue(members.isEmpty(), "Chat room should have no members");
  }

  /**
   * Tests adding the same member multiple times.
   */
  @Test
  public void testAddSameMemberMultipleTimes() {
    chatRoom.addMember(client1);
    chatRoom.addMember(client1);
    List<String> members = chatRoom.listMembers();
    assertEquals(2, members.size(), "Chat room should have duplicate entries when adding the same member twice");
  }

  /**
   * Tests access control for private chat rooms.
   */
  @Test
  public void testPrivateChatRoomAccess() {
    ChatRoom privateChatRoom = new ChatRoom("PrivateRoom", 12346, true, "");
    privateChatRoom.getAllowedUsers().add("J4RVIS");

    privateChatRoom.addMember(client1);
    List<String> members = privateChatRoom.listMembers();
    assertTrue(members.contains("J4RVIS"), "J4RVIS should be able to join the private chat room");

    privateChatRoom.addMember(client2);
    members = privateChatRoom.listMembers();
    assertFalse(members.contains("David"), "David should not be able to join the private chat room");
    assertEquals("You are not allowed to join this private chatroom.", client2.getLastMessage(),
        "David should be notified that he is not allowed to join");
  }

  /**
   * Tests chat room password functionality.
   */
  @Test
  public void testChatRoomPassword() {
    assertEquals("password123", chatRoom.getPassword(), "Chat room password should match");
  }

  /**
   * Tests allowed users list in public chat rooms.
   */
  @Test
  public void testSetAllowedUsersInPublicChatRoom() {
    chatRoom.getAllowedUsers().add("J4RVIS");
    chatRoom.addMember(client2);
    List<String> members = chatRoom.listMembers();
    assertTrue(members.contains("David"), "David should be able to join the public chat room");
  }

  /**
   * Tests getting chat room name.
   */
  @Test
  public void testGetName() {
    assertEquals("TestRoom", chatRoom.getName(), "Chat room name should match");
  }

  /**
   * Tests getting chat room port.
   */
  @Test
  public void testGetPort() {
    assertEquals(12345, chatRoom.getPort(), "Chat room port should match");
  }

  /**
   * Tests checking private chat status.
   */
  @Test
  public void testIsPrivateChat() {
    assertFalse(chatRoom.isPrivateChat(), "Chat room should not be private");
  }

  /**
   * Tests allowed users list in private chat rooms.
   */
  @Test
  public void testAllowedUsersInPrivateChat() {
    ChatRoom privateChatRoom = new ChatRoom("SecretRoom", 12347, true, "secret");
    privateChatRoom.getAllowedUsers().add("Alice");
    assertTrue(privateChatRoom.getAllowedUsers().contains("Alice"), "Allowed users should include Alice");
    assertFalse(privateChatRoom.getAllowedUsers().contains("Bob"), "Allowed users should not include Bob");
  }

  /**
   * Tests joining private chat without permission.
   */
  @Test
  public void testJoinPrivateChatWithoutPermission() {
    ChatRoom privateChatRoom = new ChatRoom("SecretRoom", 12347, true, "secret");
    privateChatRoom.getAllowedUsers().add("Alice");

    privateChatRoom.addMember(client2);
    List<String> members = privateChatRoom.listMembers();
    assertFalse(members.contains("David"), "David should not be able to join without permission");
    assertEquals("You are not allowed to join this private chatroom.", client2.getLastMessage(),
        "David should be notified about lack of permission");
  }

  /**
   * Tests broadcasting from null sender.
   */
  @Test
  public void testBroadcastMessageFromNullSender() {
    chatRoom.addMember(client1);
    chatRoom.broadcastMessage("System maintenance scheduled.", null);
    assertEquals("System maintenance scheduled.", client1.getLastMessage(),
        "Client1 should receive the broadcast message from null sender");
  }

  /**
   * Tests removing member from empty chat room.
   */
  @Test
  public void testRemoveMemberFromEmptyChatRoom() {
    chatRoom.removeMember(client1);
  }

  /**
   * Tests getting allowed users in public chat.
   */
  @Test
  public void testGetAllowedUsersInPublicChat() {
    assertNotNull(chatRoom.getAllowedUsers(), "Allowed users set should not be null");
    assertTrue(chatRoom.getAllowedUsers().isEmpty(), "Allowed users set should be empty for public chat");
  }

  /**
   * Tests broadcasting null message throws exception.
   */
  @Test
  public void testNullMessageBroadcast() {
    chatRoom.addMember(client1);
    assertThrows(NullPointerException.class, () -> {
      chatRoom.broadcastMessage(null, client1);
    }, "Broadcasting null message should throw NullPointerException");
  }
}
