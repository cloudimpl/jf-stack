/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.net.example;

import com.cloudimpl.net.TcpEngine;

/**
 *
 * @author nuwansa
 */
public class PingPong implements TcpEngine.ServerSocket.ServerListener, TcpEngine.ClientSocket.ClientListener {

    @Override
    public void onConnect(TcpEngine.ClientSocket client) {
        System.out.println("client connected . " + client.getSocket());
        client.write("ping".getBytes(), "ping".getBytes().length);
    }

    @Override
    public void onClientData(TcpEngine.ServerSocket socket, TcpEngine.ClientSocket client, byte[] data, int len) {
        System.err.println("data received : " + new String(data));
    }

    @Override
    public void onData(TcpEngine.ClientSocket client, byte[] data, int len) {
        client.write(data, len);
    }

    public static int offset = 0;
    public static String[] args;

    public static void main(String[] args) {
        PingPong.args = args;
        TcpEngine engine = new TcpEngine(args);
        PingPong pingPong = new PingPong();
        int i = 0;
        while (i < args.length) {
            i++;
            offset = i;
            if (args[0].equals("--")) {
                break;
            }
        }
//	offset += 2;
        if (getArg(0).equals("s")) {
            System.out.println("starting server");
            engine.createServer(Integer.valueOf(getArg(1)), pingPong);
        } else {
            System.out.println("starting client");
            engine.createClient(getArg(1), Integer.valueOf(getArg(2)), pingPong);
        }
        engine.start();
    }

    private static String getArg(int index) {
        return args[offset + index];
    }
}
