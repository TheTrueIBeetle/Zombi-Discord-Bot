package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

@SuppressWarnings("ConstantConditions")

public class RepeatCommand implements ICommand {


    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

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

        final GuildMusicManager musicManager = PlayerManager.getInstance().getGuildMusicManager(ctx.getGuild());
        final boolean newRepeating = !musicManager.scheduler.repeating;

        musicManager.scheduler.repeating = newRepeating;

        channel.sendMessageFormat("\uD83D\uDD02 The player has been set to **%s**", newRepeating ? "repeating mode" : "not repeating mode").queue();

    }

    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public String getHelp() {
        return "Loops the current track that is playing and sets player to repeating mode. Use the command repeat again to " +
                "take it out of repeating mode";
    }
}
