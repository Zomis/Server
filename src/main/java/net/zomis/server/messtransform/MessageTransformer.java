package net.zomis.server.messtransform;

import net.zomis.server.messages.Message;

import java.io.InputStream;
import java.util.function.Consumer;

public interface MessageTransformer {

    void registerClass(Class<? extends Message> clazz);

    void transform(Message message, Sender<byte[]> byteSender, Sender<String> stringSender);

    void read(InputStream stream, byte[] bytes, Consumer<Message> handler);

}
