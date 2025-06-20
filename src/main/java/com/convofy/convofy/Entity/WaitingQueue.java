package com.convofy.convofy.Entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;



@Getter
public class WaitingQueue {
    private ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
    private ArrayList<String> list;
    private WaitingQueue()
    {
        System.out.println("WaitingQueue()");
        list = new ArrayList<>();
    }
    public static WaitingQueue instance;
    public static WaitingQueue getInstance()
    {

        if (instance == null) {
            instance = new WaitingQueue();
        }
        return instance;
    }
    public long count(){
        return list.size();
    }
    public boolean addid(String id){
        list.add(id);
        return true;
    }
    public String removeid(){
        return list.removeFirst();
    }
    public boolean checkid(String id){
        return list.contains(id);
    }
}
