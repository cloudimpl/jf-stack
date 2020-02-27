package com.cloudimpl.net;

import com.cloudimpl.fstack.Flag;
import com.cloudimpl.fstack.utcp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nuwansa
 */
public class TcpEngine implements Runnable {

    public enum SockStatus {
        PENDING, CONNECTED
    }

    public enum SockType {
        SERVER, CLIENT
    }
    private final int handlerId;
    private final Map<Integer, Socket> mapSocks = new HashMap<>();
    private final byte[] recvBuffer = new byte[1024 * 1024];
    private final ArrayList<Timer> timers = new ArrayList<>();

    public TcpEngine(String args[]) {
        handlerId = utcp.jff_init(args);
    }

    public Timer createTimer(long interval, Timer.Listener listener) {
        Timer t = new Timer(listener, System.nanoTime() + interval, interval);
        timers.add(t);
        return t;
    }

    public ServerSocket createServer(int port, ServerSocket.ServerListener listener) {
        int sockfd = utcp.jff_socket();
        int ret = utcp.jff_bind(sockfd, port);
        if (ret < 0) {
            throw new RuntimeException("bind failed");
        }
        ret = utcp.jff_listen(sockfd);
        if (ret < 0) {
            throw new RuntimeException("listen failed");
        }

        utcp.jff_set(handlerId, sockfd, Flag.EVFILT_READ(), Flag.EV_ADD());
        ServerSocket socket = new ServerSocket(sockfd, listener);
        mapSocks.put(socket.getSocket(), socket);
        return socket;
    }

    public ClientSocket createClient(String addr, int port, ClientSocket.ClientListener listener) {
        int sockfd = utcp.jff_socket();
        utcp.jff_connect(sockfd, addr, port);
        utcp.jff_set(handlerId, sockfd, Flag.EVFILT_WRITE(), Flag.EV_ADD());
        ClientSocket client = new ClientSocket(sockfd, null, SockStatus.PENDING, listener);
        mapSocks.put(client.getSocket(), client);
        return client;
    }

    private Socket get(int socket) {
        return mapSocks.get(socket);
    }

    public void start() {
        utcp.jff_run(this);
    }

    @Override
    public void run() {
        int events = utcp.jff_kevent(handlerId);
        //      if(events > 0)
//            System.out.println("event received . "+events);
        int i = 0;
        while (i < events) {
            int sock = utcp.jff_evt_fd(handlerId, i);
            Socket s = get(sock);
            switch (s.type) {
                case CLIENT: {
                    onClientEvent(i, (ClientSocket) s);
                    break;
                }
                case SERVER: {
                    onServerEvent(i, (ServerSocket) s);
                    break;
                }
            }
            i++;
        }
        handlerTimers();
    }

    private void handlerTimers() {
        long time = System.nanoTime();
        boolean isCancel = false;
        for (int i = 0; i < timers.size(); i++) {
            timers.get(i).run(time);
            isCancel |= timers.get(i).isCancel();
        }

    }

    private void onClientEvent(int index, ClientSocket socket) {
        try {
            if ((utcp.jff_evt_flags(handlerId, index) & Flag.EV_EOF()) > 0) {
                onCloseEvent(socket);
            } else if (utcp.jff_evt_filter(handlerId, index) == Flag.EVFILT_READ()) {
                onReadEvent(socket);
            } else if (utcp.jff_evt_filter(handlerId, index) == Flag.EVFILT_WRITE()) {
                if (socket.getStatus() == SockStatus.PENDING) {
                    socket.setStatus(SockStatus.CONNECTED);
                    utcp.jff_set(handlerId, socket.getSocket(), Flag.EVFILT_WRITE(), Flag.EV_DELETE());
                    utcp.jff_set(handlerId, socket.getSocket(), Flag.EVFILT_READ(), Flag.EV_ADD());
                    socket.getListener().onConnect(socket);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onServerEvent(int index, ServerSocket server) {
        try {
            if (utcp.jff_evt_filter(handlerId, index) == Flag.EVFILT_READ()) {
                int nclientfd = utcp.jff_accept(server.getSocket());
                ClientSocket client = new ClientSocket(nclientfd, server, SockStatus.CONNECTED, null);
                utcp.jff_set(handlerId, client.getSocket(), Flag.EVFILT_READ(), Flag.EV_ADD());
                mapSocks.put(client.getSocket(), client);
                server.getListener().onClientConnect(server, client);
            } else {
                System.out.println("unknown server event . " + utcp.jff_evt_filter(handlerId, index));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void onReadEvent(ClientSocket sock) {
        int recv = utcp.jff_read(sock.getSocket(), recvBuffer);
        if (recv <= 0) {
            return;
        }
        if (sock.getServer() != null) {
            sock.getServer().getListener().onClientData(sock.getServer(), sock, recvBuffer, recv);
        } else {
            sock.getListener().onData(sock, recvBuffer, recv);
        }

    }

    private void onCloseEvent(ClientSocket sock) {
        utcp.jff_close(sock.getSocket());
        mapSocks.remove(sock.getSocket());
        if (sock.getServer() != null) {
            sock.getServer().getListener().onClientDisconnect(sock.getServer(), sock);
        } else {
            sock.getListener().onDisconnect(sock);
        }
    }

    public static class Socket {

        private final int socket;
        private final SockType type;

        public Socket(int socket, SockType type) {
            this.socket = socket;
            this.type = type;
        }

        public int getSocket() {
            return socket;
        }

        public SockType getType() {
            return type;
        }

    }

    public static final class ClientSocket extends Socket {

        private SockStatus status;
        private final ClientListener listener;
        private final ServerSocket server;

        public ClientSocket(int socket, ServerSocket server, SockStatus status, ClientListener listener) {
            super(socket, SockType.CLIENT);
            this.status = status;
            this.server = server;
            this.listener = listener;
        }

        public int write(byte[] data, int len) {
            if (status != SockStatus.CONNECTED) {
                throw new RuntimeException("socket not connected : status : " + status);
            }
            return utcp.jff_write(getSocket(), data, len);
        }

        private SockStatus getStatus() {
            return status;
        }

        public ServerSocket getServer() {
            return server;
        }

        private void setStatus(SockStatus status) {
            this.status = status;
        }

        private ClientListener getListener() {
            return listener;
        }

        public static interface ClientListener {

            default void onConnect(ClientSocket client) {
                System.out.println("client connected . " + client.getSocket());
            }

            default void onDisconnect(ClientSocket client) {
                System.out.println("client disconnected . " + client.getSocket());
            }

            void onData(ClientSocket client, byte[] data, int len);
        }
    }

    public static final class ServerSocket extends Socket {

        private final ServerListener listener;

        public ServerSocket(int socket, ServerListener listener) {
            super(socket, SockType.SERVER);
            this.listener = listener;
        }

        private ServerListener getListener() {
            return listener;
        }

        public static interface ServerListener {

            default void onClientConnect(ServerSocket server, ClientSocket client) {
                System.out.println("serverside client connected . " + client.getSocket());
            }

            default void onClientDisconnect(ServerSocket server, ClientSocket client) {
                System.out.println("serverside client disconnected . " + client.getSocket());
            }

            ;

            void onClientData(ServerSocket socket, ClientSocket client, byte[] data, int len);
        }
    }
}
