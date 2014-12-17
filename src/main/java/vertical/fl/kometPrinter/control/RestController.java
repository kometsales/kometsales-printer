package vertical.fl.kometPrinter.control;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import vertical.fl.kometPrinter.bo.PrintRequest;
import vertical.fl.kometPrinter.endPoint.RestEndPoint;
import vertical.fl.kometPrinter.service.PrinterService;
import vertical.fl.kometPrinter.utils.gcp.GooglePrinterService;
import vertical.fl.kometPrinter.utils.gcp.GpcJobType;
import vertical.fl.kometPrinter.utils.gcp.IGpcConstants;

/**
 * Controller class for REST comunication.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */
public class RestController {

    @Autowired
    PrinterService aPrinterService;
    @Autowired
    RestEndPoint   aRestEndPoint;
    
    private static Logger logger = Logger.getLogger(PrinterService.class);
    
    public boolean  processMessage(final PrintRequest aPrintRequest) {
        boolean isPrinted = true;

        //
        
    	if( (aPrintRequest.getRemotePrinterId() != null && aPrintRequest.getRemotePrinterId().trim().equals("NO_PRINTER")) || (aPrintRequest.getPrinterName() != null && aPrintRequest.getPrinterName().trim().equals("NO_PRINTER")) ) {
    		logger.info("Google Cloud Print PrinterId: " + aPrintRequest.getRemotePrinterId() + ", printerName: " + aPrintRequest.getPrinterName() + ", printServiceType: " + aPrintRequest.getPrintServiceType() + ", objectType: " + aPrintRequest.getObjType() + ", objectIds: " + aPrintRequest.getObjIds() + ". This document was ignored");
    		return true;
    	}

        String printerServiceType = aPrintRequest.getPrintServiceType();

        if(printerServiceType != null && printerServiceType.equals(IGpcConstants.PRINTER_SERVICE_TYPE_REMOTE) && (aPrintRequest.getRemotePrinterId() != null && aPrintRequest.getRemotePrinterId().trim().length() > 0 ) ) {
        	//Utilizar Google Cloud Print
        	GooglePrinterService googlePrinterService = new GooglePrinterService();

        	googlePrinterService.setEmail(aPrintRequest.getRemoteLogin());
        	googlePrinterService.addTokensKeyValue(aPrintRequest.getRemoteToken());
        	googlePrinterService.setIsFile(true);
        	googlePrinterService.setBase64(false);
        	googlePrinterService.setJobType(GpcJobType.PDF);
        	googlePrinterService.setPrinterId(aPrintRequest.getRemotePrinterId());

            // 1. call rest API
            HashMap<String, Object> data = aRestEndPoint.sendRequest(aPrintRequest);
            String fileName = "";

            if(data != null && data.containsKey("WasCreated") && (Boolean) data.get("WasCreated")) {
                fileName = (String) data.get("FileName");

                int numberOfCopies = aPrintRequest.getNumberOfCopies();
                
                System.err.println("numberOfCopies: " + numberOfCopies);

                for(int index = 1; index <= numberOfCopies; index++) {
                	googlePrinterService.setUniqueJobName(fileName + "-" + String.valueOf(System.currentTimeMillis()));
                	googlePrinterService.setJobsrc(fileName);

                	try {
        				isPrinted = googlePrinterService.submitJob();
        			} catch (Exception e) {
        				isPrinted = false;

        				logger.error("Error Google Cloud Print", e);

        				e.printStackTrace();
        			}

                	System.err.println("Google Print - isPrinted: " + isPrinted);
                }
            }
        } else {
        	//Utilizar la forma de impresion actual: Localmente

            // 1. call rest API
            HashMap<String, Object> data = aRestEndPoint.sendRequest(aPrintRequest);

            String fileName = "";

            if(data.containsKey("WasCreated") && (Boolean) data.get("WasCreated")){
            	fileName = (String) data.get("FileName");

                int numberOfCopies = aPrintRequest.getNumberOfCopies();
                
                System.err.println("numberOfCopies: " + numberOfCopies);

                for(int index = 1; index <= numberOfCopies; index++) {
	                try {
	                    // 2. read bytes from rest API
	                       // 3. call printer passing byte array
	                       logger.debug(">>>>>> Printing parameters: [ObjectType: "+aPrintRequest.getObjType()+"]");
	                       isPrinted = aPrinterService.print(aPrintRequest.getPrinterName(), fileName);
	                } catch (Exception e) {
	    				isPrinted = false;
	
	    				logger.error("Error Standard Print", e);
	
	    				e.printStackTrace();
	    			}
	                System.err.println("Normal Printer - isPrinted: " + isPrinted);
                }
            }
        }

        return isPrinted;
    }

}
