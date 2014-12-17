package vertical.fl.kometPrinter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;

public class ClientHttpUtility {
    public static String getResponse(String url) throws Exception {
        String body = null;
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        method.getParams().setSoTimeout(120000);
        try {
            // Execute the method
            int statusCode = client.executeMethod(method);
            // Validate status
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Error trying to get response from " + url + ". HttpStatus = " + statusCode);
            }
            // Read the response body.
            // byte[] responseBody = method.getResponseBody();
            byte[] responseBody = IOUtils.toByteArray(method.getResponseBodyAsStream());
            // Deal with the response.
            body = new String(responseBody);
        } catch (HttpException e) {
            throw new Exception("Fatal protocol violation from url " + url + ": " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("Fatal transport violation: " + url + ": " + e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return body;
    }

    public static byte[] getResponseAsByte(String url) throws Exception {
        byte[] responseBody = null;
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();
        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        try {
            // Execute the method
            int statusCode = client.executeMethod(method);
            // Validate status
            if (statusCode != HttpStatus.SC_OK) {
                throw new Exception("Error trying to get response from " + url + ". HttpStatus = " + statusCode);
            }
            // Read the response body.
            responseBody = method.getResponseBody();

        } catch (HttpException e) {
            throw new Exception("Fatal protocol violation from url " + url + ": " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("Fatal transport violation: " + url + ": " + e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return responseBody;
    }

    public static String getResponsePostMethod(String url, Map<String, String> listParameters, NameValuePair[] nameValuePairParams) throws Exception {
        String body = null;

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();
        client.getParams().setParameter("http.useragent", "App Vertical-Tech");

        // Create a method instance.
        PostMethod method = new PostMethod(url);
        Iterator<Map.Entry<String, String>> it = listParameters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
            method.addParameter((String) pairs.getKey(), (String) pairs.getValue());
        }
        if (nameValuePairParams != null && nameValuePairParams.length > 0) {
            method.addParameters(nameValuePairParams);
        }
        BufferedReader br = null;
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
                throw new Exception("Error trying to get response from " + url + ". HttpStatus = " + returnCode);
            } else {
                br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String readLine;
                while (((readLine = br.readLine()) != null)) {
                    body = readLine;
                }
            }
        } catch (HttpException e) {
            throw new Exception("Fatal protocol violation from url " + url + ": " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("Fatal transport violation: " + url + ": " + e.getMessage());
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return body;
    }

    public static String getResponsePostMethod(String url, Map<String, String> listParameters, Map<String, String> listRequestHeader, String xml) throws Exception {
        String body = null;

        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        PostMethod method = new PostMethod(url);

        Iterator<Map.Entry<String, String>> listreIterator = listRequestHeader.entrySet().iterator();
        while (listreIterator.hasNext()) {
            Map.Entry<String, String> pairs = (Map.Entry<String, String>) listreIterator.next();
            method.addRequestHeader((String) pairs.getKey(), (String) pairs.getValue());
        }

        Iterator<Map.Entry<String, String>> it = listParameters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
            method.addParameter((String) pairs.getKey(), (String) pairs.getValue());
        }

        RequestEntity entity = new StringRequestEntity(xml, "application/atom+xml", "UTF-8");
        method.setRequestEntity(entity);
        BufferedReader br = null;
        try {
            int returnCode = client.executeMethod(method);
            if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {

            } else {
                br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String readLine;
                while (((readLine = br.readLine()) != null)) {
                    body = readLine;
                }
            }
        } catch (HttpException e) {

        } catch (IOException e) {

        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return body;
    }
}
