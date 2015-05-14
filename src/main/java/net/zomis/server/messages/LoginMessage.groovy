package net.zomis.server.messages

import groovy.transform.ToString

@ToString
@FourChar(value = 'USER', incomingStr = {mess -> new LoginMessage(client: mess[1], username: mess[2], password: mess[3]) })
public class LoginMessage implements Message {
    String username;
    String password;
    String client;
}
