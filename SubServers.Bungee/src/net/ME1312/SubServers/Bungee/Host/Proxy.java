package net.ME1312.SubServers.Bungee.Host;

import net.ME1312.SubData.Server.DataClient;
import net.ME1312.SubData.Server.SubDataClient;
import net.ME1312.SubServers.Bungee.Event.SubRemoveProxyEvent;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Map.ObjectMapValue;
import net.ME1312.Galaxi.Library.ExtraDataHandler;
import net.ME1312.Galaxi.Library.Container.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.SubData.Server.ClientHandler;
import net.ME1312.SubServers.Bungee.Network.Packet.PacketExSyncPlayer;
import net.ME1312.SubServers.Bungee.SubAPI;
import net.ME1312.SubServers.Bungee.SubProxy;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

/**
 * Proxy Class
 */
public class Proxy implements ClientHandler, ExtraDataHandler {
    private HashMap<Integer, SubDataClient> subdata = new HashMap<Integer, SubDataClient>();
    private ObjectMap<String> extra = new ObjectMap<String>();
    private final String signature;
    private boolean persistent = true;
    private String nick = null;
    private final String name;

    @SuppressWarnings("deprecation")
    public Proxy(String name) throws IllegalArgumentException {
        if (name == null) {
            name = Util.getNew(SubAPI.getInstance().getInternals().proxies.keySet(), () -> UUID.randomUUID().toString());
            persistent = false;
        }
        if (name.contains(" ")) throw new IllegalArgumentException("Proxy names cannot have spaces: " + name);
        this.name = name;
        this.signature = SubAPI.getInstance().signAnonymousObject();

        subdata.put(0, null);
    }

    @Override
    public DataClient[] getSubData() {
        LinkedList<Integer> keys = new LinkedList<Integer>(subdata.keySet());
        LinkedList<SubDataClient> channels = new LinkedList<SubDataClient>();
        Collections.sort(keys);
        for (Integer channel : keys) channels.add(subdata.get(channel));
        return channels.toArray(new DataClient[0]);
    }

    @SuppressWarnings("deprecation")
    public void setSubData(SubDataClient client, int channel) {
        boolean update = false;
        if (channel < 0) throw new IllegalArgumentException("Subchannel ID cannot be less than zero");
        if (client != null || channel == 0) {
            if (!subdata.keySet().contains(channel) || (channel == 0 && (client == null || subdata.get(channel) == null))) {
                update = true;
                subdata.put(channel, (SubDataClient) client);
                if (client != null && (client.getHandler() == null || !equals(client.getHandler()))) ((SubDataClient) client).setHandler(this);
            }
        } else {
            update = true;
            subdata.remove(channel);
        }

        if (update) {
            DataClient[] subdata = getSubData();
            if (subdata[0] == null && subdata.length <= 1) {
                SubProxy plugin = SubAPI.getInstance().getInternals();
                for (UUID id : Util.getBackwards(plugin.rPlayerLinkP, this)) {
                    plugin.rPlayerLinkS.remove(id);
                    plugin.rPlayerLinkP.remove(id);
                    plugin.rPlayers.remove(id);
                }
                for (Proxy proxy : SubAPI.getInstance().getProxies().values()) if (proxy.getSubData()[0] != null && proxy != this) {
                    ((SubDataClient) proxy.getSubData()[0]).sendPacket(new PacketExSyncPlayer(getName(), null, (RemotePlayer[]) null));
                }
                if (!persistent) {
                    ProxyServer.getInstance().getPluginManager().callEvent(new SubRemoveProxyEvent(this));
                    SubAPI.getInstance().getInternals().proxies.remove(getName().toLowerCase());
                }
            }
        }
    }

    @Override
    public void removeSubData(DataClient client) {
        for (Integer channel : Util.getBackwards(subdata, (SubDataClient) client)) setSubData(null, channel);
    }

    /**
     * Get the Name of this Proxy
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the Display Name of this Proxy
     *
     * @return Display Name
     */
    public String getDisplayName() {
        return (nick == null)?getName():nick;
    }

    /**
     * Sets the Display Name for this Proxy
     *
     * @param value Value (or null to reset)
     */
    public void setDisplayName(String value) {
        if (value == null || value.length() == 0 || getName().equals(value)) {
            this.nick = null;
        } else {
            this.nick = value;
        }
    }

    /**
     * Determine if the proxy is the Master Proxy
     *
     * @return Master Proxy Status
     */
    public boolean isMaster() {
        return SubAPI.getInstance().getMasterProxy() == this;
    }

    /**
     * Get the players on this proxy (via RedisBungee)
     *
     * @return Remote Player Collection
     */
    @SuppressWarnings("deprecation")
    public Collection<RemotePlayer> getPlayers() {
        SubProxy plugin = SubAPI.getInstance().getInternals();
        ArrayList<RemotePlayer> players = new ArrayList<RemotePlayer>();
        for (UUID id : Util.getBackwards(plugin.rPlayerLinkP, this)) {
            players.add(plugin.rPlayers.get(id));
        }
        return players;
    }

    /**
     * Get the Signature of this Object
     *
     * @return Object Signature
     */
    public final String getSignature() {
        return signature;
    }

    @Override
    public void addExtra(String handle, Object value) {
        if (Util.isNull(handle, value)) throw new NullPointerException();
        extra.set(handle, value);
    }

    @Override
    public boolean hasExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return extra.getKeys().contains(handle);
    }

    @Override
    public ObjectMapValue getExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return extra.get(handle);
    }

    @Override
    public ObjectMap<String> getExtra() {
        return extra.clone();
    }

    @Override
    public void removeExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        extra.remove(handle);
    }

    @Override
    public ObjectMap<String> forSubData() {
        ObjectMap<String> info = new ObjectMap<String>();
        info.set("type", "Proxy");
        info.set("name", getName());
        info.set("display", getDisplayName());
        ObjectMap<String> players = new ObjectMap<String>();
        for (RemotePlayer player : getPlayers())
            players.set(player.getUniqueId().toString(), player.getName());
        info.set("players", players);
        info.set("master", isMaster());
        ObjectMap<Integer> subdata = new ObjectMap<Integer>();
        for (int channel : this.subdata.keySet()) subdata.set(channel, (this.subdata.get(channel) == null)?null:this.subdata.get(channel).getID());
        info.set("subdata", subdata);
        info.set("signature", signature);
        info.set("extra", getExtra());
        return info;
    }
}
