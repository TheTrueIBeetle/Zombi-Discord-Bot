package me.zombibot.command.commands.music;

import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

@SuppressWarnings("ConstantConditions")

public class JoinCommand implements ICommand {

    @Override
    public void handle(CommandContext ctx) {
        final TextChannel channel = ctx.getChannel();
        final Member self = ctx.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        final Member member = ctx.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        if (self.hasPermission(Permission.VOICE_CONNECT)) {
            if (selfVoiceState.inVoiceChannel()) {
                channel.sendMessage("I'm already connected to a voice channel").queue();
            }
            else if (!memberVoiceState.inVoiceChannel()){
                channel.sendMessage("You need to be in a voice channel for this command to work").queue();
            }

            final AudioManager audioManager = ctx.getGuild().getAudioManager();
            final VoiceChannel memberChannel = memberVoiceState.getChannel();

            audioManager.openAudioConnection(memberChannel);
            channel.sendMessageFormat("Connecting to `\uD83D\uDD0A %s`", memberChannel.getName()).queue();
        }
        else {
            channel.sendMessage("I don't have permission to join voice channels. Enable my permissions first.").queue();
        }

    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getHelp() {
        return "Lets the bot join your current voice channel";
    }
}
