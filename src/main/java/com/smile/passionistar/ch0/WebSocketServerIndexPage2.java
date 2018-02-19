
package com.smile.passionistar.ch0;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * Generates the demo HTML page which is served at http://localhost:8080/
 */
public final class WebSocketServerIndexPage2 {

    public static ByteBuf getContent(String webSocketLocation) {
        return PooledByteBufAllocator.DEFAULT.directBuffer(256);
    }

    private WebSocketServerIndexPage2() {
        // Unused
    }
}
