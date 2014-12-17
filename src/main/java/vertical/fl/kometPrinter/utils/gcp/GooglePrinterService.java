package vertical.fl.kometPrinter.utils.gcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.iharder.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class GooglePrinterService implements IGpcConstants {
	private Map<String,String> tokens;
	private String email;
	private String password;
	private String jobType;
	private String printerId;
	private String uniqueJobName;

	//true = El archivo ya esta en el formato base64 como lo espera Google Cloud, false: El archivo esta en su formato original. Esto solo aplica cuando el archivo a imprimir esta en formato PDF
	private boolean isBase64 = false;

	//true = Lo que se va a imprimir es un archivo, lo que se va a imprimir es un string
	private boolean isFile = true;

	//Depende de jobType: PDF = Ruta en disco del archivo
	private String jobsrc;

	private static Logger logger = Logger.getLogger(GooglePrinterService.class);

	/**
	 * Agregar tokens de autenticacion en formato clave=valor\nclave2=valor2
	 * @param tokensString
	 */
	public void addTokensKeyValue(String tokensString) {
		if(tokens == null) {
			tokens = new HashMap<String, String>();
		}

		if(tokensString != null) {
			String[] tokensArray = tokensString.split(CRLF);
			
			if(tokensArray != null && tokensArray.length > 0) {				
				int tokensSize = tokensArray.length;

				for(int j = 0; j < tokensSize; j++) {
					String currentToken = tokensArray[j];

					if(currentToken != null && currentToken.length() > 0) {
						String[] currentTokenArray = currentToken.split("=");
						
						if(currentTokenArray.length == 1) {
							tokens.put(currentTokenArray[0], null);
						} else if(currentTokenArray.length == 2) {
							tokens.put(currentTokenArray[0], currentTokenArray[1]);
						}
					}
				}
			}
		}
	}

	/**
	 * Submit a job to printerid with content of dataUrl.
	 * @return <code>boolean</code> True = submitted, False = errors.
	 */
	public boolean submitJob() throws Exception {
		String fdata = null;

		if(GpcJobType.PDF.equals(jobType)) {
			if(isBase64 != true) {
				if(isFile == true) {
					fdata = Base64.encodeFromFile(jobsrc);
				} else {
					fdata = Base64.encodeBytes(jobsrc.getBytes());
				}
			}
		} else if(GpcJobType.PNG.equals(jobType)) {
			if(isFile == true) {
				fdata = FileUtils.readFileToString(new File(jobsrc));
			} else {
				fdata = jobsrc;
			}
		} else if(GpcJobType.JPEG.equals(jobType)) {
			if(isFile == true) {
				fdata = FileUtils.readFileToString(new File(jobsrc));
			} else {
				fdata = jobsrc;
			}
		} else {
			fdata = null;
		}

		/*
		The following dictionaries expect a certain kind of data in jobsrc, depending on jobtype:
	    jobtype               jobsrc
	    //===============================================
	    pdf                     pathname to the pdf file
	    png                     pathname to the png file
	    jpeg                    pathname to the jpeg file
	    url						Public URL to file
	    //===============================================
	     */

		String file_type = null;

		if(GpcJobType.PDF.equals(jobType)) {
			file_type = "application/pdf";
		} else if(GpcJobType.PNG.equals(jobType)) {
			file_type = "image/png";
		} else if(GpcJobType.JPEG.equals(jobType)) {
			file_type = "image/jpeg";
		} else if(GpcJobType.URL.equals(jobType)) {
			file_type = "url";
		} else {
			file_type = "application/xml";
		}

		String url = String.format("%s/submit",CLOUDPRINT_URL_HTTPS) + "?printerid="+printerId;

		List<FileData> files = new ArrayList<FileData>();

		FileData capabilities = new FileData();
		capabilities.setKey("capabilities");
		capabilities.setFileName("capabilities");
		capabilities.setValue("{\"capabilities\":[]}");
		files.add(capabilities);

		if(GpcJobType.PDF.equals(jobType) || GpcJobType.PNG.equals(jobType) || GpcJobType.JPEG.equals(jobType)) {
			FileData content = new FileData();
			content.setKey("content");
			content.setFileName(jobsrc);
			content.setValue(fdata);
		}

		Map<String,String> headers = new HashMap<String, String>();
		headers.put("printerid", printerId);
		headers.put("title", getUniqueJobName());

		if(GpcJobType.PDF.equals(jobType)) {
			headers.put("content", fdata);
		} else {
			headers.put("content", jobsrc);
		}

		if(GpcJobType.PDF.equals(jobType) == true) {
			headers.put("contentTransferEncoding", "base64");
		}

		headers.put("contentType", file_type);

		HttpResponse response = this.getURL(url, this.getTokens(), headers,files, false, false);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			sb.append(line).append(CRLF);
		}

		String responseBody = sb.toString();

		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getJsonFactory();
		JsonParser jp = factory.createJsonParser(responseBody);
		JsonNode jsonResult = mapper.readTree(jp);

		String message = null;
		boolean success = false;
		String jobId = null;
		String errorCode = null;

		JsonNode successNode = jsonResult.get("success");

		if(successNode != null && successNode.asText() != null) {
			JsonNode jobNode = jsonResult.get("job");

			if(successNode.asText().toLowerCase().equals("true")) {
				success = true;
				if(jsonResult.get("message") != null) {
					message = jsonResult.get("message").asText();
				}

				if(jobNode != null && jobNode.get("id") != null) {
					jobId = jobNode.get("id").asText();
				}
			} else if(successNode.asText().toLowerCase().equals("false")) {
				success = false;
				if(jsonResult.get("message") != null) {
					message = jsonResult.get("message").asText();
				}

				if(jobNode != null && jobNode.get("errorCode") != null) {
					errorCode = jobNode.get("errorCode").asText();
				}
			}
		} else {
			message = "Error to submit Job - Response body: " + responseBody;
			success = false;
		}

		logger.debug("Google cloud response: \n" + "success: " + success + "\n" + "message: " + message + "\n" + "jobId: " + jobId + "errorCode: " + errorCode);

		return success;
	}
	
	/**
	 * Get URL, with GET or POST depending data, adds Authorization header.
	 * @param url: Url to access.
	 * @param tokens: Map of authentication tokens for specific user.
	 * @param files: If a POST request, data to be sent with the request.
	 * @param cookies: True = send authentication tokens in cookie headers.
	 * @param anonymous: True = do not send login credentials.
	 * @throws Exception
	 * @return response to the HTTP request.
	 */
	public HttpResponse getURL(String url,Map<String,String> tokens,Map<String,String> headers,List<FileData> files,boolean cookies,boolean anonymous) throws Exception {
		//We still need to get the Auth token.
		HttpClient client = null;
		HttpPost post = null;

		HttpResponse response = null;

		try {
			client = new DefaultHttpClient();
			post = new HttpPost(url);

			if(anonymous == false) {
				if(cookies == true) {
					post.addHeader("Cookie", String.format("SID=%s; HSID=%s; SSID=%s",tokens.get("SID"), tokens.get("HSID"), tokens.get("SSID")));
				} else {
					post.addHeader("Authorization", String.format("GoogleLogin auth=%s",tokens.get("Auth")));
				}
			}

			post.addHeader("X-CloudPrint-Proxy", "KS-CLOUD-PROXY");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);

			//Aditional headers
			/**/
			if(headers != null) {
				Set<Entry<String, String>> entrySetHeaders = headers.entrySet();

				Iterator<Entry<String, String>> itHeaders = entrySetHeaders.iterator();

				while(itHeaders.hasNext()) {
					Entry<String, String> currentHeader = itHeaders.next();

					//post.addHeader(currentHeader.getKey(), currentHeader.getValue());

					nameValuePairs.add(new BasicNameValuePair(currentHeader.getKey(), currentHeader.getValue()));
				}
			}

			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			if(files != null) {
				Iterator<FileData> itFiles = files.iterator();

				while(itFiles.hasNext()) {
					FileData currentFile = itFiles.next();

					nameValuePairs.add(new BasicNameValuePair(currentFile.getKey(), currentFile.getValue()));
				}
			}

			response = client.execute(post);
		} catch(Exception e) {
			e.printStackTrace();

			throw e;
		}

		return response;
	}

	public Map<String, String> getTokens() {
		return tokens;
	}

	public void setTokens(Map<String, String> tokens) {
		this.tokens = tokens;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getPrinterId() {
		return printerId;
	}

	public void setPrinterId(String printerId) {
		this.printerId = printerId;
	}

	public String getJobsrc() {
		return jobsrc;
	}

	public void setJobsrc(String jobsrc) {
		this.jobsrc = jobsrc;
	}

	public boolean isBase64() {
		return isBase64;
	}

	public void setBase64(boolean isBase64) {
		this.isBase64 = isBase64;
	}

	public String getUniqueJobName() {
		return uniqueJobName;
	}

	public void setUniqueJobName(String uniqueJobName) {
		this.uniqueJobName = uniqueJobName;
	}

	public boolean isFile() {
		return isFile;
	}

	public void setIsFile(boolean isFile) {
		this.isFile = isFile;
	}
}