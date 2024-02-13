package host.plas.essentials;

import lombok.Getter;
import lombok.Setter;
import net.streamline.api.messages.builders.TeleportMessageBuilder;
import net.streamline.api.messages.proxied.ProxiedMessage;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineLocation;
import net.streamline.api.savables.users.StreamlinePlayer;
import net.streamline.api.scheduler.ModuleDelayedRunnable;
import org.jetbrains.annotations.NotNull;
import host.plas.StreamlineUtilities;
import host.plas.events.TPATimeoutEvent;

import java.util.Date;

public class TPARequest implements Comparable<TPARequest> {
    public static class TimeoutTimer extends ModuleDelayedRunnable {
        @Getter
        private final TPARequest request;

        public TimeoutTimer(TPARequest request) {
            super(StreamlineUtilities.getInstance(), StreamlineUtilities.getConfigs().getTPATimeout());
            this.request = request;
        }

        @Override
        public void runDelayed() {
            TPATimeoutEvent event = new TPATimeoutEvent(request).fire();
            if (event.isCancelled()) {
                request.setTimeoutTimer(new TimeoutTimer(request));
                return;
            }

            request.timeout();
        }
    }

    public enum TransportType {
        SENDER_TO_RECEIVER,
        RECEIVER_TO_SENDER,
        ;
    }
    public enum ResolveType {
        LAST_LOCATION,
        CURRENT_LOCATION,
        ;
    }

    @Getter
    private final String senderUuid;
    @Getter
    private final String receiverUuid;
    @Getter
    private final Date timeSent;
    @Getter
    private final TransportType transportType;
    @Getter
    private final ResolveType resolveType;
    @Getter
    private final String server;
    @Getter @Setter
    private TimeoutTimer timeoutTimer;

    public TPARequest(String senderUuid, String receiverUuid, TransportType transportType, ResolveType resolveType, String server) {
        this.senderUuid = senderUuid;
        this.receiverUuid = receiverUuid;
        this.timeSent = new Date();
        this.transportType = transportType;
        this.resolveType = resolveType;
        this.server = server;
        this.timeoutTimer = new TimeoutTimer(this);
    }

    public TPARequest(StreamlinePlayer sender, StreamlinePlayer receiver, TransportType transportType, ResolveType resolveType) {
        this(sender.getUuid(), receiver.getUuid(), transportType, resolveType, receiver.getLatestServer());
    }

    public TPARequest(StreamlinePlayer sender, StreamlinePlayer receiver, TransportType transportType) {
        this(sender, receiver, transportType, ResolveType.CURRENT_LOCATION);
    }

    public boolean isSender(String uuid) {
        return senderUuid.equals(uuid);
    }

    public boolean isReceiver(String uuid) {
        return receiverUuid.equals(uuid);
    }

    public StreamlinePlayer getSender() {
        return ModuleUtils.getOrGetPlayer(senderUuid);
    }

    public StreamlinePlayer getReceiver() {
        return ModuleUtils.getOrGetPlayer(receiverUuid);
    }

    public void perform() {
        StreamlinePlayer from;
        StreamlinePlayer to;

        if (transportType == TransportType.SENDER_TO_RECEIVER) {
            from = getSender();
            to = getReceiver();
        } else {
            from = getReceiver();
            to = getSender();
        }

        if (from == null || to == null) {
            return;
        }

        StreamlineLocation location = to.getLocation();
        StreamlineUtilities.getInstance().logDebug("Performing TPA from " + from.getLatestName() + " to " + to.getLatestName() + " at " + location.toString());
        if (resolveType == ResolveType.CURRENT_LOCATION) {
            ModuleUtils.connect(from, to.getLatestServer());
        } else {
            ModuleUtils.connect(from, getServer());
        }

        ProxiedMessage message = TeleportMessageBuilder.build(to, location, from);
        new EssentialsManager.TeleportRunner(message);
        ModuleUtils.sendMessage(to, ModuleUtils.replaceAllPlayerBungee(to,
                StreamlineUtilities.getMessages().tpaPerformTo()
                        .replace("%this_from%", from.getLatestName())
                        .replace("%this_to%", to.getLatestName())
        ));
        ModuleUtils.sendMessage(from, ModuleUtils.replaceAllPlayerBungee(from,
                StreamlineUtilities.getMessages().tpaPerformFrom()
                        .replace("%this_from%", from.getLatestName())
                        .replace("%this_to%", to.getLatestName())
        ));

        EssentialsManager.removeTPARequest(this);

        this.timeoutTimer.cancel();
    }

    public void deny() {
        EssentialsManager.removeTPARequest(this);

        StreamlinePlayer from;
        StreamlinePlayer to;

        if (transportType == TransportType.SENDER_TO_RECEIVER) {
            from = getSender();
            to = getReceiver();
        } else {
            from = getReceiver();
            to = getSender();
        }

        if (from == null || to == null) {
            return;
        }

        // Maybe make this send a deny message?
//        ModuleUtils.sendMessage(to, ModuleUtils.replaceAllPlayerBungee(to,
//                StreamlineUtilities.getMessages().tpaPerformTo()
//                        .replace("%this_from%", from.getLatestName())
//                        .replace("%this_to%", to.getLatestName())
//        ));
//        ModuleUtils.sendMessage(from, ModuleUtils.replaceAllPlayerBungee(from,
//                StreamlineUtilities.getMessages().tpaPerformFrom()
//                        .replace("%this_from%", from.getLatestName())
//                        .replace("%this_to%", to.getLatestName())
//        ));
    }

    public void timeout() {
        EssentialsManager.removeTPARequest(this);

        StreamlinePlayer from;
        StreamlinePlayer to;

        if (transportType == TransportType.SENDER_TO_RECEIVER) {
            from = getSender();
            to = getReceiver();
        } else {
            from = getReceiver();
            to = getSender();
        }

        if (from == null || to == null) {
            return;
        }

        ModuleUtils.sendMessage(getReceiver(), ModuleUtils.replaceAllPlayerBungee(to,
                StreamlineUtilities.getMessages().tpaTimeoutTo()
                        .replace("%this_from%", from.getLatestName())
                        .replace("%this_sender%", getSender().getLatestName())
                        .replace("%this_to%", to.getLatestName())
                        .replace("%this_receiver%", getReceiver().getLatestName())
        ));
        ModuleUtils.sendMessage(getSender(), ModuleUtils.replaceAllPlayerBungee(from,
                StreamlineUtilities.getMessages().tpaTimeoutFrom()
                        .replace("%this_from%", from.getLatestName())
                        .replace("%this_sender%", getSender().getLatestName())
                        .replace("%this_to%", to.getLatestName())
                        .replace("%this_receiver%", getReceiver().getLatestName())
        ));
    }

    @Override
    public int compareTo(@NotNull TPARequest o) {
        return timeSent.compareTo(o.timeSent);
    }
}
