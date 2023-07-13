package com.omegafrog.My.piano.app.web.vo.user;

import lombok.Builder;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class AlarmProperties {

    private boolean myFollowerAlarm;
    private boolean newFollowerAlarm;
    private boolean messageAlarm;
    private boolean goodAtPostAlarm;
    private boolean goodAtCommentAlarm;
    private boolean changeRanking;

    public AlarmProperties() {
        this.myFollowerAlarm=true;
        this.newFollowerAlarm=true;
        this.messageAlarm=true;
        this.goodAtPostAlarm=true;
        this.goodAtCommentAlarm=true;
        this.changeRanking=true;
    }

    @Builder
    public AlarmProperties(boolean myFollowerAlarm, boolean newFollowerAlarm, boolean messageAlarm, boolean goodAtPostAlarm, boolean goodAtCommentAlarm, boolean changeRanking) {
        this.myFollowerAlarm = myFollowerAlarm;
        this.newFollowerAlarm = newFollowerAlarm;
        this.messageAlarm = messageAlarm;
        this.goodAtPostAlarm = goodAtPostAlarm;
        this.goodAtCommentAlarm = goodAtCommentAlarm;
        this.changeRanking = changeRanking;
    }
}
