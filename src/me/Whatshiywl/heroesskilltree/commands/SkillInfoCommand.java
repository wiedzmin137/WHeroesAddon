package me.Whatshiywl.heroesskilltree.commands;

import me.Whatshiywl.heroesskilltree.HeroesSkillTree;
import me.Wiedzmin137.wheroesaddon.WAddonCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;

public class SkillInfoCommand {
   //TODO create lanugage support
   public static void skillInfo(HeroesSkillTree hst, CommandSender sender, String[] args) {
      if(!sender.hasPermission("skilltree.info")) {
         sender.sendMessage(ChatColor.RED + "You don\'t have enough permissions!");
      } else if(args.length < 2) {
         sender.sendMessage(ChatColor.RED + "/skillinfo (skill)");
      } else if(!(sender instanceof Player)) {
         sender.sendMessage(ChatColor.RED + "You must be in game to use this command");
      } else {
         Hero hero = WAddonCore.heroes.getCharacterManager().getHero((Player)sender);
         if(!hero.hasAccessToSkill(args[1])) {
            sender.sendMessage(ChatColor.RED + "You don\'t have this skill");
         } else {
            Skill skill = WAddonCore.heroes.getSkillManager().getSkill(args[1]);
            sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + skill.getName() + "\'s info:");
            if(hst.isLocked(hero, skill)) {
               sender.sendMessage(ChatColor.RED + "This skill is currently locked!");
               if(hst.getStrongParentSkills(hero, skill) != null) {
                  sender.sendMessage(ChatColor.AQUA + "Requires Strong: " + hst.getStrongParentSkills(hero, skill).toString());
               }

               if(hst.getWeakParentSkills(hero, skill) != null) {
                  sender.sendMessage(ChatColor.AQUA + "Requires Weak: " + hst.getWeakParentSkills(hero, skill).toString());
               }
            } else if(hst.isMastered(hero, skill)) {
               sender.sendMessage(ChatColor.GREEN + "This skill has been mastered at level " + hst.getSkillLevel(hero, skill) + "!");
            } else {
               sender.sendMessage(ChatColor.AQUA + "Level: " + hst.getSkillLevel(hero, skill) + "/" + hst.getSkillMaxLevel(hero, skill));
            }

            if(sender.hasPermission("skilltree.points")) {
               sender.sendMessage(ChatColor.GOLD + "[HST] " + ChatColor.AQUA + "You currently have " + hst.getPlayerPoints(hero) + " SkillPoints.");
            }
         }
      }
   }
}
