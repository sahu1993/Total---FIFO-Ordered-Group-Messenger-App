package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by shivamsahu on 3/3/18.
 */

public class Msg implements Comparable{

    int msgNo;
    String msg;
    float seqNo;
    boolean canDeliver;
    int senderPort;



    public Msg(int msgNo, String msg, float seqNo, boolean canDeliver, int senderPort) {

        this.msgNo = msgNo;
        this.msg = msg;
        this.seqNo = seqNo;
        this.canDeliver = canDeliver;
        this.senderPort = senderPort;
    }

    public float getSeqNo() {
        return seqNo;
    }

    public boolean isCanDeliver() {
        return canDeliver;
    }

    public int getMsgNo() {
        return msgNo;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsgNo(int msgNo) {
        this.msgNo = msgNo;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setSeqNo(float seqNo) {
        this.seqNo = seqNo;
    }

    public void setCanDeliver(boolean canDeliver) {
        this.canDeliver = canDeliver;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public void setSenderPort(int senderPort) {
        this.senderPort = senderPort;
    }

    @Override
    public int compareTo(Object obj) {
        float compare = ((Msg) obj).getSeqNo();
        return Float.compare(this.seqNo,compare);
    }

}

