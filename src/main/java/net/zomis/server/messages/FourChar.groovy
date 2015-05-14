package net.zomis.server.messages

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(value = RetentionPolicy.RUNTIME)
public @interface FourChar {
    String value()
    Class incomingStr() default { mess -> throw new UnsupportedOperationException('incomingStr') }
    Class outgoingStr() default { mess -> throw new UnsupportedOperationException('outgoingStr') }
}