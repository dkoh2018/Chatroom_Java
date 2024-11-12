# Chatroom Java Project

This is a simple, command-line chat application I built for our Data Structures class project. It lets you chat with others in real-time, create your own chat rooms, and even have private conversations. I'm excited to share how I used different data structures to make this app work!

## Table of Contents

- [Features](#features)
- [How to Run the App](#how-to-run-the-app)
- [How to Use the Chat App](#how-to-use-the-chat-app)
- [Data Structures Used](#data-structures-used)
- [Notes](#notes)
- [Conclusion](#conclusion)

## Features

- **User Registration**: Set your username upon starting the app.
- **Public and Private Chat Rooms**: Create public chat rooms with passwords or initiate private, invite-only chats.
- **Real-Time Messaging**: Engage in live conversations with peers in the same room.
- **Private Chats**: Start one-on-one conversations with any online user.
- **Online Users List**: View who else is online and available to chat.
- **Multi-User Support**: The server can handle multiple clients concurrently.

## How to Run the App

Before getting started, ensure that you have Java (JDK 17 or higher) and Maven installed on your machine.

1. **Clone the Repository**

   Download the project to your local machine:

   ```bash
   git clone https://github.com/dkoh2018/Chatroom_Java.git
   ```

2. **Navigate to the Project Directory**

   Move into the project folder:

   ```bash
   cd Chatroom_Java
   ```

3. **Build the Project**

   Use Maven to build the project:

   ```bash
   mvn clean package
   ```

   This command compiles the code and packages it into a JAR file located in the `target` directory.

4. **Run the Application**

   Start the chat application by running the JAR file:

   ```bash
   java -jar target/chatapplication-1.0-SNAPSHOT.jar
   ```

   If no server is running, the application will start one automatically; otherwise, it will connect as a client.

## How to Use the Chat App

1. **Enter Your Username**

   Upon starting the app, you'll be prompted to enter a username. This will be your display name in the chat.

2. **Main Menu**

   After entering your username, the main menu will appear:

   ```
   ----------------------------
             Main Menu
   ----------------------------
    1. Create a new chatroom
    2. Join an existing chatroom
    3. List online users
    4. Start a private chat with a user
   /exit - Exit the application
   ----------------------------
   Enter your choice:
   ```

3. **Creating a Chat Room**

   - Select option `1` to create a new chat room.
   - Provide a name for your chat room when prompted.
   - Set a password for your chat room.
   - The chat room will be created, and you will receive its unique ID.

4. **Joining a Chat Room**

   - Select option `2` to join an existing chat room.
   - A list of available chat rooms will be displayed.
   - Enter the chat room ID or name when prompted.
   - If the chat room is password-protected, enter the password when asked.

5. **Chatting**

   - Inside a chat room, type your messages and press **Enter** to send.
   - Use `/exit` to leave the chat room and return to the main menu.

6. **Listing Online Users**

   - Select option `3` to view the list of online users.

7. **Starting a Private Chat**

   - Select option `4` to initiate a private chat.
   - Enter the username of the person you wish to chat with.
   - An invitation will be sent to them for a private chat room.
   - They can accept or decline your invitation.
   - If accepted, both of you will enter a private chat room.

8. **Accepting or Declining Invitations**

   - If you receive an invitation for a private chat, you can accept it by typing `/accept username` or decline it by typing `/decline username` at any time.

## Data Structures Used

Since this is a Data Structures class project, I want to highlight how I utilized different data structures to build the application.

### 1. **HashMap**

- **Purpose**: To store and quickly access data using unique keys.
- **Usage**:

  - **Chat Rooms Management**:

    ```java
    Map<String, ChatRoom> chatRooms = new HashMap<>();
    ```

    - **Description**: Maintains a mapping of chat room IDs to `ChatRoom` objects, enabling quick retrieval of a chat room using its unique ID.

  - **Chat Room IDs Mapping**:

    ```java
    Map<ChatRoom, String> chatRoomIDs = new HashMap<>();
    ```

    - **Description**: Allows retrieval of a chat room's ID when only the `ChatRoom` object is available, useful for displaying the ID to users.

  - **Online Users Tracking**:

    ```java
    Map<String, ClientHandler> onlineUsers = new HashMap<>();
    ```

    - **Description**: Keeps track of online users by mapping usernames to their respective `ClientHandler` instances, facilitating message routing.

  - **Pending Invitations**:

    ```java
    Map<String, String> pendingInvitations = new HashMap<>();
    ```

    - **Description**: Stores pending chat invitations, mapping the inviter's username to the chat room name.

### 2. **HashSet**

- **Purpose**: To store a collection of unique items with fast lookup times.
- **Usage**:

  - **Allowed Users in Private Chat Rooms**:

    ```java
    Set<String> allowedUsers = new HashSet<>();
    ```

    - **Description**: For private chat rooms, this set maintains the usernames of users who are permitted to join, ensuring no duplicates and quick membership checks.

### 3. **ArrayList**

- **Purpose**: To maintain an ordered list of items where duplicates are allowed.
- **Usage**:

  - **Chat Room Members List**:

    ```java
    List<ClientHandler> members = new ArrayList<>();
    ```

    - **Description**: Stores the list of clients currently in a chat room, allowing for message broadcasting to all members.

### 4. **Random Number Generator**

- **Purpose**: To generate unique identifiers.
- **Usage**:

  - **Generating Unique Chat Room IDs**:

    ```java
    Random random = new Random();
    ```

    - **Description**: Generates unique 6-digit IDs for chat rooms, ensuring each room can be uniquely identified.

### 5. **Synchronized Methods**

- **Purpose**: To manage concurrent access to shared resources in a multi-threaded environment.
- **Usage**:

  - **Managing Chat Room Members**:

    - Methods like `addMember`, `removeMember`, and `broadcastMessage` in the `ChatRoom` class are synchronized to prevent concurrent modification issues when users join or leave.

  - **Server Data Access**:

    - In the `ChatServer` class, methods that modify or access shared data structures are synchronized to maintain data consistency during concurrent operations.

### 6. **Message Handling System**

- **Purpose**: To manage message storage, delivery, and processing.
- **Usage**:

  - **Message Queue**:

    ```java
    List<ClientHandler> members = new ArrayList<>();
    ```

    - **Description**: This list stores the current members of a chat room, enabling O(n) broadcasting of messages to all participants. When a user sends a message, it is broadcasted to every member of the chat room.

  - **Message Broadcasting**:

    ```java
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
    ```

    - **Description**: This synchronized method ensures that messages are delivered to all room members except the sender, in a thread-safe manner. This prevents concurrent modification issues during broadcasts and ensures consistency in message delivery. Broadcasting messages to all members takes O(n) time, where n is the number of members in the chat room.

### 7. **Defensive Copies**

- **Purpose**: To prevent external modification of internal data structures.
- **Usage**:

  - **Returning Data to Clients**:

    - Methods like `listChatRooms` and `getOnlineUsers` return new copies of the data structures to prevent clients from modifying the server's internal state.

## Notes

- **Integrated Server and Client**: The application can act as both a server and a client. If no server is detected when the app starts, it will automatically start one.
- **Testing**: To test the chat functionality, you can open multiple instances of the application on your computer.
- **CLI Interface**: The application runs entirely in the terminal.
- **Multithreading**: Each client connection is handled in its own thread, allowing multiple users to chat simultaneously without interference.
