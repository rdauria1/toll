package com.amaxzadigital.tollpays.checkin.modelclasses;

/**
 * Created by Hussain Marvi on 13-Apr-17.
 */

public class ModelClassLaneVoting {
    private int laneNo, votingCount, index;

    public ModelClassLaneVoting(int laneNo, int votingCount, int index) {
        this.laneNo = laneNo;
        this.votingCount = votingCount;
        this.index = index;
    }

    public ModelClassLaneVoting() {
    }

    public int getLaneNo() {
        return laneNo;
    }

    public void setLaneNo(int laneNo) {
        this.laneNo = laneNo;
    }

    public int getVotingCount() {
        return votingCount;
    }

    public void setVotingCount(int votingCount) {
        this.votingCount = votingCount;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
