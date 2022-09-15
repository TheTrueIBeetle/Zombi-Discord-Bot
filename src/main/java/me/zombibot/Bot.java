/*
 * Project Name: ZombiBot
 * File Name: Bot.java
 * Type: Class
 * Author: Luke Bas
 * Date Created: 2021-08-29
 */

package me.zombibot;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.zombibot.event.events.UserLeaveVoice;
import me.zombibot.util.Globals;
import me.zombibot.util.WipeThread;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Bot {

    //Constructor
    private Bot() throws LoginException {

        EventWaiter waiter = new EventWaiter();

        JDABuilder.createDefault(
                Config.get("token"),
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_EMOJIS
        )
                .enableCache(CacheFlag.VOICE_STATE) //Asserting that voice state is enabled
                .addEventListeners(new Listener(waiter), waiter, new UserLeaveVoice())
                .setActivity(Activity.listening("zhelp | 24/7 Music"))
                .build();
    }

    public static void main(String[] args) throws LoginException {
        new Bot();
    }

}
