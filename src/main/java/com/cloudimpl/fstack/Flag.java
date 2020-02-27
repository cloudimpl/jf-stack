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
public class Flag {
      public static native short EV_EOF();
      public static native short EV_ADD();
      public static native short EV_DELETE();

      public static native short EVFILT_READ();
      public static native short EVFILT_WRITE();
}
