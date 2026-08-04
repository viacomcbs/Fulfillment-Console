package com.paramount.test.ff.common.util.props;

public enum WheelDirection {
	WHEEL_DOWN("DOWN"), WHEEL_UP("UP");
	 private final String value;

	  WheelDirection(String value) {
	    this.value = value;
	  }

	  public String value() {
	    return value;
	  }
	  
}
