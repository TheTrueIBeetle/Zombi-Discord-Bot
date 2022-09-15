package me.zombibot.command.commands;

import me.zombibot.CommandManager;
import me.zombibot.Config;
import me.zombibot.command.CommandContext;
import me.zombibot.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public class HelpCommand implements ICommand {

    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(CommandContext ctx) {
        List<String> args = ctx.getArgs();
        TextChannel channel = ctx.getChannel();

        if (!args.isEmpty()) {
            String search = args.get(0);
            ICommand command = manager.getCommand(search);
            if (command == null) {
                channel.sendMessage("Nothing found for " + search).queue();
            }
            else {
                channel.sendMessage(command.getHelp()).queue();
            }
        }
        else {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("List of commands");
            eb.setDescription("`Use 'z' as the command prefix`");
            eb.setColor(0x672f80);
            List<ICommand> cmds = manager.getCommands();
            int count = 0;
            while (count != cmds.size()) {
                if (eb.length() > 1500) { //Accounts for the Discord character limit
                    channel.sendMessageEmbeds(eb.build()).queue();
                    eb.clearFields();
                }
                else {
                    eb.addField(cmds.get(count).getName(), cmds.get(count).getHelp(), false);
                }
                count++;
            }
            channel.sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getHelp() {
        return "Displays the list of commands for Zombi or the specific command's details\n" +
                "Usage: `zhelp` OR `zhelp [command]`";
    }

    @Override
    public List<String> getAliases(){
        //return List.of("commands", "cmds", "cmd", "commandlist", "cmdlist");
        return Arrays.asList("commands", "cmds", "cmd", "commandlist", "cmdlist");
    }
}
