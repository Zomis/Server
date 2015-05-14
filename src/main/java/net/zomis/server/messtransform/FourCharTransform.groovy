package net.zomis.server.messtransform

import groovy.transform.TypeChecked
import groovy.util.logging.Log4j
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

import java.util.function.Consumer

@Log4j('logger')
@TypeChecked
class FourCharTransform implements MessageTransformer {

    private final Map<String, Class<? extends Message>> messages = new HashMap<>();

    @Override
    void registerClass(Class<? extends Message> clazz) {
        FourChar annot = clazz.getAnnotation(FourChar)
        messages.put(annot.value(), clazz)
    }

    @Override
    void transform(Message message, Sender<byte[]> byteSender, Sender<String> stringSender) {
        if (stringSender) {
            def annot = message.class.getAnnotation(FourChar)
            Closure<String> closure = (Closure<String>) annot.outgoingStr().newInstance(null, null)
            String data = closure.call(message)
            stringSender.send(data)
        } else {
            throw new IllegalArgumentException('expected a string sender')
        }
    }

    @Override
    void read(InputStream stream, byte[] bytes, Consumer<Message> handler) {
        char[] readBuffer = new char[4096];
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        String data;
        try {
            int bytesRead;
            while ((bytesRead = reader.read(readBuffer)) != -1) {
                data = new String(readBuffer, 0, bytesRead);
                logger.info("Read: $data");

                String[] datas = data.split("" + (char) 0);
                for (String mess : datas) {
                    if (mess.trim().isEmpty()) {
                        continue;
                    }

                    handler.accept(stringToMessage(mess));
                }
            }
            logger.info("Socket Communication no more bytes to read for " + this.toString());
        } catch (IOException ioe) {
            logger.warn("Socket " + this + " exception", ioe);
        }
    }

    Message stringToMessage(String str) {
        String[] array = str.split(' ')
        String id = array[0]
        Class<?> clazz = messages.get(id)
        if (!clazz) {
            return null
        }
        FourChar annot = clazz.getAnnotation(FourChar)
        Closure<Message> closure = (Closure<Message>) annot.incomingStr().newInstance(null, null)
        Message mess = closure.call(array)
        logger.info "Transformed into $mess"
        mess
    }
}
