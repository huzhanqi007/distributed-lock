package com.dzh.microservice.lock.thread;

import com.dzh.microservice.lock.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by herbert on 2018/8/13.
 */
public class SpikeThread  extends Thread {
    @Autowired
    private LockService service;

    public SpikeThread(LockService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.seckill();
    }

    public static void main(String[] args) {
        LockService service = new LockService();
        for (int i = 0; i < 200; i++) {
            SpikeThread spikeThread = new SpikeThread(service);
            spikeThread.start();
        }
    }

}