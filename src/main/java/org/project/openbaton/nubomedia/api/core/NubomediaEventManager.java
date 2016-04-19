package org.project.openbaton.nubomedia.api.core;

import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.project.openbaton.nubomedia.api.configuration.PaaSProperties;
import org.project.openbaton.nubomedia.api.messages.BuildingStatus;
import org.project.openbaton.nubomedia.api.openbaton.OpenbatonCreateServer;
import org.project.openbaton.nubomedia.api.openbaton.OpenbatonEvent;
import org.project.openbaton.nubomedia.api.openshift.exceptions.DuplicatedException;
import org.project.openbaton.nubomedia.api.openshift.exceptions.UnauthorizedException;
import org.project.openbaton.nubomedia.api.persistence.Application;
import org.project.openbaton.nubomedia.api.persistence.ApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;

/**
 * Created by Carlo on 19/04/2016.
 */
@RestController
@RequestMapping ("/api/v1/nubomedia/event")
public class NubomediaEventManager {

    @Autowired private OpenshiftManager osmanager;
    @Autowired private OpenbatonManager obmanager;
    @Autowired private Map<String, OpenbatonCreateServer> deploymentMap;
    @Autowired private ApplicationRepository appRepo;
    @Autowired private PaaSProperties paaSProperties;

    private Logger logger;

    @PostConstruct
    private void init(){

        this.logger = LoggerFactory.getLogger(this.getClass());

    }

    @RequestMapping(value = "/openbaton/{id}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void startOpenshiftBuild(@RequestBody OpenbatonEvent evt, @PathVariable("id") String id) throws UnauthorizedException {
        logger.debug("starting callback for appId" + id);
        logger.info("Received event " + evt);
        Application app = appRepo.findFirstByAppID(id);
        logger.debug(deploymentMap.toString());
        OpenbatonCreateServer server = deploymentMap.get(id);

        if(evt.getAction().equals(Action.INSTANTIATE_FINISH) && server.getMediaServerID().equals(evt.getPayload().getId())){
            logger.info("[PAAS]: EVENT_FINISH " + new Date().getTime());
            app.setStatus(BuildingStatus.INITIALISED);
            app.setResourceOK(true);
            appRepo.save(app);

            String vnfrID ="";
            String cloudRepositoryIp = null;
            String cloudRepositoryPort = null;

            for(VirtualNetworkFunctionRecord record : evt.getPayload().getVnfr()){

                if(record.getEndpoint().equals("media-server"))
                    vnfrID = record.getId();

                if(record.getName().contains("mongodb")){
                    cloudRepositoryIp = this.getCloudRepoIP(record);
                    cloudRepositoryPort = "7676";
                }

            }

            String route = null;
            try {
                int[] ports = new int[app.getPorts().size()];
                int[] targetPorts = new int[app.getTargetPorts().size()];

                for(int i = 0; i < ports.length; i++){
                    ports[i] = app.getPorts().get(i);
                    targetPorts[i] = app.getTargetPorts().get(i);
                }

                logger.info("[PAAS]: CREATE_APP_OS " + new Date().getTime());
                logger.debug("cloudRepositoryPort "+ cloudRepositoryPort + " IP " + cloudRepositoryIp);

                try {
                    route = osmanager.buildApplication(app.getAppID(), app.getAppName(), app.getProjectName(), app.getGitURL(), ports, targetPorts, app.getProtocols().toArray(new String[0]), app.getReplicasNumber(), app.getSecretName(), vnfrID, paaSProperties.getVnfmIP(), paaSProperties.getVnfmPort(), cloudRepositoryIp, cloudRepositoryPort);

                } catch (ResourceAccessException e){
                    obmanager.deleteDescriptor(server.getNsdID());
                    obmanager.deleteEvent(server.getEventAllocatedID());
                    obmanager.deleteEvent(server.getEventErrorID());
                    app.setStatus(BuildingStatus.FAILED);
                    appRepo.save(app);
                    deploymentMap.remove(app.getAppID());
                }
                logger.info("[PAAS]: SCHEDULED_APP_OS " + new Date().getTime());
            } catch (DuplicatedException e) {
                app.setRoute(e.getMessage());
                app.setStatus(BuildingStatus.DUPLICATED);
                appRepo.save(app);
                return;
            }
            obmanager.deleteDescriptor(server.getNsdID());

            obmanager.deleteEvent(server.getEventAllocatedID());
            obmanager.deleteEvent(server.getEventErrorID());
            app.setRoute(route);
            appRepo.save(app);
            deploymentMap.remove(app.getAppID());
        }
        else if (evt.getAction().equals(Action.ERROR)){

            obmanager.deleteDescriptor(server.getNsdID());
            obmanager.deleteEvent(server.getEventErrorID());
            obmanager.deleteEvent(server.getEventAllocatedID());
            obmanager.deleteRecord(server.getMediaServerID());
            app.setStatus(BuildingStatus.FAILED);
            appRepo.save(app);
            deploymentMap.remove(app.getAppID());
        }

    }

    private String getCloudRepoIP(VirtualNetworkFunctionRecord record) {

        for (VirtualDeploymentUnit vdu : record.getVdu()){
            for (VNFCInstance instance : vdu.getVnfc_instance()){
                for (Ip ip : instance.getFloatingIps()){
                    if (ip != null){
                        return ip.getIp();
                    }
                }
            }
        }

        return null;
    }
}
