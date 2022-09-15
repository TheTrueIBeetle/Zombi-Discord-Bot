package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public class SkipCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        TextChannel channel = ctx.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        TrackScheduler scheduler = musicManager.scheduler;
        AudioPlayer player = musicManager.player;

        if (player.getPlayingTrack() == null){
            channel.sendMessage("The player isn't currently playing anything").queue();
            return;
        }
        try {
            scheduler.nextTrack();
        }
        catch (FriendlyException ex){
            channel.sendMessage("Yikes..something went wrong. The queue and tracks have been reset.").queue();
            return;
        }


        channel.sendMessage("\u23ED Skipping the current track...").queue();
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getHelp() {
        return "Skips the current song and plays the next in queue";
    }

    @Override
    public List<String> getAliases(){
        //return List.of("next");
        return Arrays.asList("next");
    }
}
