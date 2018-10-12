package com.makesailing.neo.queue.consumer;

public class MessageHandler {

    /**
     *
     没有设置默认的处理方法的时候，方法名是handleMessage
     */
    public void handleMessage(byte[] message){
        System.out.println("--------- handleMessage -------------");
        System.out.println(new String(message));
    }

    /**
     *
     通过设置setDefaultListenerMethod时候指定的方法名
     */
    public void onMessage(byte[] message){
        System.out.println("--------- onMessage -------------");
        System.out.println(new String(message));
    }

    /**
     *
     以下指定不同的队列不同的处理方法名
     */
    public void onInfo(byte[] message){
        System.out.println("--------- onInfo -------------");
        System.out.println(new String(message));
    }

    public void onWarn(byte[] message){
        System.out.println("--------- onWarn -------------");
        System.out.println(new String(message));
    }

    public void onError(byte[] message){
        System.out.println("--------- onError -------------");
        System.out.println(new String(message));
    }

}
