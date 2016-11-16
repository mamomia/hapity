package com.app.hopity.appdata;

/**
 * Created by Mushi on 8/28/2015.
 */
public class Constants {
    // TestURL
    //public static final String BASE_URL = "http://testing.egenienext.com/project/hapity/webservice/";
    // LiveURL
    public static final String BASE_URL = "http://api.hapity.com/webservice/";

    public static final String SIGN_UP_URL = BASE_URL + "signup/";
    public static final String SIGN_IN_URL = BASE_URL + "signin/";
    public static final String SIGN_IN_FACEBOOK_URL = BASE_URL + "facebook_login/";
    public static final String SIGN_IN_TWITTER_URL = BASE_URL + "twitter_login/";
    public static final String SIGN_OUT_URL = BASE_URL + "logout?";
    public static final String INSERT_PROFILE_PICTURE_URL = BASE_URL + "insert_profile_picture/";
    public static final String INSERT_BROADCAST_IMAGE_URL = BASE_URL + "insert_broadcast_image/";
    public static final String GET_USER_PROFILE_URL = BASE_URL + "get_profile_info?";
    public static final String EDIT_PROFILE_URL = BASE_URL + "edit_profile/";
    public static final String SEARCH_BROADCASTS_LIST = BASE_URL + "search_broadcast?";
    public static final String GET_ALL_BROADCASTS = BASE_URL + "getallbroadcasts/";
    public static final String GET_PEOPLE_LIST_URL = BASE_URL + "get_people?";
    public static final String SEARCH_PEOPLE_URL = BASE_URL + "search_user?";
    public static final String GET_WOWZA_SERVER_IP = BASE_URL + "getIp/";
    public static final String START_BROADCAST_URL = BASE_URL + "startbroadcast?";
    public static final String OFFLINE_BROADCAST_URL = BASE_URL + "offline_broadcast/";
    public static final String FOLLOW_USER_URL = BASE_URL + "follow_user?";
    public static final String UNFOLLOW_USER_URL = BASE_URL + "unfollow_user?";
    public static final String TIMESTAMP_BROADCAST_URL = BASE_URL + "update_timestamp_broadcast/";
    public static final String POST_COMMENT = BASE_URL + "post_comment/";

    public static final String BLOCK_USER = BASE_URL + "block_user?";
    public static final String BROADCASTER_BLOCK_USER = BASE_URL + "block_user_broadcast?";
    public static final String UNBLOCK_USER = BASE_URL + "unblock_user?";
    public static final String REPORT_USER = BASE_URL + "report_user?";
    public static final String REPORT_BROADCAST = BASE_URL + "report_broadcast?";
    public static final String JOIN_BROADCAST = BASE_URL + "join_broadcast?";
    public static final String LEFT_BROADCAST = BASE_URL + "left_broadcast?";

    //shared preference keys
    public static final String GCM_REG_KEY = "regIdGcm";

    // Google Project Number
    public static final String GOOGLE_PROJ_ID = "167720339572";
    // Message Key
    public static String GCM_USERNAME_KEY = "msg";

    private Constants() {
        throw new UnsupportedOperationException();
    }
}