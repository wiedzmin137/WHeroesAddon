package me.Whatshiywl.heroesskilltree;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.Whatshiywl.heroesskilltree.EventListener;
import me.Whatshiywl.heroesskilltree.commands.SkillAdminCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillDownCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillInfoCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillListCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillLockedCommand;
import me.Whatshiywl.heroesskilltree.commands.SkillUpCommand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class HeroesSkillTree extends JavaPlugin {

   public final int VERSION = 1;
   public final double SUBVERSION = 5.2D;
   public static final Logger logger = Logger.getLogger("Minecraft");
   public final EventListener HEventListener = new EventListener(this);
   public HeroesSkillTree plugin;
   public static Heroes heroes = (Heroes)Bukkit.getServer().getPluginManager().getPlugin("Heroes");
   public List<Skill> SkillStrongParents = new ArrayList();
   public List<Skill> SkillWeakParents = new ArrayList();
   public static YamlConfiguration LANG;
   public static File LANG_FILE;
   public static Logger LOG;
   private HashMap<String, HashMap<String, HashMap<String, Integer>>> playerSkills = new HashMap();
   private HashMap<String, HashMap<String, Integer>> playerClasses = new HashMap();
   private int pointsPerLevel = 1;
   private HashMap<String, FileConfiguration> hConfigs = new HashMap();
   

   public void onEnable() {
      PluginManager pm = this.getServer().getPluginManager();
      this.getConfig().options().copyDefaults(true);
      this.saveConfig();
      this.loadConfig();
      loadLang();
      LOG = getServer().getLogger();
      pm.registerEvents(this.HEventListener, this);
      logger.info(Lang.CONSOLE_ENABLING.toString());
      Player[] var6;
      int var5 = (var6 = Bukkit.getServer().getOnlinePlayers()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Player player = var6[var4];
         Hero hero = heroes.getCharacterManager().getHero(player);
         this.recalcPlayerPoints(hero, hero.getHeroClass());
      }
   }
   
   public void onDisable() {
	      this.saveAll();
	      logger.info(Lang.CONSOLE_DISABLING.toString());
	      
	      LANG = null;
	      LANG_FILE = null;
	   }

   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
      Hero hero = heroes.getCharacterManager().getHero((Player)sender);
      String skillPoints = String.valueOf(this.getPlayerPoints(hero));
	   
      if(commandLabel.equalsIgnoreCase("skillup")) {
         SkillUpCommand.skillUp(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skilldown")) {
         SkillDownCommand.skillDown(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillinfo")) {
         SkillInfoCommand.skillInfo(this, sender, args);
         return true;
      } else if(commandLabel.equalsIgnoreCase("skillpoints")) {
         if(!(sender instanceof Player)) {
        	 sender.sendMessage(Lang.ERROR_IN_CONSOLE_DENIED.toString());
            return true;
         } else {
            if(sender.hasPermission("skilltree.points")) {
               sender.sendMessage(Lang.TITLE.toString() + Lang.INFO_SKILLPOINTS.toString().replace("%points", skillPoints));
            } else {
               sender.sendMessage(Lang.TITLE.toString() + Lang.ERROR_PERMISSION_DENIED);
            }

            return true;
         }
      } else if(commandLabel.equalsIgnoreCase("skilladmin")) {
         SkillAdminCommand.skillAdmin(this, sender, args);
         return true;
      } else if(!commandLabel.equalsIgnoreCase("slist") && !commandLabel.equalsIgnoreCase("sl")) {
         if(!commandLabel.equalsIgnoreCase("unlocks") && !commandLabel.equalsIgnoreCase("un")) {
        	sender.sendMessage(Lang.HELP_1.toString());
        	sender.sendMessage(Lang.HELP_2.toString());
        	sender.sendMessage(Lang.HELP_3.toString());
        	sender.sendMessage(Lang.HELP_4.toString());
        	sender.sendMessage(Lang.HELP_5.toString());
        	sender.sendMessage(Lang.HELP_6.toString());
        	sender.sendMessage(Lang.HELP_7.toString());
        	sender.sendMessage(Lang.INFO_SKILLPOINTS.toString().replace("%points", skillPoints));
            return true;
         } else {
            SkillLockedCommand.skillList(this, sender, args);
            return true;
         }
      } else {
         SkillListCommand.skillList(this, sender, args);
         return true;
      }
   }

   public void resetPlayer(Player player) {
      String name = player.getName();
      this.playerSkills.put(name, new HashMap());
      this.playerClasses.put(name, new HashMap());
      this.resetPlayerConfig(name);
   }

   private void resetPlayerConfig(String name) {
      File playerFolder = new File(this.getDataFolder(), "data");
      if(!playerFolder.exists()) {
         playerFolder.mkdir();
      }

      File playerFile = new File(playerFolder, name + ".yml");
      if(playerFolder.exists() && !playerFolder.delete()) {
         logger.log(Level.SEVERE, Lang.SERVRE_FAILED_DELETE.toString().replace("%name%", name));
      } else {
         try {
            playerFile.createNewFile();
         } catch (IOException var5) {
            logger.log(Level.SEVERE, Lang.SERVRE_FAILED_CREATE.toString().replace("%name%", name));
         }

      }
   }

   public int getPlayerPoints(Hero hero) {
      return this.playerClasses.get(hero.getPlayer().getName()) != null && ((HashMap)this.playerClasses.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName()) != null?((Integer)((HashMap)this.playerClasses.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).intValue():0;
   }

   public void recalcPlayerPoints(Hero hero, HeroClass hClass)   {
	    String name = hero.getPlayer().getName();
	    String className = hClass.getName();
	    int points = hero.getLevel(hClass) * getPointsPerLevel();
	    if (this.playerClasses.get(name) == null) {
	      this.playerClasses.put(name, new HashMap());
	    }
	    if (hero.getPlayer().hasPermission("skilltree.override.usepoints"))
	    {
	      ((HashMap)this.playerClasses.get(name)).put(className, Integer.valueOf(points));
	      return;
	    }
	    if (((HashMap)this.playerClasses.get(name)).get(className) == null)
	    {
	      ((HashMap)this.playerClasses.get(name)).put(className, Integer.valueOf(0));
	      return;
	    }
	    if (this.playerSkills.get(name) == null)
	    {
	      this.playerSkills.put(name, new HashMap());
	      return;
	    }
	    if (((HashMap)this.playerSkills.get(name)).get(className) == null)
	    {
	      ((HashMap)this.playerSkills.get(name)).put(className, new HashMap());
	      return;
	    }
	    for (Skill skill : heroes.getSkillManager().getSkills())
	    {
	      String skillName = skill.getName();
	      if (((HashMap)((HashMap)this.playerSkills.get(name)).get(className)).get(skillName) != null)
	      {
	        points -= ((Integer)((HashMap)((HashMap)this.playerSkills.get(name)).get(className)).get(skillName)).intValue();
	        if (points < 0)
	        {
	          logger.warning("[HeroesSkillTree] " + name + "'s skills are at a too high level!");
	          points = 0;
	        }
	      }
	    }
	    ((HashMap)this.playerClasses.get(name)).put(className, Integer.valueOf(points));
	  }

   public void setPlayerPoints(Hero hero, int i) {
      if(this.playerClasses.get(hero.getPlayer().getName()) == null) {
         this.playerClasses.put(hero.getPlayer().getName(), new HashMap());
      }

      ((HashMap)this.playerClasses.get(hero.getPlayer().getName())).put(hero.getHeroClass().getName(), Integer.valueOf(i));
   }

   public void setPlayerPoints(Hero hero, HeroClass hClass, int i) {
      if(this.playerClasses.get(hero.getPlayer().getName()) == null) {
         this.playerClasses.put(hero.getPlayer().getName(), new HashMap());
      }

      ((HashMap)this.playerClasses.get(hero.getPlayer().getName())).put(hClass.getName(), Integer.valueOf(i));
   }

   public int getSkillLevel(Hero hero, Skill skill) {
      return this.playerSkills.get(hero.getPlayer().getName()) != null && ((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName()) != null && ((HashMap)((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).get(skill.getName()) != null?((Integer)((HashMap)((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).get(skill.getName())).intValue():0;
   }

   public void setSkillLevel(Hero hero, Skill skill, int i) {
      if(this.playerSkills.get(hero.getPlayer().getName()) == null) {
         this.playerSkills.put(hero.getPlayer().getName(), new HashMap());
      }

      if(((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName()) == null) {
         ((HashMap)this.playerSkills.get(hero.getPlayer().getName())).put(hero.getHeroClass().getName(), new HashMap());
      }

      ((HashMap)((HashMap)this.playerSkills.get(hero.getPlayer().getName())).get(hero.getHeroClass().getName())).put(skill.getName(), Integer.valueOf(i));
   }

   public int getSkillMaxLevel(Hero hero, Skill skill) {
      return SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1) == -1?SkillConfigManager.getUseSetting(hero, skill, "max-level", -1, false):SkillConfigManager.getSetting(hero.getHeroClass(), skill, "max-level", -1);
   }

   public List<String> getStrongParentSkills(Hero hero, Skill skill) {
      return this.getParentSkills(hero, skill, "strong");
   }

   public List<String> getWeakParentSkills(Hero hero, Skill skill) {
      return this.getParentSkills(hero, skill, "weak");
   }

   public List<String> getParentSkills(Hero hero, Skill skill, String weakOrStrong) {
      FileConfiguration hCConfig = this.getHeroesClassConfig(hero.getHeroClass());
      return hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents") == null?null:hCConfig.getConfigurationSection("permitted-skills." + skill.getName() + ".parents").getStringList(weakOrStrong);
   }

   public boolean isLocked(Hero hero, Skill skill) {
      if(skill != null && hero.canUseSkill(skill)) {
         boolean skillLevel = this.getSkillLevel(hero, skill) < 1;
         List<String> strongParents = this.getStrongParentSkills(hero, skill);
         boolean hasStrongParents = strongParents != null && !strongParents.isEmpty();
         List<String> weakParents = this.getWeakParentSkills(hero, skill);
         boolean hasWeakParents = weakParents != null && !weakParents.isEmpty();
         return skillLevel && (hasStrongParents || hasWeakParents);
      } else {
         return true;
      }
   }

   public boolean isMastered(Hero hero, Skill skill) {
      return hero.hasAccessToSkill(skill)?this.getSkillLevel(hero, skill) >= this.getSkillMaxLevel(hero, skill):false;
   }

   public boolean canUnlock(Hero hero, Skill skill)
   {
     if ((!hero.hasAccessToSkill(skill)) || (!hero.canUseSkill(skill))) {
       return false;
     }
     List<String> strongParents = getStrongParentSkills(hero, skill);
     boolean hasStrongParents = (strongParents != null) && (!strongParents.isEmpty());
     List<String> weakParents = getWeakParentSkills(hero, skill);
     boolean hasWeakParents = (weakParents != null) && (!weakParents.isEmpty());
     if ((!hasStrongParents) && (!hasWeakParents)) {
       return true;
     }
     if (hasStrongParents) {
       for (String name : getStrongParentSkills(hero, skill)) {
         if (!isMastered(hero, heroes.getSkillManager().getSkill(name))) {
           return false;
         }
       }
     }
     if (hasWeakParents)
     {
       for (String name : getWeakParentSkills(hero, skill)) {
         if (isMastered(hero, heroes.getSkillManager().getSkill(name))) {
           return true;
         }
       }
       return false;
     }
     return true;
   }


   public void loadPlayerConfig(String name)
   {
     FileConfiguration playerConfig = new YamlConfiguration();
     File playerFolder = new File(getDataFolder(), "data");
     if (!playerFolder.exists()) {
       playerFolder.mkdir();
     }
     File playerConfigFile = new File(playerFolder, name + ".yml");
     if (!playerConfigFile.exists()) {
       try
       {
         playerConfigFile.createNewFile();
       }
       catch (IOException ex)
       {
    	 logger.severe(Lang.SERVRE_FAILED_CREATE.toString().replace("%name", name));;
         return;
       }
     }
     try
     {
       playerConfig.load(playerConfigFile);
       if (!this.playerClasses.containsKey(name)) {
         this.playerClasses.put(name, new HashMap());
       }
       for (String s : playerConfig.getKeys(false))
       {
         ((HashMap)this.playerClasses.get(name)).put(s, Integer.valueOf(playerConfig.getInt(s + ".points", 0)));
         if (!this.playerSkills.containsKey(s)) {
           this.playerSkills.put(name, new HashMap());
         }
         if (!((HashMap)this.playerSkills.get(name)).containsKey(s)) {
           ((HashMap)this.playerSkills.get(name)).put(s, new HashMap());
         }
         if (playerConfig.getConfigurationSection(s + ".skills") != null) {
           for (String st : playerConfig.getConfigurationSection(s + ".skills").getKeys(false)) {
             ((HashMap)((HashMap)this.playerSkills.get(name)).get(s)).put(st, Integer.valueOf(playerConfig.getInt(s + ".skills." + st, 0)));
           }
         }
       }
     }
     catch (Exception e)
     {
       logger.severe("[HeroesSkillTree] failed to load " + name + ".yml");
     }
   }
   
   public FileConfiguration getHeroesClassConfig(HeroClass hClass) {
      if(this.hConfigs.containsKey(hClass.getName())) {
         return (FileConfiguration)this.hConfigs.get(hClass.getName());
      } else {
         File classFolder = new File(heroes.getDataFolder(), "classes");
         File[] var6;
         int var5 = (var6 = classFolder.listFiles()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            File f = var6[var4];
            YamlConfiguration config = new YamlConfiguration();

            try {
               config.load(f);
               String currentClassName = config.getString("name");
               if(currentClassName.equalsIgnoreCase(hClass.getName())) {
                  this.hConfigs.put(hClass.getName(), config);
                  return config;
               }

               if(!this.hConfigs.containsKey(currentClassName)) {
                  this.hConfigs.put(currentClassName, config);
               }
            } catch (Exception var9) {
               ;
            }
         }

         return null;
      }
   }

   private void saveAll()
   {
     for (String s : this.playerClasses.keySet()) {
       savePlayerConfig(s);
     }
   }

   public void savePlayerConfig(String s) {
      YamlConfiguration playerConfig = new YamlConfiguration();
      File playerDataFolder = new File(this.getDataFolder(), "data");
      if(!playerDataFolder.exists()) {
         playerDataFolder.mkdir();
      }

      File playerFile = new File(this.getDataFolder() + "/data", s + ".yml");
      String message;
      if(!playerFile.exists()) {
         try {
            playerFile.createNewFile();
         } catch (IOException var9) {
            message = "[HeroesSkillTree] failed to save " + s + ".yml";
            logger.severe(message);
            return;
         }
      }

      try {
         playerConfig.load(playerFile);
         Iterator message1 = ((HashMap)this.playerClasses.get(s)).keySet().iterator();

         while(message1.hasNext()) {
            String e = (String)message1.next();
            playerConfig.set(e + ".points", ((HashMap)this.playerClasses.get(s)).get(e));
            if(this.playerSkills.containsKey(s) && ((HashMap)this.playerSkills.get(s)).containsKey(e)) {
               Iterator var8 = ((HashMap)((HashMap)this.playerSkills.get(s)).get(e)).keySet().iterator();

               while(var8.hasNext()) {
                  String skillName = (String)var8.next();
                  playerConfig.set(e + ".skills." + skillName, ((HashMap)((HashMap)this.playerSkills.get(s)).get(e)).get(skillName));
               }
            }
         }

         playerConfig.save(playerFile);
      } catch (Exception var10) {
         message = "[HeroesSkillTree] failed to save " + s + ".yml";
         logger.severe(message);
      }

   }

   private void loadConfig() {
      File configFile = new File(this.getDataFolder(), "config.yml");
      if(!configFile.exists()) {
         try {
            configFile.createNewFile();
         } catch (IOException var5) {
            logger.severe("[HeroesSkillTree] failed to create new config.yml");
            return;
         }
      }

      YamlConfiguration config = new YamlConfiguration();

      try {
         config.load(configFile);
         this.pointsPerLevel = config.getInt("points-per-level", 1);
      } catch (Exception var4) {
         logger.severe("[HeroesSkillTree] failed to load config.yml");
      }

   }
   
   public YamlConfiguration getLang() {
	   return LANG;
   }
   
   public File getLangFile() {
	   return LANG_FILE;
   }

   public int getPointsPerLevel() {
      return this.pointsPerLevel;
   }

	@SuppressWarnings("static-access")
	public void loadLang() {
		File lang = new File(getDataFolder(), "lang.yml");
		if (!lang.exists()) {
			try {
				getDataFolder().mkdir();
				lang.createNewFile();
				InputStream defConfigStream = this.getResource("lang.yml");
				if (defConfigStream != null) {
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
					defConfig.save(lang);
					Lang.setFile(defConfig);
					return;
				}
			} catch(IOException e) {
				e.printStackTrace(); // So they notice
				LOG.severe("[PluginName] Couldn't create language file.");
				LOG.severe("[PluginName] This is a fatal error. Now disabling");
				this.setEnabled(false); // Without it loaded, we can't send them messages
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		for(Lang item:Lang.values()) {
			if (conf.getString(item.getPath()) == null) {
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		this.LANG = conf;
		this.LANG_FILE = lang;
		try {
			conf.save(getLangFile());
		} catch(IOException e) {
			LOG.log(Level.WARNING, "PluginName: Failed to save lang.yml.");
			LOG.log(Level.WARNING, "PluginName: Report this stack trace to <your name>.");
			e.printStackTrace();
		}
	}
}
