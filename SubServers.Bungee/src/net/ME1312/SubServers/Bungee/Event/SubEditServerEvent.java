package net.ME1312.SubServers.Bungee.Event;

import net.ME1312.SubServers.Bungee.Host.Server;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Map.ObjectMapValue;
import net.ME1312.Galaxi.Library.Container.NamedContainer;
import net.ME1312.SubServers.Bungee.Library.SubEvent;
import net.ME1312.Galaxi.Library.Util;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

import java.util.UUID;

/**
 * Server Edit Event
 */
public class SubEditServerEvent extends Event implements SubEvent, Cancellable {
    private boolean cancelled = false;
    private UUID player;
    private Server server;
    private NamedContainer<String, ObjectMapValue> edit;
    private boolean perm;

    /**
     * Server Edit Event
     *
     * @param player Player Adding Server
     * @param server Server to be Edited
     * @param edit Edit to make
     * @param permanent If the change is permanent
     */
    public SubEditServerEvent(UUID player, Server server, NamedContainer<String, ?> edit, boolean permanent) {
        if (Util.isNull(server, edit)) throw new NullPointerException();
        ObjectMap<String> section = new ObjectMap<String>();
        section.set(".", edit.get());
        this.player = player;
        this.server = server;
        this.edit = new NamedContainer<String, ObjectMapValue>(edit.name(), section.get("."));
        this.perm = permanent;
    }

    /**
     * Gets the Server to be Edited
     *
     * @return The Server to be Edited
     */
    public Server getServer() { return server; }

    /**
     * Gets the player that triggered the Event
     *
     * @return The Player that triggered this Event or null if Console
     */
    public UUID getPlayer() { return player; }

    /**
     * Gets the edit to be made
     *
     * @return Edit to be made
     */
    public NamedContainer<String, ObjectMapValue> getEdit() {
        return edit;
    }

    /**
     * Gets if the edit is permanent
     *
     * @return Permanent Status
     */
    public boolean isPermanent() {
        return perm;
    }

    /**
     * Gets the Cancelled Status
     *
     * @return Cancelled Status
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the Cancelled Status
     */
    public void setCancelled(boolean value) {
        cancelled = value;
    }
}
