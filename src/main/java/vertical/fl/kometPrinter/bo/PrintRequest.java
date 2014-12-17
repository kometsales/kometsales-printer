package vertical.fl.kometPrinter.bo;

public class PrintRequest {

    public String token;
    public String objType;
    public String objIds;
    public String printerName;

    private String remotePrinterId;
    private String remoteLogin;
    private String remoteToken;
    private String printServiceType;

    /**
     * Numero de copias
     */
    private Integer numberOfCopies;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getObjType() {
        return objType;
    }

    public void setObjType(String objType) {
        this.objType = objType;
    }

    public String getObjIds() {
        return objIds;
    }

    public void setObjIds(String objIds) {
        this.objIds = objIds;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

	public String getRemotePrinterId() {
		return remotePrinterId;
	}

	public void setRemotePrinterId(String remotePrinterId) {
		this.remotePrinterId = remotePrinterId;
	}

	public String getRemoteLogin() {
		return remoteLogin;
	}

	public void setRemoteLogin(String remoteLogin) {
		this.remoteLogin = remoteLogin;
	}

	public String getRemoteToken() {
		return remoteToken;
	}

	public void setRemoteToken(String remoteToken) {
		this.remoteToken = remoteToken;
	}

	public String getPrintServiceType() {
		return printServiceType;
	}

	public void setPrintServiceType(String printServiceType) {
		this.printServiceType = printServiceType;
	}

	public Integer getNumberOfCopies() {
		return numberOfCopies;
	}

	public void setNumberOfCopies(Integer numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
	}

}
