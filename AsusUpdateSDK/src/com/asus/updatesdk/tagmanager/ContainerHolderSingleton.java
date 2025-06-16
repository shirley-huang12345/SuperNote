package com.asus.updatesdk.tagmanager;

import com.google.android.gms.tagmanager.ContainerHolder;

/**
 * Singleton to hold the GTM Container (since it should be only created once
 * per run of the app).
 */
public class ContainerHolderSingleton {
    private static ContainerHolder sContainerHolder;

    /**
     * Utility class; don't instantiate.
     */
    private ContainerHolderSingleton() {
    }

    public static ContainerHolder getContainerHolder() {
        return sContainerHolder;
    }

    public static void setContainerHolder(ContainerHolder containerHolder) {
        sContainerHolder = containerHolder;
    }
}