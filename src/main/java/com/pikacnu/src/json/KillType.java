package com.pikacnu.src.json;

/**
 * 定義所有可能的擊殺類型。
 */
public enum KillType {
	PLAYER,
	SPARE,
	VOID,
	MELT;

	/**
	 * 從字串獲取對應的 enum 值（不區分大小寫）
	 */
	public static KillType fromString(String text) {
		if (text == null)
			throw new IllegalArgumentException("No enum constant for null string");

		for (KillType type : KillType.values())
			if (type.name().equalsIgnoreCase(text))
				return type;

		throw new IllegalArgumentException("No enum constant for string: " + text);
	}

	/**
	 * 從字串獲取對應的 enum 值，如果找不到則返回 null
	 */
	public static KillType fromStringOrNull(String text)
	{
		if (text == null)
			return null;

		for (KillType type : KillType.values())
			if (type.name().equalsIgnoreCase(text))
				return type;

		return null;
	}
}
