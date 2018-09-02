package com.makesailing.neo.provider;

import com.makesailing.neo.utils.ConnectionUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

public class ConfirmSend3 {

    private static final String QUEUE_NAME = "test_queue_confirm1";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        // 获取连接
        Connection connection = ConnectionUtils.getConnection();
        // 从连接开一个通道
        Channel channel = connection.createChannel();
        // 声明一个队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.confirmSelect(); // 开始confirm模式

        // 未确认的消息列表
        SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());

        channel.addConfirmListener(new ConfirmListener() {

            // 有效的应答
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                if (multiple) {
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }
            // 有问题的应答
            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                System.out.println("Nack, SeqNo: " + deliveryTag + ", multiple: " + multiple);
                // TODO 处理未确认的应答
                if (multiple) {
                    confirmSet.headSet(deliveryTag + 1).clear();
                } else {
                    confirmSet.remove(deliveryTag);
                }
            }
        });

        while (true) {
            long nextSeqNo = channel.getNextPublishSeqNo();
            String message = "hello, confirm message ";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            confirmSet.add(nextSeqNo);
        }

    }

}