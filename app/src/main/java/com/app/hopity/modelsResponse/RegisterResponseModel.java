package com.app.hopity.modelsResponse;

import com.app.hopity.models.UserInfo;

/**
 * Created by Mushi on 3/19/2016.
 */
public class RegisterResponseModel {
    public int user_id;
    public String status = "";
    public String error = "";
    public UserInfo[] user_info = new UserInfo[]{};
}
