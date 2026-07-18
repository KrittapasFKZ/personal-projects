package net.otsutsukimiho.nozomiaddon.utils.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Event {
    private static final String MOD_ID = "nozomiaddon";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public boolean postAndCatch() {
        try {
            EventBus.post(this);
        } catch (Throwable t) {
            LOGGER.error("Error posting event " + this.getClass().getSimpleName(), t);
        }
        return false;
    }
}