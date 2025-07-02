package com.uta2.data;

public enum KillType {
    MELEE("近戰"),
    RANGED("遠程"),
    EXPLOSION("爆炸"),
    FALL("墜落"),
    ENVIRONMENT("環境"),
    MAGIC("魔法"),
    OTHER("其他");

    private final String displayName;

    KillType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static KillType fromString(String name) {
        try {
            return KillType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
