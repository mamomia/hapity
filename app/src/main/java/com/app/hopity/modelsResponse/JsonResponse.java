package com.app.hopity.modelsResponse;

import com.app.hopity.models.UserInfo;

/**
 * Created by Mushi on 8/28/2015.
 */
public class JsonResponse {
    public int user_id;
    public String status = "";
    public String error = "";
    public UserInfo[] user_info = new UserInfo[]{};
}
