package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

@SuppressWarnings("ConstantConditions")

public class VolumeCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        final TextChannel channel = ctx.getChannel();

        if (args.isEmpty()) {
            channel.sendMessage("Please provide a volume level").queue();
            return;
        }

        int volumeLevel = 0;
        try {
            volumeLevel = Integer.parseInt(args.get(0));
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
            channel.sendMessage("I can only interpret numbers for this command. Trying using just the number for example: " +
                    "`zvolume 100`").queue();
            return;
        }

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

        musicManager.player.setVolume(volumeLevel);
        channel.sendMessageFormat("Volume has been set to **%s**", String.valueOf(volumeLevel)).queue();


    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getHelp() {
        return "Sets the player volume to specified level" +
                "\nUsage: `zvolume [volume level]` ... 100: Normal | 200+ Loud";
    }
}
