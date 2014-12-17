package vertical.fl.kometPrinter.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.log4j.Logger;

/**
 * Service class for print byte arrays.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */

public class PrinterService {
    private static Logger logger = Logger.getLogger(PrinterService.class);

    public boolean print(String printerName, String fileName) {
        PrintService selectedPrinter = null;
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(null, null);

        logger.debug(">>>>>> Search Printer: " + printerName);

        for (int i = 0; i < printers.length; i++) {
            PrintService printService = printers[i];
            if (printService.getName().equalsIgnoreCase(printerName)) {
                selectedPrinter = printService;
                logger.debug(">>>>>> Selected Printer: " + printService.getName() + " is " + printerName);
                break;
            } else {
            	logger.debug(">>>>>> Current Printer: " + printService.getName() + " diferent from " + printerName);
            }
        }

        try {
            if(selectedPrinter==null){
                logger.debug(">>>>>> Printer Not Found: "+printerName);
            }else{
                FileInputStream psStream = new FileInputStream(fileName);

                DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
                Doc myDoc = new SimpleDoc(psStream, psInFormat, null);
                PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();

                DocPrintJob job = selectedPrinter.createPrintJob();
                job.print(myDoc, aset);

                logger.debug(">>>>>> Printing File: " + fileName);
                logger.debug(">>>>>> Printing on: " + selectedPrinter.getName());
            }

            return true;
        } catch (FileNotFoundException e) {
            logger.debug(">>>>>> Error printing on "+printerName+": ",e);
            return false;
        } catch (Exception e) {
            logger.debug(">>>>>> Error printing on "+printerName+": ",e);
            return false;
        }
    }
}
