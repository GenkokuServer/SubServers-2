package net.ME1312.SubServers.Client.Sponge.Network;

import net.ME1312.Galaxi.Library.Callback.Callback;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.SubData.Client.Library.DisconnectReason;
import net.ME1312.SubData.Client.SubDataClient;
import net.ME1312.SubData.Client.SubDataProtocol;
import net.ME1312.SubServers.Client.Sponge.Event.SubNetworkConnectEvent;
import net.ME1312.SubServers.Client.Sponge.Event.SubNetworkDisconnectEvent;
import net.ME1312.SubServers.Client.Sponge.Network.Packet.*;
import net.ME1312.SubServers.Client.Sponge.SubAPI;
import net.ME1312.SubServers.Client.Sponge.SubPlugin;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

public class SubProtocol extends SubDataProtocol {
    private static SubProtocol instance;
    private SubProtocol() {}

    @SuppressWarnings("deprecation")
    public static SubProtocol get() {
        if (instance == null) {
            instance = new SubProtocol();

            SubPlugin plugin = SubAPI.getInstance().getInternals();

            instance.setName("SubServers 2");
            instance.addVersion(new Version("2.14a+"));


            // 00-09: Object Link Packets
            instance.registerPacket(0x0002, PacketLinkServer.class);
            instance.registerPacket(0x0002, new PacketLinkServer(plugin));


            // 10-29: Download Packets
            instance.registerPacket(0x0010, PacketDownloadLang.class);
            instance.registerPacket(0x0011, PacketDownloadPlatformInfo.class);
            instance.registerPacket(0x0012, PacketDownloadProxyInfo.class);
            instance.registerPacket(0x0013, PacketDownloadHostInfo.class);
            instance.registerPacket(0x0014, PacketDownloadGroupInfo.class);
            instance.registerPacket(0x0015, PacketDownloadServerInfo.class);
            instance.registerPacket(0x0016, PacketDownloadPlayerList.class);
            instance.registerPacket(0x0017, PacketCheckPermission.class);

            instance.registerPacket(0x0010, new PacketDownloadLang(plugin));
            instance.registerPacket(0x0011, new PacketDownloadPlatformInfo());
            instance.registerPacket(0x0012, new PacketDownloadProxyInfo());
            instance.registerPacket(0x0013, new PacketDownloadHostInfo());
            instance.registerPacket(0x0014, new PacketDownloadGroupInfo());
            instance.registerPacket(0x0015, new PacketDownloadServerInfo());
            instance.registerPacket(0x0016, new PacketDownloadPlayerList());
            instance.registerPacket(0x0017, new PacketCheckPermission());


            // 30-49: Control Packets
            instance.registerPacket(0x0030, PacketCreateServer.class);
            instance.registerPacket(0x0031, PacketAddServer.class);
            instance.registerPacket(0x0032, PacketStartServer.class);
            instance.registerPacket(0x0033, PacketEditServer.class);
            instance.registerPacket(0x0034, PacketRestartServer.class);
            instance.registerPacket(0x0035, PacketCommandServer.class);
            instance.registerPacket(0x0036, PacketStopServer.class);
            instance.registerPacket(0x0037, PacketRemoveServer.class);
            instance.registerPacket(0x0038, PacketDeleteServer.class);

            instance.registerPacket(0x0030, new PacketCreateServer());
            instance.registerPacket(0x0031, new PacketAddServer());
            instance.registerPacket(0x0032, new PacketStartServer());
            instance.registerPacket(0x0033, new PacketEditServer());
            instance.registerPacket(0x0034, new PacketRestartServer());
            instance.registerPacket(0x0035, new PacketCommandServer());
            instance.registerPacket(0x0036, new PacketStopServer());
            instance.registerPacket(0x0037, new PacketRemoveServer());
            instance.registerPacket(0x0038, new PacketDeleteServer());


            // 70-79: External Misc Packets
          //instance.registerPacket(0x0070, PacketInExRunEvent.class);
          //instance.registerPacket(0x0071, PacketInExReset.class);
          //instance.registerPacket(0x0072, PacketInExReload.class);
            instance.registerPacket(0x0074, PacketExCheckPermission.class);

            instance.registerPacket(0x0070, new PacketInExRunEvent(plugin));
            instance.registerPacket(0x0071, new PacketInExReset());
            instance.registerPacket(0x0072, new PacketInExReload(plugin));
            instance.registerPacket(0x0074, new PacketExCheckPermission());
        }

        return instance;
    }

