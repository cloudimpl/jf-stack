/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.net.example;

import com.cloudimpl.net.TcpEngine;
import com.cloudimpl.net.Timer;

/**
 *
 * @author nuwansa
 */
public class PingPong implements TcpEngine.ServerSocket.ServerListener, TcpEngine.ClientSocket.ClientListener,Timer.Listener {

    private Timer timer;
    private TcpEngine.ClientSocket client;
    private final TcpEngine engine;

    public PingPong(TcpEngine engine) {
        
        this.engine = engine;
    }
    
     @Override
    public void OnTimer(Timer timer) {
        if(this.timer == timer)
            client.write("ping".getBytes(), "ping".getBytes().length);
    }
   

    @Override
    public void onConnect(TcpEngine.ClientSocket client) {
        System.out.println("client connected . " + client.getSocket());
        if (timer != null) {
            timer.cancel();
        }
        this.client = client;
        timer = engine.createTimer(1000000000, this);

    }

    @Override
    public void onDisconnect(TcpEngine.ClientSocket client) {
        System.out.println("client disconnected . " + client.getSocket());
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        this.client = null;
    }

    @Override
    public void onClientData(TcpEngine.ServerSocket socket, TcpEngine.ClientSocket client, byte[] data, int len) {
        System.err.println("data received : " + new String(data));
        client.write("pong".getBytes(), "pong".getBytes().length);
    }

    @Override
    public void onData(TcpEngine.ClientSocket client, byte[] data, int len) {
        //client.write(data, len);
        System.err.println("data received : " + new String(data));
    }

    public static int offset = 0;
    public static String[] args;

    public static void main(String[] args) {
        PingPong.args = args;
        String[] args2 = new String[0];
        TcpEngine engine = new TcpEngine(args2);
        PingPong pingPong = new PingPong(engine);
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
