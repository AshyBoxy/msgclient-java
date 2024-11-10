package xyz.ashyboxy.msgclient;

import java.net.URI;

public class Main {
    public static Logger logger = new MainLogger();

    public static void main(String[] args) throws Exception {
        Client client = new Client(URI.create("ws://localhost:8071"), logger);
        Thread.sleep(1000);
        client.sendMessage(new Message("Java Test", "java test message", "javatest", "Java Test"));
        Thread.currentThread().join();
    }

    private static class MainLogger implements Logger {
        @Override
        public void log(String msg) {
            System.out.println(msg);
        }
    }
}