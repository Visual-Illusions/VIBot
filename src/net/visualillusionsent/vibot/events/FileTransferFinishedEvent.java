package net.visualillusionsent.vibot.events;

import net.visualillusionsent.vibot.io.DccFileTransfer;
import net.visualillusionsent.vibot.plugin.BotPlugin;

public abstract class FileTransferFinishedEvent extends BaseEvent {

    public FileTransferFinishedEvent(BotPlugin plugin) {
        super(plugin, EventType.FILE_TRANSFER_FINISHED);
    }

    public FileTransferFinishedEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.FILE_TRANSFER_FINISHED);
    }

    public abstract void execute(DccFileTransfer transfer, Exception ex);

}
