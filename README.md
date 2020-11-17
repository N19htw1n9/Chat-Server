# 🃏 Baccarat Server
This repository serves as the backend server for the [Baccarat Client](https://github.com/ayaanqui/baccarat-client). This repo has two parts the first part is the actual socket server, and the second part is the server GUI to interact with the socket server.

The socket server starts at the port specified by the server GUI. Once the server is started at a specified port, the GUI displays all incomming requests and response from users listening to, and interacting with the port.

## Working with the project

### Clone project

```
git clone https://github.com/ayaanqui/baccarat-server.git
```

### Compile project

```
mvn package
```

### Run program
```
mvn exec:java
```