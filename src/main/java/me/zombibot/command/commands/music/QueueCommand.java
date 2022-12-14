package me.zombibot.command.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

public class QueueCommand implements ICommand {
    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();


        if (queue.isEmpty()){
            channel.sendMessage("The queue is currently empty").queue();
            return;
        }

        int trackCount = Math.min(queue.size(), 20);
        List<AudioTrack> tracks = new ArrayList<>(queue);
        EmbedBuilder builder = EmbedUtils.getDefaultEmbed()
                .setTitle("Current Queue (Total: " + queue.size() + ")")
                .setDescription("Shuffle the queue with `zshuffle`\n\n")
                .setFooter(Config.get("latest_update"))
                .setColor(0x672f80);

        channel.sendMessage("Showing only the first 20 tracks: ").queue();

        for (int i = 0; i < trackCount; i++){
            AudioTrack track = tracks.get(i);
            AudioTrackInfo info = track.getInfo();

            builder.appendDescription("**" + String.valueOf(i + 1) + ". " + "**");
            builder.appendDescription(String.format(
                    "%s - %s\n",
                    info.title,
                    info.author
            ));
        }
        channel.sendMessage(builder.build()).setActionRow(Button.primary("btn_shuffle", "Shuffle"),
                Button.primary("btn_moveltf", "Move Last To First"),
                Button.danger("btn_clear", "Clear Queue")).queue();
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getHelp() {
        return "Shows current queue for the music player";
    }
}
