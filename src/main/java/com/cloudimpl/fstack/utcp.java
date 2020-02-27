package com.cloudimpl.fstack;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nuwansa
 */
public class utcp {

    static {
        try {
            System.loadLibrary("utcp");
        } catch (Throwable t) {
            System.err.println("Failed to load utcp.so. Make sure you run "
                    + "the JVM with -Djava.library.path=<path to libutcp.so>.");
            t.printStackTrace();
        }
    }

    public static native int jff_init(String[] argv);

    public static native int jff_socket();

    public static native int jff_connect(int sockfd,String address, int port);

    public static native int jff_bind(int sockfd, int port);

    public static native int jff_listen(int sockfd);

    public static native int jff_close(int sockfd);

    public static native int jff_accept(int sockfd);

    public static native int jff_write(int sockfd, byte[] buffer,int len);

    public static native int jff_read(int socketfd, byte[] buffer);

    public static native int jff_set(int handlerId, int socketfd, short eventFilter, short action);

    public static native int jff_kevent(int handlerId);

    public static native int jff_evt_fd(int handlerId, int index);

    public static native int jff_evt_flags(int handlerId, int index);

    public static native int jff_evt_filter(int handlerId, int index);

    public static native int jff_evt_data(int handlerId, int index);

    public static native void jff_run(Runnable runner);

}
