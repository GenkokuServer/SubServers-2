package net.ME1312.SubServers.Client.Bukkit.Network.Packet;

import net.ME1312.SubServers.Client.Bukkit.Graphic.UIRenderer;
import net.ME1312.SubServers.Client.Bukkit.Library.Callback;
import net.ME1312.SubServers.Client.Bukkit.Library.Config.YAMLSection;
import net.ME1312.SubServers.Client.Bukkit.Library.Util;
import net.ME1312.SubServers.Client.Bukkit.Library.Version.Version;
import net.ME1312.SubServers.Client.Bukkit.Network.PacketIn;
import net.ME1312.SubServers.Client.Bukkit.Network.PacketOut;

import java.util.HashMap;
import java.util.UUID;

/**
 * Create Server Packet
 */
public class PacketCreateServer implements PacketIn, PacketOut {
    private static HashMap<String, Callback<YAMLSection>[]> callbacks = new HashMap<String, Callback<YAMLSection>[]>();
    private UUID player;
    private String name;
    private String host;
    private String template;
    private Version version;
    private Integer port;
    private String id;

    /**
     * New PacketCreateServer (In)
     */
    public PacketCreateServer() {}

    /**
     * New PacketCreateServer (Out)
     *
     * @param player Player Creating
     * @param name Server Name
     * @param host Host to use
     * @param template Server Template
     * @param version Server Version
     * @param port Server Port
     * @param callback Callbacks
     */
    @SafeVarargs
    public PacketCreateServer(UUID player, String name, String host, String template, Version version, Integer port, Callback<YAMLSection>... callback) {
        if (Util.isNull(name, host, template, version, callback)) throw new NullPointerException();
        this.player = player;
        this.name = name;
        this.host = host;
        this.template = template;
        this.version = version;
        this.port = port;
        this.id = Util.getNew(callbacks.keySet(), UUID::randomUUID).toString();
        callbacks.put(id, callback);
    }

    /**
     * New PacketCreateServer (Out)
     *
     * @param player Player Creating
     * @param options Creator UI Options
     * @param callback Callbacks
     */
    @SafeVarargs
    public PacketCreateServer(UUID player, UIRenderer.CreatorOptions options, Callback<YAMLSection>... callback) {
        if (Util.isNull(options, callback)) throw new NullPointerException();
        this.player = player;
        this.name = options.getName();
        this.host = options.getHost();
        this.template = options.getTemplate();
        this.version = options.getVersion();
        this.port = options.getPort();
        this.id = Util.getNew(callbacks.keySet(), UUID::randomUUID).toString();
        callbacks.put(id, callback);

    }

    @Override
    public YAMLSection generate() {
        YAMLSection data = new YAMLSection();
        data.set("id", id);
        if (player != null) data.set("player", player.toString());
        YAMLSection creator = new YAMLSection();
        creator.set("name", name);
        creator.set("host", host);
        creator.set("template", template);
        creator.set("version", version);
        if (port != null) creator.set("port", port);
        data.set("creator", creator);
        return data;
    }

    @Override
    public void execute(YAMLSection data) {
        for (Callback<YAMLSection> callback : callbacks.get(data.getRawString("id"))) callback.run(data);
        callbacks.remove(data.getRawString("id"));
    }

    @Override
    public Version getVersion() {
        return new Version("2.13b");
    }
}
