package vertical.fl.kometPrinter.utils;

import java.util.ResourceBundle;

/**
 * Class that reads the properties files.
 * 
 * @author Hector Mosquera Turner
 * @since 11/10/2012
 * @version 1.0
 */
public class PropertiesReader {
  private static String propertiesFileName = "config";
  private static ResourceBundle resource = ResourceBundle.getBundle(propertiesFileName);
  
  public static String getProperties(String propertyName) {
    return resource.getString(propertyName);
  }
  
}
