/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.net;

/**
 *
 * @author nuwansa
 */
public class Timer {
    
    private final Listener listener;
    private  long next = 0;
    private final long interval;
    private boolean cancel;
    public Timer(Listener listener,long next,long interval) {
        this.listener = listener;
        this.next = next;
        this.interval = interval;
        this.cancel = false;
    }
    
    public void cancel()
    {
        this.cancel = true;
    }
    
    public boolean isCancel()
    {
        return this.cancel;
    }
    
    public void run(long time)
    {
        if(time >= next)
        {
            next = next + interval;
            listener.OnTimer(this);
        }
    }
    
    public static  interface Listener 
    {
        void OnTimer(Timer timer);
    }
}
