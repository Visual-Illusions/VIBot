package net.visualillusionsent.vibot.api.events;

import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.dcc.DccFileTransfer;

public abstract class FileTransferFinishedEvent extends BaseEvent {

    public FileTransferFinishedEvent(BotPlugin plugin) {
        super(plugin, EventType.FILE_TRANSFER_FINISHED);
    }

    public FileTransferFinishedEvent(BotPlugin plugin, EventPriority priority) {
        super(plugin, priority, EventType.FILE_TRANSFER_FINISHED);
    }

    public abstract void execute(DccFileTransfer transfer, Exception ex);

}
