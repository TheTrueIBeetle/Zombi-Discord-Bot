package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;

public class PauseCommand implements ICommand {


    @Override
    public void handle(CommandContext ctx) {
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());

        musicManager.player.setPaused(true);
        ctx.getChannel().sendMessage("Paused the music player").queue();
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getHelp() {
        return "Pauses the music player";
    }
}
