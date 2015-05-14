package net.zomis.server.model;

import net.zomis.server.clients.ClientIO;
import net.zomis.server.messages.Message;

/**
 * Created by Simon on 5/14/2015.
 */
public interface MessageHandler<T extends Message> {

    void handle(T message, ClientIO client);

}
