package vertical.fl.kometPrinter.service;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import vertical.fl.kometPrinter.utils.PropertiesReader;

public class MainService {

    /**
     * Start spring context
     * 
     * @param args
     */
    private static Logger logger = Logger.getLogger(MainService.class);

    @SuppressWarnings("unused")
	public static void main(final String[] args) {
        logger.debug(">>>>>> Starting KometPrint Service: ");

        String queueURL = PropertiesReader.getProperties("queue.printing.url");

        logger.info("Process message from URL queue: " + queueURL);

        ApplicationContext appCtx = new ClassPathXmlApplicationContext("applicationContext.xml");

        logger.info("Context Up");
    }

}
