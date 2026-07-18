package net.otsutsukimiho.nozomiaddon.utils.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CancellableEvent extends Event {
    private static final String MOD_ID = "nozomiaddon";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean isCancelled = false;

    public boolean isCancelled() {
        return isCancelled;
    }

    public void cancel() {
        isCancelled = true;
    }

    @Override
    public boolean postAndCatch() {
        try {
            EventBus.post(this);
        } catch (Throwable t) {
            LOGGER.error("Error posting event " + this.getClass().getSimpleName(), t);
        }
        return isCancelled;
    }
}