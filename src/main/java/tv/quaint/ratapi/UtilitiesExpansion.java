package tv.quaint.ratapi;

import net.streamline.api.configs.given.GivenConfigs;
import net.streamline.api.configs.given.MainMessagesHandler;
import net.streamline.api.objects.AtomicString;
import net.streamline.api.placeholders.expansions.RATExpansion;
import net.streamline.api.placeholders.replaceables.IdentifiedReplaceable;
import tv.quaint.StreamlineUtilities;
import tv.quaint.configs.obj.PermissionGroup;
import tv.quaint.utils.MatcherUtils;

public class UtilitiesExpansion extends RATExpansion {
    public UtilitiesExpansion() {
        super(new RATExpansionBuilder("utils"));
    }

    @Override
    public void init() {
        new IdentifiedReplaceable(this, MatcherUtils.makeLiteral("group_") + "(.*?)", 1, (s) -> {
            AtomicString string = new AtomicString(s.string());
            s.handledString().isolateIn(s.string()).forEach(str -> {
                string.set(startsWithGroup(str));
            });
            return string.get() == null ? s.string() : string.get();
        }).register();

        new IdentifiedReplaceable(this, "maintenance_mode", (s) -> StreamlineUtilities.getMaintenanceConfig().isModeEnabled()
                ? MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() : MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get()).register();
        new IdentifiedReplaceable(this, "maintenance_message", (s) -> StreamlineUtilities.getMaintenanceConfig().getModeKickMessage()).register();

        new IdentifiedReplaceable(this, "whitelist_mode", (s) -> GivenConfigs.getWhitelistConfig().isEnabled()
                ? MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_TRUE.get() : MainMessagesHandler.MESSAGES.DEFAULTS.PLACEHOLDERS.IS_FALSE.get()).register();
        new IdentifiedReplaceable(this, "whitelist_message", (s) -> MainMessagesHandler.MESSAGES.INVALID.WHITELIST_NOT.get()).register();

        new IdentifiedReplaceable(this, MatcherUtils.makeLiteral("server_alias_") + "(.*?)", 1, (s) -> {
            AtomicString string = new AtomicString(s.string());
            s.handledString().isolateIn(s.string()).forEach(str -> {
                string.set(StreamlineUtilities.getServerAliasesConfig().getActualName(str));
            });
            return string.get() == null ? s.string() : string.get();
        }).register();
    }

    public String startsWithGroup(String s) {
        String identifier = s;
        if (identifier.contains("_")) identifier = identifier.substring(0, identifier.indexOf("_"));
        PermissionGroup group = StreamlineUtilities.getGroupedPermissionConfig().getPermissionGroups().get(identifier);
        if (group == null) return null;
        if (s.equals(identifier + "_name")) {
            return group.name();
        }
        if (s.equals(identifier + "_permission")) {
            return group.permission();
        }
        return null;
    }
}
