/*
 * Project Name: ZombiBot
 * File Name: Config.java
 * Type: Class
 * Author: Luke Bas
 * Date Created: 2021-08-29
 */

package me.zombibot;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    private static final Dotenv dotenv = Dotenv.load();

    public static String get(String key){
        return dotenv.get(key.toUpperCase());
    }
}
