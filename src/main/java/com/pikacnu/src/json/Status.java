package com.pikacnu.src.json;

/**
 * 定義所有可能的狀態。
 */
public enum Status {
  SUCCESS(1),
  ERROR(0);

  private final int value;

  Status(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  /**
   * 從數值獲取對應的 enum 值
   */
  public static Status fromValue(int value) {
    for (Status status : Status.values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("No enum constant for value: " + value);
  }

  /**
   * 從數值獲取對應的 enum 值，如果找不到則返回 null
   */
  public static Status fromValueOrNull(int value) {
    for (Status status : Status.values()) {
      if (status.value == value) {
        return status;
      }
    }
    return null;
  }
}
