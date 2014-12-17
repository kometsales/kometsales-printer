package vertical.fl.kometPrinter.endPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import vertical.fl.kometPrinter.bo.PrintRequest;
import vertical.fl.kometPrinter.utils.ClientHttpUtility;
import vertical.fl.kometPrinter.utils.PropertiesReader;

/**
 * EndPoint for REST comunication.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */
public class RestEndPoint {
    
    private static Logger logger = Logger.getLogger(RestEndPoint.class);

    public HashMap<String, Object> sendRequest(final PrintRequest aPrintRequest) {
        boolean wasCreated = false;
        HashMap<String, Object> data = new HashMap<String, Object>();
        
        logger.debug(">>>>>> Getting bytes for objectIds "+aPrintRequest.getObjIds()+", objectType "+aPrintRequest.getObjType());
        
        String path = PropertiesReader.getProperties("app.temp.folder");
        String fileName = path + "tempPDF" + System.currentTimeMillis() + (Math.random()) + ".pdf";
        String url = PropertiesReader.getProperties("komet.sales.api.url");
        StringBuilder params = new StringBuilder();
        params.append("?authenticationToken=" + aPrintRequest.getToken());
        params.append("&objectIds=" + aPrintRequest.getObjIds());
        params.append("&objectType=" + aPrintRequest.getObjType());
        params.append("&remotePrinterId=" + aPrintRequest.getRemotePrinterId());

        try {
            logger.debug(">>>>>> API URL " + ( url + "/file.get" + params.toString() ));
            String response = ClientHttpUtility.getResponse(url + "/file.get" + params.toString());
            logger.debug(">>>>>> API Response " + response);
            JSONObject value = (JSONObject) JSONValue.parse(response);
            Long status = (Long) value.get("status");
            if (status == 1) {
                String bytes = (String) value.get("content");
                logger.debug(">>>>>> File for objectIds "+aPrintRequest.getObjIds()+", content: "+ bytes);
                if(0!=bytes.length()){
                    wasCreated = generatePDF(readResponse(bytes), fileName);
                }
                logger.debug(">>>>>> File for objectIds "+aPrintRequest.getObjIds()+", was created "+wasCreated);
            }
        } catch (ParseException e) {
            logger.error("Error reading API info", e);
        } catch (Exception e) {
            logger.error("Error reading API info", e);
        }
        data.put("FileName", fileName);
        data.put("WasCreated", wasCreated);
        return data;
    }

    /**
     * 
     * @param bytesStr
     * @return
     */
    public byte[] readResponse(String bytesStr) {
        // We count how many bytes there are in this string.
        // One byte per Token.
        StringTokenizer st = new StringTokenizer(bytesStr.toString(), ",");
        byte[] buf = new byte[1024];
        buf = new byte[st.countTokens()];
        int i = 0;
        // Now we parse out all Bytes from the string, and put them into
        // the prepared byte array.
        while (st.hasMoreTokens()) {
            byte bytes = Byte.parseByte(st.nextToken());
            buf[i] = bytes;
            i++;
        }
        // Here I print true if both strings are exactly the same
        // which they should be, which means that the bytes are intact
        // before and after conversion.
        // Here we would make the physical file on the machine.
        return buf;
    }

    /**
     * Write a PDF file from a byte array.
     * 
     * @param data
     *            :byte array
     * @param fileName
     *            :target
     * @return
     */
    private boolean generatePDF(byte[] data, String fileName) {
        FileOutputStream fos;
        boolean wasCreated = false;
        try {
            File someFile = new File(fileName);
            fos = new FileOutputStream(someFile);
            fos.write(data);
            fos.flush();
            fos.close();
            wasCreated = true;            
        } catch (FileNotFoundException e) {
            wasCreated = false;
            logger.error("", e);
        } catch (IOException e) {
            wasCreated = false;
            logger.error("", e);
        } catch (Exception e) {
            wasCreated = false;
            logger.error("", e);
        }
        return wasCreated;
    }

}
