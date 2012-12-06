package net.visualillusionsent.vibot.events;

import java.util.Comparator;

public final class EventPriorityComparator implements Comparator<BaseEvent> {

    public EventPriorityComparator() {}

    @Override
    public final int compare(BaseEvent a, BaseEvent b) {
        int ax = a.getPriority().ordinal();
        int bx = b.getPriority().ordinal();

        if (ax > bx) {
            return 1;
        }
        else if (ax == bx) {
            return 0;
        }
        else {
            return -1;
        }
    }
}
