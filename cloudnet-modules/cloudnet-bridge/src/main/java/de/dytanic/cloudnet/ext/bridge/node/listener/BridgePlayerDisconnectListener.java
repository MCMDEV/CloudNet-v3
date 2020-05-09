package de.dytanic.cloudnet.ext.bridge.node.listener;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceInfoUpdateEvent;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceStopEvent;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import de.dytanic.cloudnet.ext.bridge.node.player.NodePlayerManager;
import de.dytanic.cloudnet.ext.bridge.player.CloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.ServicePlayer;

public class BridgePlayerDisconnectListener {

    private NodePlayerManager nodePlayerManager;

    public BridgePlayerDisconnectListener(NodePlayerManager nodePlayerManager) {
        this.nodePlayerManager = nodePlayerManager;
    }

    @EventListener
    public void handleServiceUpdate(CloudServiceInfoUpdateEvent event) {
        if (!event.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy()) {
            return;
        }

        event.getServiceInfo().getProperty(BridgeServiceProperty.PLAYERS).ifPresent(players -> {
            for (ServiceInfoSnapshot service : CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices()) {
                if (!service.getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy()) {
                    continue;
                }

                service.getProperty(BridgeServiceProperty.PLAYERS).ifPresent(players::addAll);
            }

            for (CloudPlayer onlinePlayer : this.nodePlayerManager.getOnlinePlayers()) {
                if (onlinePlayer.getLoginService().getEnvironment().isMinecraftProxy() &&
                        players.stream().noneMatch(servicePlayer -> servicePlayer.getUniqueId().equals(onlinePlayer.getUniqueId()))) {
                    this.nodePlayerManager.logoutPlayer(onlinePlayer);
                }
            }
        });
    }

    @EventListener
    public void handleServiceStop(CloudServiceStopEvent event) {
        event.getServiceInfo().getProperty(BridgeServiceProperty.PLAYERS).ifPresent(players -> {
            for (ServicePlayer player : players) {
                CloudPlayer cloudPlayer = this.nodePlayerManager.getOnlinePlayer(player.getUniqueId());

                if (cloudPlayer != null &&
                        cloudPlayer.getLoginService().getServiceId().getUniqueId()
                                .equals(event.getServiceInfo().getServiceId().getUniqueId())) {
                    this.nodePlayerManager.logoutPlayer(cloudPlayer);
                }

            }
        });
    }

}