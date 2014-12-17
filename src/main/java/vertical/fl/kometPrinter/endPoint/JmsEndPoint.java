package vertical.fl.kometPrinter.endPoint;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import vertical.fl.kometPrinter.utils.PropertiesReader;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/**
 * EndPoint for JMS comunication.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */
public class JmsEndPoint {
    private static Logger logger = Logger.getLogger(JmsEndPoint.class);

    /**
     * Read the SQS looking for messages for komet printing services.
     * 
     * @return
     */
    public HashMap<String, Object> readQueue() {
        //logger.debug(">>>>>> ReadQueue Service srtarted.... ");
        HashMap<String, Object> data = new HashMap<String, Object>();
        boolean hasMessage = false;
        String messag = "";
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setProtocol(Protocol.HTTP);
        String aReceiptHandle = "";
        String messageID = "";

        // sqs = new AmazonSQSAsyncClient(new BasicAWSCredentials(getAwsAccessKey(), getAwsSecretKey()));
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(getQueuePrinting());
        receiveMessageRequest.setMaxNumberOfMessages(1);
        receiveMessageRequest.setVisibilityTimeout(2);
        List<Message> messages = getSqs().receiveMessage(receiveMessageRequest).getMessages();
        if(messages.size()==0){
            //logger.debug(">>>>>> No messages");    
        }else{
            logger.debug(">>>>>> Message list size: " + messages.size());
        }
        
        if (null != messages && 0 != messages.size() && null != messages.get(0)) {
            Message message = messages.get(0);
            hasMessage = true;
            messag = message.getBody();
            aReceiptHandle = message.getReceiptHandle();
            messageID = message.getMessageId();

        }
        if(StringUtils.hasLength(messageID)){
            logger.debug(">>>>>> Message ID: " + messageID);
        }
        data.put("HasMessage", hasMessage);
        data.put("Message", messag);
        data.put("ReceiptHandle", aReceiptHandle);
        data.put("MessageID", messageID);
        return data;
    }

    public void purgeQueue(String aReceiptHandle) {
        getSqs().deleteMessage(new DeleteMessageRequest(getQueuePrinting(), aReceiptHandle));
    }

    public String getQueuePrinting() {
        return PropertiesReader.getProperties("queue.printing.url");
    }

    public String getAwsAccessKey() {
        return PropertiesReader.getProperties("accessKey.printing");
    }

    public String getAwsSecretKey() {
        return PropertiesReader.getProperties("secretKey.printing");
    }

    public AmazonSQS getSqs() {
        return new AmazonSQSAsyncClient(new BasicAWSCredentials(getAwsAccessKey(), getAwsSecretKey()));
    }

}