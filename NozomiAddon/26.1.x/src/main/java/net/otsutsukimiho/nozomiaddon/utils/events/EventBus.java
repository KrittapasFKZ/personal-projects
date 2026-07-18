package net.otsutsukimiho.nozomiaddon.utils.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public class EventBus {
    private static final String MOD_ID = "nozomiaddon";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Map<Class<? extends Event>, EventListener[]> listenerMap = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Event>, String> profilerNames = new ConcurrentHashMap<>();

    public static <T extends Event> void post(T event) {
        EventListener[] listeners = listenerMap.get(event.getClass());

        if (listeners == null || listeners.length == 0) return;

        ProfilerFiller profiler = Profiler.get();
        String profilerName = profilerNames.computeIfAbsent(event.getClass(), k -> "NA: " + k.getSimpleName());

        profiler.push(profilerName);
        try {
            for (EventListener listener : listeners) {
                if (event instanceof CancellableEvent cancellable) {
                    if (!listener.ignoreCancelled && cancellable.isCancelled()) {
                        continue;
                    }
                }

                try {
                    @SuppressWarnings("unchecked")
                    Consumer<T> castedHandler = (Consumer<T>) listener.handler;
                    castedHandler.accept(event);
                } catch (Exception e) {
                    LOGGER.error("Error dispatching event to " + listener.name, e);
                }
            }
        } finally {
            profiler.pop();
        }
    }

    public static <T extends Event> void register(
            Class<?> subscriberClass,
            Class<T> eventClass,
            Consumer<T> handler
    ) {
        register(subscriberClass, eventClass, 0, false, handler);
    }

    public static <T extends Event> void register(
            Class<?> subscriberClass,
            Class<T> eventClass,
            int priority,
            boolean ignoreCancelled,
            Consumer<T> handler
    ) {
        String name = subscriberClass.getSimpleName();
        EventListener newListener = new EventListener(name, priority, ignoreCancelled, handler);

        listenerMap.compute(eventClass, (k, currentArray) -> {
            if (currentArray == null) {
                return new EventListener[]{newListener};
            } else {
                List<EventListener> list = new ArrayList<>(Arrays.asList(currentArray));
                list.add(newListener);

                list.sort((a, b) -> Integer.compare(b.priority, a.priority));

                return list.toArray(new EventListener[0]);
            }
        });
    }

    private static class EventListener {
        final String name;
        final int priority;
        final boolean ignoreCancelled;
        final Consumer<?> handler;

        EventListener(String name, int priority, boolean ignoreCancelled, Consumer<?> handler) {
            this.name = name;
            this.priority = priority;
            this.ignoreCancelled = ignoreCancelled;
            this.handler = handler;
        }
    }
}