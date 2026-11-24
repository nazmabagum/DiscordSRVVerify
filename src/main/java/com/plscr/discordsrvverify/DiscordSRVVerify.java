package com.plscr.discordsrvverify;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.commands.PluginSlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommand;
import github.scarsz.discordsrv.api.commands.SlashCommandProvider;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Set;

public final class DiscordSRVVerify extends JavaPlugin implements SlashCommandProvider {
    private String guildId;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        guildId = getConfig().getString("guild-id", "").trim();

        if (!guildId.matches("\\d{17,20}")) {
            getLogger().severe("Invalid guild-id in config.yml. Disabling DiscordSRVVerify.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (DiscordSRV.getPlugin() == null) {
            getLogger().severe("DiscordSRV is not available. Ensure it is installed and enabled first.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Enabled. /verify will be registered only in guild " + guildId + ".");
        getLogger().info("Uses DiscordSRV's existing bot and account-link database; no bot token is required.");
    }

    @Override
    public Set<PluginSlashCommand> getSlashCommands() {
        CommandData command = new CommandData("verify", "Link your Minecraft account using the code shown in-game.")
                .addOptions(new OptionData(
                        OptionType.STRING,
                        "code",
                        "The 4-digit code from /discord link in Minecraft.",
                        true
                ));

        return Collections.singleton(new PluginSlashCommand(this, command, guildId));
    }

    @SlashCommand(path = "verify", deferReply = true, deferEphemeral = true)
    public void onVerify(SlashCommandEvent event) {
        if (event.getGuild() == null || !guildId.equals(event.getGuild().getId())) {
            event.getHook().sendMessage("This command can only be used in the configured Discord server.").queue();
            return;
        }

        String code = event.getOption("code") == null ? "" : event.getOption("code").getAsString().trim();
        if (!code.matches("\\d{4}")) {
            event.getHook().sendMessage("Please enter the 4-digit code shown by `/discord link` in Minecraft.").queue();
            return;
        }

        try {
            AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
            if (manager == null) {
                event.getHook().sendMessage("Account linking is temporarily unavailable. Please try again later.").queue();
                return;
            }

            // This is DiscordSRV's own linking workflow. It consumes the code,
            // stores the link in DiscordSRV's configured backend, fires its link
            // event, and runs its configured linked-role/console-command actions.
            String result = manager.process(code, event.getUser().getId());
            event.getHook().sendMessage(result == null || result.isBlank()
                    ? "Verification request processed."
                    : result).queue();
        } catch (Exception exception) {
            getLogger().severe("Could not process /verify: " + exception.getMessage());
            event.getHook().sendMessage("Verification failed unexpectedly. Please contact a server administrator.").queue();
        }
    }
}
