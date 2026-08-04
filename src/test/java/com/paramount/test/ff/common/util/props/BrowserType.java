package com.paramount.test.ff.common.util.props;

public enum BrowserType {
	
  CHROME ("chrome"),
  FIREFOX ("firefox"),
  SAFARI ("safari"),
  IEXPLORE ("internet explorer"),
  MICROSOFTEDGE ("MicrosoftEdge");

  private final String value;

  BrowserType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static BrowserType getEnumByString(String value) {
      for (BrowserType browserType : BrowserType.values()) {
    	  if (value.equalsIgnoreCase(browserType.value)) {
        	  return browserType;
          }
      }
      return null;
  }
  
}
