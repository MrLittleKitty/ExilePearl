package com.devotedmc.testbukkit.core;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.inventory.PlayerInventory;

import com.devotedmc.testbukkit.TestBukkit;
import com.devotedmc.testbukkit.TestInventory;
import com.devotedmc.testbukkit.TestPlayer;
import com.devotedmc.testbukkit.TestServer;
import com.devotedmc.testbukkit.annotation.ProxyTarget;
import com.devotedmc.testbukkit.annotation.ProxyStub;

@ProxyTarget(TestPlayer.class)
class CoreTestPlayer extends ProxyMockBase<TestPlayer> {

	private final String name;
	private final UUID uid;
	private final PlayerInventory inventory;
	private final Queue<String> messages;
	private final TestPlayer player;
	private Location location;
	
	public CoreTestPlayer(String name, UUID uid) throws Exception {
		super(TestPlayer.class);
		this.name = name;
		this.uid = uid;
		this.player = getProxy();
		location = getServer().getWorld("world").getSpawnLocation();
		messages = new LinkedBlockingQueue<String>();
		inventory = (PlayerInventory)createInstance(TestInventory.class, player, InventoryType.PLAYER, PlayerInventory.class);
	}

	@ProxyStub
	public String getName() {
		return name;
	}

	@ProxyStub
	public UUID getUniqueId() {
		return uid;
	}

	@ProxyStub
	public boolean isOnline() {
		return TestBukkit.getServer().getOnlinePlayers().contains(this);
	}
	
	public String toString() {
		return String.format("TestPlayer{ name: %s, uid: %s }", name, uid.toString());
	}

	@ProxyStub
	public Location getLocation() {
		return location;
	}

	@ProxyStub
	public TestServer getServer() {
		return TestBukkit.getServer();
	}

	@ProxyStub
	public boolean connect() {
    	final InetSocketAddress address = new InetSocketAddress("localhost", 25565);
    	getServer().log("UUID of player %s is %s", player.getName(), uid.toString());
    	final AsyncPlayerPreLoginEvent preLoginEvent = new AsyncPlayerPreLoginEvent(name, address.getAddress(), uid);
    	TestBukkit.getPluginManager().callEvent(preLoginEvent);
    	if (preLoginEvent.getLoginResult() != Result.ALLOWED) {
    		return false;
    	}
    	
    	final PlayerLoginEvent loginEvent = new PlayerLoginEvent(player, "localhost", address.getAddress());
    	TestBukkit.getPluginManager().callEvent(loginEvent);

    	if (loginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
    		return false;
    	}
    	
    	final PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "");
    	TestBukkit.getPluginManager().callEvent(joinEvent);
		
    	Location l = player.getLocation();
    	getServer().log("%s logged in at ([%s]%f, %f, %f)", player.getName(), l.getWorld().getName(), l.getX(), l.getY(),l.getZ());
    	getServer().addPlayer(player);
		return true;
	}

	@ProxyStub
	public void disconnect() {
		TestBukkit.getServer().getOnlinePlayers().remove(this);
		
		final PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, "");
    	TestBukkit.getPluginManager().callEvent(quitEvent);
    	getServer().log("%s left the game", player.getName());
	}

	@ProxyStub
    public boolean runCommand(String commandLine) {
    	getServer().getLogger().log(Level.INFO, String.format("Running player command '%s'", commandLine));
    	return getServer().dispatchCommand(player, commandLine);
    }

	@ProxyStub
    public void sendMessage(String message) {
    	if (message != null) {
        	messages.add(message);
    	}
    }

	@ProxyStub
    public void sendMessage(String[] messages) {
    	for(String s : messages) {
    		sendMessage(s);
    	}
    }

	@ProxyStub
    public Queue<String> getMessages() {
    	return messages;
    }

	@ProxyStub
	public String pollMessage() {
		return messages.poll();
	}
	
	@ProxyStub
	public void clearMessages() {
		messages.clear();
	}

	@ProxyStub
    public PlayerInventory getInventory() {
    	return inventory;
    }
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}

		o = getEqualsProxy(o);

		if (getClass() != o.getClass()) {
			return false;
		}

		CoreTestPlayer other = (CoreTestPlayer) o;

		return new EqualsBuilder()
				.append(name, other.name)
				.append(uid, other.uid)
				.append(inventory, other.inventory)
				.append(messages, other.messages)
				.append(location, other.location)
				.isEquals();
	}
}
