package com.app.hopity.player;

import java.util.Locale;

/**
 * Created by Mushi on 9/26/2016.
 */
public class StreamInstance {

    public final String name;
    public final String contentId;
    public final String provider;
    public final String uri;
    public final int type;

    public StreamInstance(String name, String uri, int type) {
        this(name, name.toLowerCase(Locale.US).replaceAll("\\s", ""), "", uri, type);
    }

    public StreamInstance(String name, String contentId, String provider, String uri, int type) {
        this.name = name;
        this.contentId = contentId;
        this.provider = provider;
        this.uri = uri;
        this.type = type;
    }

}
