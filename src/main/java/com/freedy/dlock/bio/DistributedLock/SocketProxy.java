package com.freedy.dlock.bio.DistributedLock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/10/8 9:34
 */
public class SocketProxy {
    private final Socket socket;
    private final InputStream is;
    private final OutputStream os;

    public SocketProxy(Socket socket) {
        this.socket = socket;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SocketProxy(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readMessage() {
        List<Byte> bytes = new ArrayList<>();
        try {
            byte b;
            while ((b = (byte) is.read()) != '\n') {
                if (b == -1) throw new RuntimeException("reach the end of the stream");
                bytes.add(b);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] bs = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bs[i] = bytes.get(i);
        }
        return new String(bs);
    }

    public void writeMessage(String msg) {
        try {
            os.write((msg + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SocketAddress getRemoteSocketAddress() {
        return socket.getRemoteSocketAddress();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public void close() {
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
