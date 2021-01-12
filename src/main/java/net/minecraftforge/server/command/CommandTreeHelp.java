/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.server.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Add help for parent and all its children.
 * Must be added to parent after all other commands.
 */
public class CommandTreeHelp extends CommandTreeBase
{
    private final ICommand parent;

    public CommandTreeHelp(CommandTreeBase parent)
    {
        this.parent = parent;
        for (ICommand command : parent.getSubCommands())
        {
            addSubcommand(new HelpSubCommand(this, command));
        }
    }

    @Override
    public int func_82362_a()
    {
        return 0;
    }

    @Override
    public String func_71517_b()
    {
        return "help";
    }

    @Override
    public String func_71518_a(ICommandSender sender)
    {
        return "commands.forge.usage.help";
    }

    @Override
    public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.func_145747_a(TextComponentHelper.createComponentTranslation(sender, parent.func_71518_a(sender)));
            for (ICommand subCommand : getSubCommands())
            {
                if (subCommand instanceof HelpSubCommand && subCommand.func_184882_a(server, sender))
                {
                    subCommand.func_184881_a(server, sender, args);
                }
            }
            return;
        }
        super.func_184881_a(server, sender, args);
    }

    public static class HelpSubCommand extends CommandBase
    {
        private final CommandTreeHelp parent;
        private final ICommand command;

        public HelpSubCommand(CommandTreeHelp parent, ICommand command)
        {
            this.parent = parent;
            this.command = command;
        }

        @Override
        public int func_82362_a()
        {
            return 0;
        }

        @Override
        public String func_71517_b()
        {
            return command.func_71517_b();
        }

        @Override
        public String func_71518_a(ICommandSender sender)
        {
            return command.func_71518_a(sender);
        }

        @Override
        public boolean func_184882_a(MinecraftServer server, ICommandSender sender)
        {
            return command.func_184882_a(server, sender);
        }

        @Override
        public void func_184881_a(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
        {
            sender.func_145747_a(TextComponentHelper.createComponentTranslation(sender, command.func_71518_a(sender)));
        }
    }
}
