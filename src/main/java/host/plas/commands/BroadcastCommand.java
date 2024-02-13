package host.plas.commands;

import lombok.Getter;
import net.streamline.api.command.ModuleCommand;
import net.streamline.api.modules.ModuleUtils;
import net.streamline.api.savables.users.StreamlineUser;
import host.plas.StreamlineUtilities;

import java.util.concurrent.ConcurrentSkipListSet;

public class BroadcastCommand extends ModuleCommand {
    @Getter
    private final String messageResultAll;

    public BroadcastCommand() {
        super(StreamlineUtilities.getInstance(),
                "proxybroadcast",
                "streamline.command.broadcast.default",
                "pb", "proxyb", "pbroadcast"
        );

        messageResultAll = getCommandResource().getOrSetDefault("messages.result.all", "&c&lBROADCAST &7&l>> &r%this_message%");
    }

    @Override
    public void run(StreamlineUser StreamlineUser, String[] strings) {
        String message = ModuleUtils.argsToString(strings);

        boolean isCustom = false;
        if (message.startsWith("!")) {
            isCustom = true;
            message = message.substring(1);
        }

        final String finalMessage = message;
        boolean finalIsCustom = isCustom;
        ModuleUtils.getLoadedUsersSet().forEach(a -> {
            if (! finalIsCustom) {
                ModuleUtils.sendMessage(a, messageResultAll
                        .replace("%this_message%", finalMessage)
                        .replace("%this_sender%", StreamlineUser.getName())
                );
            } else {
                ModuleUtils.sendMessage(a, finalMessage
                        .replace("%this_sender%", StreamlineUser.getName())
                );
            }
        });
    }

    @Override
    public ConcurrentSkipListSet<String> doTabComplete(StreamlineUser StreamlineUser, String[] strings) {
        return new ConcurrentSkipListSet<>();
    }
}
