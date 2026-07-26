package com.bloodbank.notification.infrastructure.config;

import com.bloodbank.common.events.config.SharedRabbitConfig;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedRabbitConfig.class)
public class NotificationRabbitConfig {

    // Exchanges
    public static final String DONOR_EXCHANGE = "bloodbank.donor.exchange";
    public static final String DONATION_EXCHANGE = "bloodbank.donation.exchange";
    public static final String TRANSACTION_EXCHANGE = "bloodbank.transaction.exchange";
    public static final String MASTER_EXCHANGE = "bloodbank.master.exchange";

    // Queues
    public static final String DONOR_REGISTERED_QUEUE = "notification.donor.registered.queue";
    public static final String DONATION_COMPLETED_QUEUE = "notification.donation.completed.queue";
    public static final String BLOOD_REQUEST_QUEUE = "notification.blood.request.queue";
    public static final String MASTER_LOOKUP_QUEUE = "notification.master.lookup.queue";

    // Dead Letter Exchanges & Queues
    public static final String DONOR_REGISTERED_DLX = "notification.donor.registered.dlx";
    public static final String DONOR_REGISTERED_DLQ = "notification.donor.registered.dlq";

    public static final String DONATION_COMPLETED_DLX = "notification.donation.completed.dlx";
    public static final String DONATION_COMPLETED_DLQ = "notification.donation.completed.dlq";

    public static final String BLOOD_REQUEST_DLX = "notification.blood.request.dlx";
    public static final String BLOOD_REQUEST_DLQ = "notification.blood.request.dlq";

    public static final String MASTER_LOOKUP_DLX = "notification.master.lookup.dlx";
    public static final String MASTER_LOOKUP_DLQ = "notification.master.lookup.dlq";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // --- Exchanges ---
    @Bean
    public TopicExchange donorTopicExchange() {
        return new TopicExchange(DONOR_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange donationTopicExchange() {
        return new TopicExchange(DONATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange transactionTopicExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange masterTopicExchange() {
        return new TopicExchange(MASTER_EXCHANGE, true, false);
    }

    // --- DLX Declarations ---
    @Bean
    public DirectExchange donorRegisteredDlx() {
        return new DirectExchange(DONOR_REGISTERED_DLX, true, false);
    }

    @Bean
    public DirectExchange donationCompletedDlx() {
        return new DirectExchange(DONATION_COMPLETED_DLX, true, false);
    }

    @Bean
    public DirectExchange bloodRequestDlx() {
        return new DirectExchange(BLOOD_REQUEST_DLX, true, false);
    }

    @Bean
    public DirectExchange masterLookupDlx() {
        return new DirectExchange(MASTER_LOOKUP_DLX, true, false);
    }

    // --- DLQ Declarations ---
    @Bean
    public Queue donorRegisteredDlq() {
        return QueueBuilder.durable(DONOR_REGISTERED_DLQ).build();
    }

    @Bean
    public Queue donationCompletedDlq() {
        return QueueBuilder.durable(DONATION_COMPLETED_DLQ).build();
    }

    @Bean
    public Queue bloodRequestDlq() {
        return QueueBuilder.durable(BLOOD_REQUEST_DLQ).build();
    }

    @Bean
    public Queue masterLookupDlq() {
        return QueueBuilder.durable(MASTER_LOOKUP_DLQ).build();
    }

    // --- DLQ Bindings ---
    @Bean
    public Binding donorRegisteredDlqBinding() {
        return BindingBuilder.bind(donorRegisteredDlq()).to(donorRegisteredDlx()).with(DONOR_REGISTERED_DLQ);
    }

    @Bean
    public Binding donationCompletedDlqBinding() {
        return BindingBuilder.bind(donationCompletedDlq()).to(donationCompletedDlx()).with(DONATION_COMPLETED_DLQ);
    }

    @Bean
    public Binding bloodRequestDlqBinding() {
        return BindingBuilder.bind(bloodRequestDlq()).to(bloodRequestDlx()).with(BLOOD_REQUEST_DLQ);
    }

    @Bean
    public Binding masterLookupDlqBinding() {
        return BindingBuilder.bind(masterLookupDlq()).to(masterLookupDlx()).with(MASTER_LOOKUP_DLQ);
    }

    // --- Main Listener Queues with DLX Args ---
    @Bean
    public Queue donorRegisteredQueue() {
        return QueueBuilder.durable(DONOR_REGISTERED_QUEUE)
                .withArgument("x-dead-letter-exchange", DONOR_REGISTERED_DLX)
                .withArgument("x-dead-letter-routing-key", DONOR_REGISTERED_DLQ)
                .build();
    }

    @Bean
    public Queue donationCompletedQueue() {
        return QueueBuilder.durable(DONATION_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DONATION_COMPLETED_DLX)
                .withArgument("x-dead-letter-routing-key", DONATION_COMPLETED_DLQ)
                .build();
    }

    @Bean
    public Queue bloodRequestQueue() {
        return QueueBuilder.durable(BLOOD_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", BLOOD_REQUEST_DLX)
                .withArgument("x-dead-letter-routing-key", BLOOD_REQUEST_DLQ)
                .build();
    }

    @Bean
    public Queue masterLookupQueue() {
        return QueueBuilder.durable(MASTER_LOOKUP_QUEUE)
                .withArgument("x-dead-letter-exchange", MASTER_LOOKUP_DLX)
                .withArgument("x-dead-letter-routing-key", MASTER_LOOKUP_DLQ)
                .build();
    }

    // --- Main Queue Bindings ---
    @Bean
    public Binding donorRegisteredBinding() {
        return BindingBuilder.bind(donorRegisteredQueue()).to(donorTopicExchange()).with("donor.registered");
    }

    @Bean
    public Binding donationCompletedBinding() {
        return BindingBuilder.bind(donationCompletedQueue()).to(donationTopicExchange()).with("donation.completed");
    }

    @Bean
    public Binding bloodRequestBinding() {
        return BindingBuilder.bind(bloodRequestQueue()).to(transactionTopicExchange()).with("transaction.request.created");
    }

    @Bean
    public Binding masterLookupBinding() {
        return BindingBuilder.bind(masterLookupQueue()).to(masterTopicExchange()).with("master.lookup.changed");
    }

    // --- Container Factory 1: User Notifications (Exponential Backoff 2s/8s/32s) ---
    @Bean
    public SimpleRabbitListenerContainerFactory userNotificationListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);

        Advice advice = RetryInterceptorBuilder.stateless()
                .backOffOptions(2000L, 4.0d, 32000L)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        factory.setAdviceChain(advice);
        return factory;
    }

    // --- Container Factory 2: Emergency Alerts (Fast Backoff 500ms/1000ms) ---
    @Bean
    public SimpleRabbitListenerContainerFactory emergencyAlertListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);

        Advice advice = RetryInterceptorBuilder.stateless()
                .backOffOptions(500L, 2.0d, 1000L)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        factory.setAdviceChain(advice);
        return factory;
    }

    // --- Container Factory 3: Master Lookup Logging (Single Attempt Log & Drop) ---
    @Bean
    public SimpleRabbitListenerContainerFactory lookupLogListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);

        Advice advice = RetryInterceptorBuilder.stateless()
                .backOffOptions(1000L, 1.0d, 1000L)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();

        factory.setAdviceChain(advice);
        return factory;
    }
}
