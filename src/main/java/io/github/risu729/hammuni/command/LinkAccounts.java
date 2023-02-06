/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni.command;

import io.github.risu729.hammuni.PointApi;
import io.github.risu729.hammuni.util.CommandUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public final class LinkAccounts extends ExecutableCommandData {

  @NotNull PointApi pointApi;

  public LinkAccounts(@NotNull PointApi pointApi) {
    super(Commands.slash("link", "複数アカウントを紐づける")
        .addOptions(CommandUtil.createVarArgsOption(new OptionData(OptionType.USER,
            "user",
            "紐づけるユーザー",
            true), 2, 3))
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
    this.pointApi = pointApi;
  }

  @Override
  public void execute(@NotNull InteractionHook hook,
      @NotNull GenericCommandInteractionEvent event) {
    Set<String> setForDistinct = new HashSet<>();
    var members = event.getOptions()
        .stream()
        .map(OptionMapping::getAsMember)
        .filter(Objects::nonNull)
        .filter(member -> setForDistinct.add(member.getUser().getId()))
        .toList();
    var users = members.stream().map(Member::getUser).toList();
    checkArgument(users.stream().noneMatch(User::isBot));
    pointApi.linkUsers(users);
    hook.sendMessageEmbeds(new EmbedBuilder().setTitle("link")
        .setDescription("アカウントを紐づけました")
        .addField("Users",
            members.stream().map(Member::getEffectiveName).collect(Collectors.joining("\n")),
            false)
        .build()).queue();
  }
}
