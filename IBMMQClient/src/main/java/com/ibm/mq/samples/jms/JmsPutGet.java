package com.ibm.mq.samples.jms;

import javax.jms.JMSException;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.ibm.mq.MQException;
import com.ibm.mq.jmqi.JmqiException;
import com.ibm.msg.client.jms.DetailedInvalidDestinationRuntimeException;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsExceptionDetail;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

public class JmsPutGet {

    // System exit status value (assume unset value to be 1)
    private static int status = 1;

    // Create variables for the connection to MQ
    private static final String HOST = "localhost"; // Host name or IP address
    private static final int PORT = 1414; // Listener port for your queue manager
    private static final String CHANNEL = "PASSWORD.SVRCONN"; // Channel name
    private static final String QMGR = "QM1"; // Queue manager name
    private static final String APP_USER = "firzhan"; // User name that application uses to connect to MQ
    private static final String APP_PASSWORD = "passw0rd"; // Password that the application uses to connect to MQ
    private static final String QUEUE_NAME = "LQ1"; // Queue that the
    // application uses to put and get messages to and from


    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {

        // Variables
        JMSContext context = null;
        Destination destination = null;
        JMSProducer producer = null;
        JMSConsumer consumer = null;



        try {
            // Create a connection factory
            JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            JmsConnectionFactory cf = ff.createConnectionFactory();

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, " WSO2 EI " +
                    "Consumer");
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            cf.setStringProperty(WMQConstants.USERID, APP_USER);
            cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);

            // Create JMS objects
            context = cf.createContext();
            destination = context.createQueue("queue:///" + QUEUE_NAME);

            long uniqueNumber = System.currentTimeMillis() % 1000;
            TextMessage message = context.createTextMessage("Your lucky number today is " + uniqueNumber);

            producer = context.createProducer();
            producer.send(destination, message);
            System.out.println("Sent message:\n" + message);

           /* consumer = context.createConsumer(destination); // autoclosable
            String receivedMessage = consumer.receiveBody(String.class, 15000); // in ms or 15 seconds

            System.out.println("\nReceived message:\n" + receivedMessage);
*/
            recordSuccess();
        } catch (JMSException je) {
            System.err.println("Caught JMSException");

            // Check for linked exceptions in JMSException
            Throwable t = je;
            while (t != null) {
                // Write out the message that is applicable to all exceptions
                System.err.println("Exception Msg: " + t.getMessage());
                // Write out the exception stack trace
                t.printStackTrace(System.err);

                // Add on specific information depending on the type of exception
                if (t instanceof JMSException) {
                    JMSException je1 = (JMSException) t;
                    System.err.println("JMS Error code: " + je1.getErrorCode());

                    if (t instanceof JmsExceptionDetail){
                        JmsExceptionDetail jed = (JmsExceptionDetail)je1;
                        System.err.println("JMS Explanation: " + jed.getExplanation());
                        System.err.println("JMS Explanation: " + jed.getUserAction());
                    }
                } else if (t instanceof MQException) {
                    MQException mqe = (MQException) t;
                    System.err.println("WMQ Completion code: " + mqe.getCompCode());
                    System.err.println("WMQ Reason code: " + mqe.getReason());
                } else if (t instanceof JmqiException){
                    JmqiException jmqie = (JmqiException)t;
                    System.err.println("WMQ Log Message: " + jmqie.getWmqLogMessage());
                    System.err.println("WMQ Explanation: " + jmqie.getWmqMsgExplanation());
                    System.err.println("WMQ Msg Summary: " + jmqie.getWmqMsgSummary());
                    System.err.println("WMQ Msg User Response: "
                            + jmqie.getWmqMsgUserResponse());
                    System.err.println("WMQ Msg Severity: " + jmqie.getWmqMsgSeverity());
                }

                // Get the next cause
                t = t.getCause();
            }
        }

        System.exit(status);

    } // end main()

    /**
     * Record this run as successful.
     */
    private static void recordSuccess() {
        System.out.println("SUCCESS");
        status = 0;
        return;
    }

    /**
     * Record this run as failure.
     *
     * @param ex
     */
    private static void recordFailure(Exception ex) {
        if (ex != null) {
            if (ex instanceof JMSException) {
                processJMSException((JMSException) ex);
            } else {
                System.out.println(ex);
            }
        }
        System.out.println("FAILURE");
        status = -1;
        return;
    }

    /**
     * Process a JMSException and any associated inner exceptions.
     *
     * @param jmsex
     */
    private static void processJMSException(JMSException jmsex) {
        System.out.println(jmsex);
        Throwable innerException = jmsex.getLinkedException();
        if (innerException != null) {
            System.out.println("Inner exception(s):");
        }
        while (innerException != null) {
            System.out.println(innerException);
            innerException = innerException.getCause();
        }
        return;
    }


}
