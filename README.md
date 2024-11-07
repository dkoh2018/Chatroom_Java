# ChatRoom Java Project

Hey everyone!

This is a simple chat application I built for our Java class project. It's a command-line program that lets you chat with others in real-time. You can create chat rooms, join existing ones, and even have private conversations.

## Features

- **User Registration**: Set your username when you start the app.
- **Public and Private Chat Rooms**: Create chat rooms that are open to everyone or invite-only.
- **Real-Time Messaging**: Chat live with classmates in the same room.
- **Private Chats**: Start a one-on-one conversation with any online user.
- **Online Users List**: See who else is online and available to chat.

## Prerequisites

Make sure you have the following installed:

- **Java Development Kit (JDK) 17 or higher**
- **Maven 3.x**

## How to Run the App

1. **Clone the Repository**

   First, clone the project repository to your local machine:

   ```bash
   git clone https://github.com/yourusername/ChatRoom_Java.git
   ```

2. **Navigate to the Project Directory**

   Go into the project folder:

   ```bash
   cd ChatRoom_Java
   ```

3. **Build the Project**

   Use Maven to build the project:

   ```bash
   mvn clean package
   ```

   This will compile the code and package it into a JAR file.

4. **Run the Application**

   Start the chat application by running the JAR file:

   ```bash
   java -jar target/chatapplication-1.0-SNAPSHOT.jar
   ```

   If no server is running, it will start one automatically. Otherwise, it will connect as a client.

## How to Use the Chat App

1. **Enter Your Username**

   When you start the app, you'll be prompted to enter a username. This is how others will see you in the chat.

2. **Main Menu**

   After entering your username, you'll see the main menu:

   ```
   ----------------------------
             Main Menu
   ----------------------------
    1. Create a new chatroom
    2. List available chatrooms
    3. Join an existing chatroom
    4. List online users
    5. Start a private chat with a user
   /exit - Leave the chatroom
   ----------------------------
   Enter your choice:
   ```

3. **Creating a Chat Room**

   - Choose option `1` to create a new chat room.
   - Enter a name for your chat room.
   - Decide if you want it to be private (`yes` or `no`).
     - If private, list the usernames of the people you want to invite.
     - If public, just proceed without adding users.

4. **Joining a Chat Room**

   - Choose option `2` to see a list of available chat rooms.
   - Choose option `3` to join a chat room.
   - Enter the chat room ID when prompted.

5. **Chatting**

   - Once inside a chat room, just type your messages and press Enter to send.
   - Use `/exit` to leave the chat room and return to the main menu.

6. **Private Chats**

   - Choose option `5` to start a private chat.
   - Enter the username of the person you want to chat with.
   - They will receive an invitation to join your private chat room.

## Notes

- **Server and Client in One**: The app can act as both server and client. If a server isn't running when you start the app, it will start one for you.
- **Multiple Instances**: You can open multiple instances of the app to simulate different users (helpful for testing).
- **No GUI**: This is a command-line application because we're going back to the 80's baby.

Happy chatting!
