package com.uta2.data;

public enum PartyResultMessage {
    // Success messages
    PARTY_CREATED("隊伍已建立"),
    INVITATION_SENT("邀請已送出"),
    PLAYER_JOINED("你已加入隊伍"),
    PLAYER_LEFT("你已離開隊伍"),
    PLAYER_KICKED("玩家已被踢出"),
    PARTY_DISBANDED("隊伍已解散"),
    LEADER_CHANGED("隊長已轉移給 "),

    // Error messages
    ALREADY_IN_PARTY("你已經在一個隊伍中了"),
    NOT_IN_PARTY("你不在任何隊伍中"),
    NOT_LEADER("你不是隊長"),
    PLAYER_NOT_FOUND("找不到該玩家"),
    PLAYER_ALREADY_IN_PARTY("該玩家已經在隊伍中了"),
    INVITATION_EXPIRED("邀請已過期"),
    NO_INVITATION("你沒有收到來自該玩家的邀請"),
    CANNOT_KICK_SELF("你不能踢掉自己"),
    PARTY_FULL("隊伍已滿"),
    INVITATION_FAILED("邀請失敗"),
    CANNOT_INVITE_SELF("你不能邀請自己");


    private final String message;

    PartyResultMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
