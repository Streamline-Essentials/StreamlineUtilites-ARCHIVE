package tv.quaint.configs;

import net.streamline.api.configs.ModularizedConfig;
import tv.quaint.StreamlineUtilities;

public class Messages extends ModularizedConfig {
    public Messages() {
        super(StreamlineUtilities.getInstance(), "messages.yml", true);
    }

    @Override
    public void init() {
        errorsFunctionsNotEnabled();
        errorsFunctionsNotLoaded();

        tpaPerformTo();
        tpaPerformFrom();
    }

    public String errorsFunctionsNotLoaded() {
        reloadResource();

        return getResource().getOrSetDefault("errors.functions.not.loaded", "&cThat function is not loaded!");
    }

    public String errorsFunctionsNotEnabled() {
        reloadResource();

        return getResource().getOrSetDefault("errors.functions.not.enabled", "&cThat function is not enabled!");
    }

    public String tpaPerformTo() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.perform.to", "&d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*% &ewas teleported to you&8!");
    }

    public String tpaPerformFrom() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.perform.from", "&eYou were teleported to &e%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*%&8!");
    }

    public String tpaTimeoutTo() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.timeout.to", "&eTeleport request from &d%streamline_parse_%this_from%:::*/*streamline_user_formatted*/*% &ctimed out&8!");
    }

    public String tpaTimeoutFrom() {
        reloadResource();

        return getResource().getOrSetDefault("tpa.timeout.from", "&eTeleport request to &d%streamline_parse_%this_to%:::*/*streamline_user_formatted*/*% &ctimed out&8!");
    }
}
