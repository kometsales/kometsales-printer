package vertical.fl.kometPrinter.control;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;

import vertical.fl.kometPrinter.bo.PrintRequest;
import vertical.fl.kometPrinter.endPoint.JmsEndPoint;
import vertical.fl.kometPrinter.utils.MemoryDBManager;

/**
 * Controller class for JMS comunication.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */
public class QueueController {

    @Autowired
    RestController                  aRestController;
    @Autowired
    JmsEndPoint                     aJmsEndPoint;

    @Autowired
    MemoryDBManager                 aMemoryDBManager;

   // private HashMap<String, String> processedMessages = new HashMap<String, String>();

    private static Logger           logger            = Logger.getLogger(QueueController.class);

    public void readQueue() {
        boolean wasPrinted = false;
        HashMap<String, Object> data = aJmsEndPoint.readQueue();
        String messageID = "";
        // 1. check queue
        if (data.containsKey("HasMessage") && (Boolean) data.get("HasMessage")) {
            messageID = (String) data.get("MessageID");
            aMemoryDBManager.initDB();
            if (!aMemoryDBManager.getRow(messageID)) {
                // if(!processedMessages.containsKey(messageID)){
                // processedMessages.put(messageID, messageID);
                aMemoryDBManager.addRow(messageID, messageID);
                logger.debug(">>>>>> Message received");
                
                // 2. hasMessage then read message and bind DTO
                PrintRequest aPrintRequest = null;
                Object jsonData = JSONValue.parse((String) data.get("Message"));
                                
                if(JSONArray.class.isInstance(jsonData)){ // Si el mensaje es un JSONArray entonces se itera el array para procesar cada item del array como un mensaje independiente
                	
                	JSONArray dataList = (JSONArray) jsonData;
                	if(dataList != null && !dataList.isEmpty()){
                		wasPrinted = true;
                		for (Object dataMessage : dataList) {
                			aPrintRequest = fillDTO(dataMessage.toString());
                			// 4. Call RestController.processMessage
                            wasPrinted &= aRestController.processMessage(aPrintRequest);
						}
                	}
                	
                } else { // Si el mensaje es un JSONObject entonces se procesa 1 solo mensaje y listo
                	aPrintRequest = fillDTO((String) data.get("Message"));
                	// 4. Call RestController.processMessage
                    wasPrinted = aRestController.processMessage(aPrintRequest);
                }               

                logger.debug(">>>>>> Message was processed >>>>> "+wasPrinted);

                aJmsEndPoint.purgeQueue((String) data.get("ReceiptHandle"));
                logger.debug(">>>>>> Message deleted from the Queue");
            }else{
            	//se deberia borrar el mensaje de la cola porque al parecer esta enbasurado. 
            	//pero se debe validar mejor con la cantidad de veces que ha sido leido el mensaje: Ej: > 1000
            	//aJmsEndPoint.purgeQueue((String) data.get("ReceiptHandle"));
                //logger.debug(">>>>>> Message deleted from the Queue because somebody forgot to delete it");
            }

        }
    }

    public PrintRequest fillDTO(final String message) {
        PrintRequest aPrintRequest = new PrintRequest();
        JSONObject value = (JSONObject) JSONValue.parse(message);
        aPrintRequest.setToken((String) value.get("token"));
        aPrintRequest.setObjType(String.valueOf((Long) value.get("objectType")));
        aPrintRequest.setPrinterName((String) value.get("printerName"));
        aPrintRequest.setObjIds((String) value.get("objectIds"));

        aPrintRequest.setRemotePrinterId(value.get("remotePrinterId") != null ? value.get("remotePrinterId").toString() : "" );
        aPrintRequest.setRemoteLogin((String) value.get("remoteLogin"));
        aPrintRequest.setRemoteToken((String) value.get("remoteToken"));
        aPrintRequest.setPrintServiceType(value.get("printServiceType") != null ? value.get("printServiceType").toString() : "");

        //N�mero de copias a imprimir
        int numberOfCopies = 1;

        try {
        	Object numberOfCopiesObject = value.get("numberOfCopies");

        	if(numberOfCopiesObject != null && numberOfCopiesObject.toString().trim().length() > 0) {
        		numberOfCopies = Integer.parseInt(numberOfCopiesObject.toString().trim());
        	}
        } catch(Exception e) {
        	logger.error("",e);
        	numberOfCopies = 1;
        }

        if(numberOfCopies <= 0) {
        	numberOfCopies = 1;
        } else if(numberOfCopies > 5) {
        	numberOfCopies = 5;
        }

        aPrintRequest.setNumberOfCopies(numberOfCopies);

        return aPrintRequest;
    }
}
