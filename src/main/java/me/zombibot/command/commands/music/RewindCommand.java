package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

@SuppressWarnings("ConstantConditions")

public class RewindCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            channel.sendMessage("Please provide a duration to rewind (in seconds)").queue();
            return;
        }

        long durationSeconds = 0;
        try {
            durationSeconds = Long.parseLong(args.get(0));
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
            channel.sendMessage("I can only interpret numbers for this command. Trying using just the number in seconds for example: " +
                    "`zrewind 15`").queue();
            return;
        }

        final long durationMilis = durationSeconds * 1000;
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        PlayerManager playerManager = PlayerManager.getInstance();
        GuildMusicManager musicManager = playerManager.getGuildMusicManager(ctx.getGuild());

        if (!selfVoiceState.inVoiceChannel()) {
            channel.sendMessage("I need to be in a voice channel before I can do this").queue();
            return;
        }

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inVoiceChannel()) {
            channel.sendMessage("Please join yourself to a voice channel first before using this").queue();
            return;
        }

        if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            channel.sendMessage("You must be in the same voice channel as me in order to use this").queue();
            return;
        }

        musicManager.player.getPlayingTrack().setPosition(musicManager.player.getPlayingTrack().getPosition() - durationMilis);
        channel.sendMessageFormat("\u23EA Rewound **%s** ", String.valueOf(durationSeconds) + " seconds back in the track").queue();

    }

    @Override
    public String getName() {
        return "rewind";
    }

    @Override
    public String getHelp() {
        return "Rewinds the current track by an amount in seconds" +
                "\nUsage: `zrewind [duration in seconds]`";
    }
}
