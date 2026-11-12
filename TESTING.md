
# QuickChat Part 3 - Testing Guide

## Automated Testing

The application uses JUnit 5 and Maven.

To run the tests in Apache NetBeans:

1. Open QuickChatPart3.
2. Right-click the project.
3. Select Test.
4. Check the test results in the Output window.

The project contains 14 Part 3 unit tests.

GitHub Actions also runs the tests automatically
when changes are pushed to the master branch.

## POE Test Data

| Message | Recipient | Message text | Status |
|---|---|---|---|
| 1 | +27834557896 | Did you get the cake? | Sent |
| 2 | +27838884567 | Where are you? You are late! I have asked you to be on time. | Stored |
| 3 | +27834484567 | Yohoooo, I am at your gate. | Disregarded |
| 4 | 0838884567 | It is dinner time ! | Sent |
| 5 | +27838884567 | Ok, I am leaving without you. | Stored |

## Features to Verify

1. Sent messages array contains Messages 1 and 4.
2. Disregarded messages array contains Message 3.
3. Stored messages array contains Messages 2 and 5.
4. Message IDs and hashes are generated.
5. The longest message is Message 2.
6. Searching by message ID returns the correct message.
7. Searching for +27838884567 returns Messages 2 and 5.
8. Deleting by message hash removes the correct stored message.
9. Stored JSON messages can be loaded again.
10. The report displays message details.

## Expected Test Results

JUnit tests: 14
Expected failures: 0
Expected errors: 0

These tests passed locally in Apache NetBeans
on 12 November 2026.
