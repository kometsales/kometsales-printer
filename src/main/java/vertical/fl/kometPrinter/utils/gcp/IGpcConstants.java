package vertical.fl.kometPrinter.utils.gcp;

public interface IGpcConstants {
	public static final String[] COOKIES_KEY = { "SID", "LSID", "HSID", "SSID" };

	public static final String CRLF = "\r\n";

	//The following are used for authentication functions.
	public static final String FOLLOWUP_HOST = "www.google.com/cloudprint";
	public static final String FOLLOWUP_URI = "select%2Fgaiaauth";
	public static final String GAIA_HOST = "www.google.com";
	public static final String LOGIN_URI = "/accounts/ServiceLoginAuth";
	public static final String LOGIN_URL = "https://www.google.com/accounts/ClientLogin";
	public static final String SERVICE = "cloudprint";
	//public static final String OAUTH = "This_should_contain_oauth_string_for_your_username";

	//The following are used for general backend access.
	public static final String CLOUDPRINT_URL = "http://www.google.com/cloudprint";
	public static final String CLOUDPRINT_URL_HTTPS = "https://www.google.com/cloudprint";

	//CLIENT_NAME should be some string identifier for the client you are writing.
	public static final String CLIENT_NAME = "Komet Sales";

	//Impresora local
	public String PRINTER_SERVICE_TYPE_LOCAL = "1";

	//Impresora remota
	public String PRINTER_SERVICE_TYPE_REMOTE = "2";
}
