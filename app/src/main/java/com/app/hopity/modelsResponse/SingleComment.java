package com.app.hopity.modelsResponse;

/**
 * Created by Mushi on 9/2/2015.
 */
public class SingleComment {
    public String commentMsg;
    public String commenterUsername;
    public String commenterUserId;
    public String commenterProfilePicture;
    public String status;

    public SingleComment(String stat, String id, String uname, String profpic, String msg) {
        status = stat;
        commenterUserId = id;
        commentMsg = msg;
        commenterUsername = uname;
        commenterProfilePicture = profpic;
    }

    public String toString() {
        return String.format("stat : %s \n id: %s \n uname: %s \n propic: %s \n msg: %s \n", status, commenterUserId, commenterUsername, commenterProfilePicture, commentMsg);
    }
}