    private Logger getLogger(int channel) {
        Logger log = Logger.getAnonymousLogger();
        log.setUseParentHandlers(false);
        log.addHandler(new Handler() {
            private org.slf4j.Logger log = LoggerFactory.getLogger("SubData" + ((channel != 0)? "/Sub-"+channel:""));
            private boolean open = true;

            @Override
            public void publish(LogRecord record) {
                if (open) {
                    if (record.getLevel().intValue() == OFF.intValue()) {
                        // do nothing
                    } else if (record.getLevel().intValue() == FINE.intValue() || record.getLevel().intValue() == FINER.intValue() || record.getLevel().intValue() == FINEST.intValue()) {
                        log.debug(record.getMessage());
                    } else if (record.getLevel().intValue() == ALL.intValue() || record.getLevel().intValue() == CONFIG.intValue() || record.getLevel().intValue() == INFO.intValue()) {
                        log.info(record.getMessage());
                    } else if (record.getLevel().intValue() == WARNING.intValue()) {
                        log.warn(record.getMessage());
                    } else if (record.getLevel().intValue() == SEVERE.intValue()) {
                        log.error(record.getMessage());
                    }
                }
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {
                open = false;
            }
        });

        return log;
    }

    @Override
    protected SubDataClient sub(Callback<Runnable> scheduler, Logger logger, InetAddress address, int port) throws IOException {
        SubPlugin plugin = SubAPI.getInstance().getInternals();
        HashMap<Integer, SubDataClient> map = Util.getDespiteException(() -> Util.reflect(SubPlugin.class.getDeclaredField("subdata"), plugin), null);

        int channel = 1;
        while (map.keySet().contains(channel)) channel++;
        final int fc = channel;

        SubDataClient subdata = super.open(scheduler, getLogger(fc), address, port);
        map.put(fc, subdata);
        subdata.sendPacket(new PacketLinkServer(plugin, fc));
        subdata.on.closed(client -> map.remove(fc));

        return subdata;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SubDataClient open(Callback<Runnable> scheduler, Logger logger, InetAddress address, int port) throws IOException {
        SubPlugin plugin = SubAPI.getInstance().getInternals();
        HashMap<Integer, SubDataClient> map = Util.getDespiteException(() -> Util.reflect(SubPlugin.class.getDeclaredField("subdata"), plugin), null);

        SubDataClient subdata = super.open(scheduler, logger, address, port);
        subdata.sendPacket(new PacketLinkServer(plugin, 0));
        subdata.sendPacket(new PacketDownloadLang());
        subdata.on.ready(client -> Sponge.getEventManager().post(new SubNetworkConnectEvent((SubDataClient) client)));
        subdata.on.closed(client -> {
            SubNetworkDisconnectEvent event = new SubNetworkDisconnectEvent(client.get(), client.name());
            Sponge.getEventManager().post(event);
            map.put(0, null);

            Logger log = Util.getDespiteException(() -> Util.reflect(SubDataClient.class.getDeclaredField("log"), client.get()), null);
            int reconnect = plugin.config.get().getMap("Settings").getMap("SubData").getInt("Reconnect", 30);
            if (Util.getDespiteException(() -> Util.reflect(SubPlugin.class.getDeclaredField("reconnect"), plugin), false) && reconnect > 0
                    && client.name() != DisconnectReason.PROTOCOL_MISMATCH && client.name() != DisconnectReason.ENCRYPTION_MISMATCH) {
                log.info("Attempting reconnect in " + reconnect + " seconds");
                Sponge.getScheduler().createTaskBuilder().async().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Util.reflect(SubPlugin.class.getDeclaredMethod("connect"), plugin);
                        } catch (InvocationTargetException e) {
                            if (e.getTargetException() instanceof IOException) {
                                log.info("Connection was unsuccessful, retrying in " + reconnect + " seconds");

                                Sponge.getScheduler().createTaskBuilder().async().execute(this).delay(reconnect, TimeUnit.SECONDS).submit(plugin);
                            } else e.printStackTrace();
                        } catch (NoSuchMethodException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }).delay(reconnect, TimeUnit.SECONDS).submit(plugin);
            }
        });

        return subdata;
    }

    @SuppressWarnings("deprecation")
    @Override
    public SubDataClient open(Logger logger, InetAddress address, int port) throws IOException {
        SubPlugin plugin = SubAPI.getInstance().getInternals();
        return open(event -> Sponge.getScheduler().createTaskBuilder().async().execute(event).submit(plugin), logger, address, port);
    }

    public SubDataClient open(InetAddress address, int port) throws IOException {
        return open(getLogger(0), address, port);
    }
}
