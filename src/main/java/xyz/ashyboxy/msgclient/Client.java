package xyz.ashyboxy.msgclient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class Client {
    private Logger logger;
    private WebSocket ws;
    private Listener listener;
    private String key = "this is a test key\n";

    public Client(URI server, Logger logger) throws ExecutionException, InterruptedException {
        this.logger = logger;
        listener = new Listener();
        ws = HttpClient.newHttpClient().newWebSocketBuilder().connectTimeout(Duration.ofSeconds(2))
                .buildAsync(server, listener).get();
    }

    private void handleMessage(Message message) {
        logger.log("Got message: " + message);
    }

    public void sendMessage(Message message) {
        ws.sendBinary(Utils.encodeMessage(message, key), true);
    }

    private class Listener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            logger.log("Connection opened");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            logger.log("Got text: " + data);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            if (!Utils.verifyMessage(data, key)) logger.log("Got invalid message");
            else handleMessage(Utils.getMessage(data));

            return WebSocket.Listener.super.onBinary(webSocket, data, last);
        }
    }
}
