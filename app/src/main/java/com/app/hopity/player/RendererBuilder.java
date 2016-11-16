package com.app.hopity.player;

/**
 * Created by Mushi on 9/26/2016.
 */

/**
 * Builds renderers for the player.
 */
public interface RendererBuilder {
    /**
     * Builds renderers for playback.
     *
     * @param player The player for which renderers are being built. {@link DemoPlayer#onRenderers}
     *               should be invoked once the renderers have been built. If building fails,
     *               {@link DemoPlayer#onRenderersError} should be invoked.
     */
    void buildRenderers(DemoPlayer player);

    /**
     * Cancels the current build operation, if there is one. Else does nothing.
     * <p>
     * A canceled build operation must not invoke {@link DemoPlayer#onRenderers} or
     * {@link DemoPlayer#onRenderersError} on the player, which may have been released.
     */
    void cancel();
}
