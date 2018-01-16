import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VoteStore extends JavaPlugin implements Listener {
	public static HashMap<String, Integer> votecount = new HashMap<String, Integer>();
	public ArrayList<VoteCommand> commands = new ArrayList<VoteCommand>();

	@Override
	public void onEnable() {
		Iterator<String> l = getConfig().getStringList("commands").iterator();
		while (l.hasNext()){
			commands.add(new VoteCommand(l.next(),l.next(),Integer.parseInt(l.next())));
		}
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		getServer().getPluginManager().registerEvents(this, this);
		File fi = new File(this.getDataFolder() + "/data.txt");
		if (!fi.exists()) {
			try {
				fi.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Scanner s = null;
		try {
			s = new Scanner(fi);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (s.hasNext()) {
			String uuid = s.next();
			getLogger().info(uuid);
			int votes = s.nextInt();
			getLogger().info(votes+"");
			votecount.put(uuid, votes);
		}
		s.close();
		getLogger().info("VoteStore enabled!");
	}

	@Override
	public void onDisable() {
		String[] s =new String[commands.size()*3];
		for (int i = 0; i < commands.size(); i++){
			s[i*3+0]=commands.get(i).description;
			s[i*3+1]=commands.get(i).command;
			s[i*3+2]=commands.get(i).cost+"";
		}
		getConfig().set("commands", Arrays.asList(s));
		File f = new File(this.getDataFolder() + "/data.txt");
		FileWriter fi = null;
		try {
			fi = new FileWriter(f);
			for (String user : votecount.keySet()) {
				fi.write(user + " " + votecount.get(user) + '\n');
			}
			fi.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getLogger().info("VoteStore disabled.");

	}

	public static boolean isInteger(String s) {
		return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {
		if (s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0)
				return false;
		}
		return true;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onVotifierEvent(VotifierEvent e) {
		Vote vote = e.getVote();
		Player p = Bukkit.getPlayerExact(vote.getUsername());
		String pId = p.getUniqueId().toString();
		int votes = 1;
		if (votecount.containsKey(pId)) {
			votes += votecount.get(pId);
		}
		votecount.put(p.getUniqueId().toString(), votes);
		p.sendMessage("§AYou have just voted!");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = (Player) sender;
		String pId = p.getUniqueId().toString();
		if (!votecount.containsKey(pId)) {
			votecount.put(pId, 0);
		}
		int allowance = votecount.get(pId);
		if (cmd.getName().equalsIgnoreCase("votestore") || cmd.getName().equalsIgnoreCase("vs")) {
			if (args.length==0){
				p.sendMessage("§6- TerraPrime Vote -");
				p.sendMessage("§6/votestore list - §7 Lists all rewards with their numbers.");
				p.sendMessage("§6/votestore buy [number] - §7 Buy a specific reward.");
				p.sendMessage("§6/votestore amount - §7 Shows how many votes you have.");
				p.sendMessage("§6/vs - §7 Shortcut for /votestore.");
				return true;
			}
			if (args[0].equals("list")) {
				for (int i = 0; i < commands.size(); i++) {
					sender.sendMessage("6#" + i + " : " + commands.get(i).description);
				}
				return true;
			} else if (args[0].equals("buy")) {
				if (args.length>1){
				if (isInteger(args[1])) {
					int i = Integer.parseInt(args[1]);
					if (i < commands.size()) {
						VoteCommand command = commands.get(i);
						if (command.cost > allowance) {
							p.sendMessage("§CYou do not have enough to get this reward!");
							return true;
						} else {
							String straya = command.command.replaceAll("@p", p.getPlayerListName());
							getServer().dispatchCommand(getServer().getConsoleSender(), straya);
							allowance -= command.cost;
							votecount.put(pId, allowance);
							p.sendMessage("§6Thank you for supporting our server!");
							return true;
						}
					} else {
						p.sendMessage("§CThere are only " + commands.size() + " rewards.\n Please choose another reward.");
						return true;
					}

				} else {
					p.sendMessage("§CYou need to vote more to unlock this.");
					return true;
				}
				}else{
					p.sendMessage("§CYou need to specify which reward you want.");
					p.sendMessage("§7/votestore buy [reward id]");
					return true;
				}
			} else if (args[0].equals("amount")) {
				int i = votecount.get(pId);
				if (i<0){
					p.sendMessage("§6You owe us "+(-i)+" votes.");
				}
				if (i==0){
					p.sendMessage("§6You have no votes.");
				}
				if (i==1){
				p.sendMessage("§6You have " + i+ " vote.");
				}
				if (i>1){
					p.sendMessage("§6You have " + i+ " votes.");
				}
				return true;
			} else if (args[0].equals("reload")){
			}else{
				p.sendMessage("§6/votestore list - §7 Lists all rewards with their numbers.");
				p.sendMessage("§6/votestore buy [number] - §7 Buy a specific reward.");
				p.sendMessage("§6/votestore amount - §7 Shows how many votes you have.");
				p.sendMessage("§6/vs - §7 Shortcut for /votestore.");
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("vsmanage")) {

		}
		return false;
	}
}
