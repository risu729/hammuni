/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.google.common.base.Preconditions.checkNotNull;

@Configuration
public class JDAConfiguration {

  @Bean
  public @NotNull JDA jda(@Value("${hammuni.bot.token}") @NotNull String discordToken,
      @Value("${hammuni.bot.guild}") @NotNull String guildId, @NotNull Listener listener,
      @NotNull VoiceListener voiceListener, @NotNull CommandListener commandListener)
      throws InterruptedException {
    var jda = JDABuilder.createLight(discordToken,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS)
        .enableCache(CacheFlag.VOICE_STATE)
        .setMemberCachePolicy(MemberCachePolicy.VOICE)
        .setActivity(Activity.playing("はむにりす vs あざらし"))
        .addEventListeners(listener, voiceListener, commandListener)
        .build();
    jda.awaitReady();
    // グローバルコマンドを消去する
    jda.updateCommands().queue();
    // サーバーコマンドとして追加する
    checkNotNull(jda.getGuildById(guildId)).updateCommands()
        .addCommands(commandListener.commands())
        .queue();
    // 特定のサーバー以外から脱退する
    jda.getGuilds()
        .stream()
        .filter(guild -> !guild.getId().equals(guildId))
        .map(Guild::leave)
        .forEach(RestAction::queue);
    voiceListener.setJda(jda);
    return jda;
  }
}
