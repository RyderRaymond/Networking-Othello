# VIMN'T Othello

## Overview

VIMN'T Othello is an Othello game that allows players to compete against an AI opponent over a network. This project is implemented in Java and requires a Java Development Kit (JDK) to compile and run.

---

## Prerequisites

1. **Java Development Kit (JDK)**: Ensure JDK 8 or later is installed on your system.
   - You can download it from [Oracle's website](https://www.oracle.com/java/technologies/javase-downloads.html) or use OpenJDK.

2. **Command Line/Terminal**: Use a terminal or an IDE to compile and run the program.

---

## How to Use

1. **Compile and run Server.java,** it will ask you to enter the port on which to listen. Enter an open port.

2. **To play against the server,** compile and run Client.java. It will ask you which host to connect to. If you are running the client on the same computer as the server, type localhost, if not, type the IP address of the computer that the server is being run on. Then, when it prompts you to enter port the server is listening on, enter the port that the server is listening on.

3. **Proceed to play the game against the AI.**

Note: To swap the AI to easy mode, comment out line 111 of Server.java and uncomment 110.
