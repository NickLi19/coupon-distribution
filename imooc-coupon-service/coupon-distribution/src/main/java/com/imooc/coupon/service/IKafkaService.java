package com.imooc.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface IKafkaService {
    void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record);
}
