package com.paramount.test.ff.common.util.props;

public enum DesktopOSType {
	
  MQE_MAC ("MAC"),
  MQE_WINDOWS ("WINDOWS");

  private final String value;

  DesktopOSType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static DesktopOSType getEnumByString(String value) {
      for (DesktopOSType osType : DesktopOSType.values()) {
    	  if (value.equalsIgnoreCase(osType.value)) {
        	 return osType;
          }
      }
      return null;
  }
  
}
