package com.app.hopity.modelsResponse;

import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.models.UserInfo;

/**
 * Created by Mushi on 9/6/2015.
 */
public class ProfileInfo {
    public String status = "";
    public String is_follower = "";
    public String is_block = "";
    public UserInfo[] profile_info = new UserInfo[]{};
    public BroadcastSingle[] broadcast = new BroadcastSingle[]{};
    public UserInfo[] blocked = new UserInfo[]{};
    public UserInfo[] followers = new UserInfo[]{};
    public UserInfo[] following = new UserInfo[]{};
}
