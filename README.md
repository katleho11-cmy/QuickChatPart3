
# QuickChatPart3

Programming 1A — PROG5121
Portfolio of Evidence: Part 3

## Project Description

QuickChatPart3 is a Java messaging application developed using
Apache NetBeans and Maven.

The application includes registration, login, message creation,
message storage, searching, deleting, and reporting.

## Application Features

### Part 1
- User registration
- Username and password validation
- South African cellphone number validation
- User login

### Part 2
- Send messages
- Disregard messages
- Store messages
- Generate message IDs and message hashes
- Validate recipients and message lengths

### Part 3
- Store messages using ArrayLists
- Read and write messages using JSON
- Display sender and recipient details
- Find the longest message
- Search messages by ID
- Search messages by recipient
- Delete stored messages using message hashes
- Display a full message report

## Technologies

- Java 17
- Apache NetBeans
- Maven
- Gson
- JUnit 5
- GitHub Actions

## How to Run

1. Open the project in Apache NetBeans.
2. Allow Maven to download the required dependencies.
3. Run QuickChatPart3.java.
4. Register and log in.
5. Use the main menu to manage messages.

## Testing

The project contains 14 JUnit tests for the Part 3 features.

Run the tests in NetBeans using:
Right-click project > Test

Automated tests also run through GitHub Actions
when changes are pushed to the master branch.

## JSON Storage

The application creates a local messages.json file
to store messages.

This file is excluded from GitHub using .gitignore.
