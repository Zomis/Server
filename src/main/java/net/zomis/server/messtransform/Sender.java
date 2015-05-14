package net.zomis.server.messtransform;

import java.io.IOException;

/**
 * Created by Simon on 5/12/2015.
 */
public interface Sender<T> {

    void send(T data) throws IOException;

}
