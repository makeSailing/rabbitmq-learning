package com.makesailing.neo.common;

/**
 * 自定义 Body
 * @author jamie.li
 */
public class MyBody {

    private byte[] bodys;

    public MyBody(byte[] bodys){
        this.bodys = bodys;
    }

    @Override
    public String toString() {
        return new String(bodys);
    }
}
