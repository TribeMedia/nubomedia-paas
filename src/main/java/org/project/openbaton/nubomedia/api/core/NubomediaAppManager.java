package org.project.openbaton.nubomedia.api.core;

import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.sdk.api.exception.SDKException;
import org.project.openbaton.nubomedia.api.configuration.PaaSProperties;
import org.project.openbaton.nubomedia.api.exceptions.ApplicationNotFoundException;
import org.project.openbaton.nubomedia.api.messages.*;
import org.project.openbaton.nubomedia.api.openbaton.OpenbatonCreateServer;
import org.project.openbaton.nubomedia.api.openbaton.OpenbatonEvent;
import org.project.openbaton.nubomedia.api.openbaton.exceptions.StunServerException;
import org.project.openbaton.nubomedia.api.openbaton.exceptions.turnServerException;
import org.project.openbaton.nubomedia.api.openshift.exceptions.DuplicatedException;
import org.project.openbaton.nubomedia.api.openshift.exceptions.NameStructureException;
import org.project.openbaton.nubomedia.api.openshift.exceptions.UnauthorizedException;
import org.project.openbaton.nubomedia.api.persistence.Application;
import org.project.openbaton.nubomedia.api.persistence.ApplicationRepository;
import org.project.openbaton.nubomedia.api.security.NubomediaUserDetailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;


/**
 * Created by maa on 28.09.15.
 */

@RestController
@RequestMapping("/api/v1/nubomedia/paas")
public class NubomediaAppManager {

    @Autowired private Map<String, OpenbatonCreateServer> deploymentMap = new HashMap<>();
    private Logger logger;
    private SecureRandom appIDGenerator;

    @Autowired private OpenshiftManager osmanager;
    @Autowired private OpenbatonManager obmanager;
    @Autowired private ApplicationRepository appRepo;
    @Autowired private PaaSProperties paaSProperties;

    @PostConstruct
    private void init() {
        System.setProperty("javax.net.ssl.trustStore", "/opt/nubomedia/nubomedia-paas/resource/openshift-keystore");
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.appIDGenerator = new SecureRandom();
    }

    @RequestMapping(value = "/app",  method = RequestMethod.POST)
    public @ResponseBody NubomediaCreateAppResponse createApp(@RequestBody NubomediaCreateAppRequest request) throws SDKException, UnauthorizedException, DuplicatedException, NameStructureException, turnServerException, StunServerException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        if(request.getAppName().length() > 18){

            throw new NameStructureException("Name is too long");

        }

        if(request.getAppName().contains(".")){

            throw new NameStructureException("Name can't contains dots");

        }

        if(!request.getAppName().matches("[a-z0-9]+(?:[._-][a-z0-9]+)*")){
            throw new NameStructureException("Name must match [a-z0-9]+(?:[._-][a-z0-9]+)*");
        }

        if(!appRepo.findByAppName(request.getAppName()).isEmpty()){
            throw new DuplicatedException("Application with " + request.getAppName() + " already exist");
        }

        logger.debug("REQUEST" + request.toString());

        List<String> protocols = new ArrayList<>();
        List<Integer> targetPorts = new ArrayList<>();
        List<Integer> ports = new ArrayList<>();

        for (int i = 0; i < request.getPorts().length; i++){

            protocols.add(request.getPorts()[i].getProtocol());
            targetPorts.add(request.getPorts()[i].getTargetPort());
            ports.add(request.getPorts()[i].getPort());

        }

        NubomediaCreateAppResponse res = new NubomediaCreateAppResponse();
        String appID = new BigInteger(130,appIDGenerator).toString(64);
        logger.debug("App ID " + appID + "\n");

        logger.debug("request params " + request.getAppName() + " " + request.getGitURL() + ports + " " + protocols + " " + request.getReplicasNumber());

        //Openbaton MediaServer Request
        logger.info("[PAAS]: EVENT_APP_CREATE " + new Date().getTime());
        OpenbatonCreateServer openbatonCreateServer = obmanager.getMediaServerGroupID(request.getFlavor(),appID,paaSProperties.getInternalURL(),request.isCloudRepository(), request.getQualityOfService(),request.isTurnServerActivate(),request.getTurnServerUrl(),request.getTurnServerUsername(),request.getTurnServerPassword(),request.isStunServerActivate(), request.getStunServerIp(), request.getStunServerPort(), request.getScaleInOut(),request.getScale_out_threshold());

        deploymentMap.put(appID,openbatonCreateServer);

        Application persistApp = new Application(appID,request.getFlavor(),request.getAppName(),projectName,"",openbatonCreateServer.getMediaServerID(), request.getGitURL(), targetPorts, ports, protocols,null, request.getReplicasNumber(),request.getSecretName(),false);
        appRepo.save(persistApp);

        res.setApp(persistApp);
        res.setCode(200);
        return res;
    }

    @RequestMapping(value = "/app/{id}", method =  RequestMethod.GET)
    public @ResponseBody Application getApp(@PathVariable("id") String id) throws ApplicationNotFoundException, UnauthorizedException {

        logger.info("Request status for " + id + " app");

        if(!appRepo.exists(id)){
            throw new ApplicationNotFoundException("Application with ID not found");
        }

        Application app = appRepo.findFirstByAppID(id);
        logger.debug("Retrieving status for " + app.toString() + "\nwith status " + app.getStatus());

        app.setStatus(this.getStatus(app));

        appRepo.save(app);

        return app;

    }

    @RequestMapping(value = "/app/{id}/buildlogs", method = RequestMethod.GET)
    public @ResponseBody NubomediaBuildLogs getBuildLogs(@PathVariable("id") String id) throws UnauthorizedException {

        NubomediaBuildLogs res = new NubomediaBuildLogs();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        if(!appRepo.exists(id)){
            return null;
        }

        Application app = appRepo.findFirstByAppID(id);

        if(app.getStatus().equals(BuildingStatus.FAILED) && !app.isResourceOK()){

            res.setId(id);
            res.setAppName(app.getAppName());
            res.setLog("Something wrong on retrieving resources");

        } else if(app.getStatus().equals(BuildingStatus.CREATED) || app.getStatus().equals(BuildingStatus.INITIALIZING)){
            res.setId(id);
            res.setAppName(app.getAppName());
            res.setLog("The application is retrieving resources " + app.getStatus());

            return res;
        } else if (app.getStatus().equals(BuildingStatus.PAAS_RESOURCE_MISSING)){
            res.setId(id);
            res.setAppName(app.getAppName());
            res.setLog("PaaS components are missing, send an email to the administrator to chekc the PaaS status");

            return res;
        } else {

            res.setId(id);
            res.setAppName(app.getAppName());
            try {
                res.setLog(osmanager.getBuildLogs(app.getAppName(), projectName));
            } catch (ResourceAccessException e) {
                app.setStatus(BuildingStatus.PAAS_RESOURCE_MISSING);
                appRepo.save(app);
                res.setLog("Openshift is not responding, app " + app.getAppName() + " is not anymore available");
            }
        }

        return res;
    }

    @RequestMapping(value = "/app/{id}/logs/{podName}", method = RequestMethod.GET)
    public @ResponseBody String getApplicationLogs(@PathVariable("id") String id,@PathVariable("podName") String podName) throws UnauthorizedException, ApplicationNotFoundException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        if(!appRepo.exists(id)){
            throw new ApplicationNotFoundException("Application with ID not found");
        }

        Application app = appRepo.findFirstByAppID(id);

        if(!app.getStatus().equals(BuildingStatus.RUNNING)){
            return "Application Status " + app.getStatus() + ", logs are not available until the status is RUNNING";
        }

        return osmanager.getApplicationLog(app.getAppName(),projectName,podName);

    }

    @RequestMapping(value = "/app", method = RequestMethod.GET)
    public @ResponseBody Iterable<Application> getApps() throws UnauthorizedException, ApplicationNotFoundException {

        //BETA
        Iterable<Application> applications = this.appRepo.findAll();

        for (Application app : applications){
            app.setStatus(this.getStatus(app));
        }

        this.appRepo.save(applications);

        return applications;
    }

    @RequestMapping(value = "/app/{id}", method = RequestMethod.DELETE)
    public @ResponseBody NubomediaDeleteAppResponse deleteApp(@PathVariable("id") String id) throws UnauthorizedException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        logger.debug("id " + id);

        if(!appRepo.exists(id)){
            return new NubomediaDeleteAppResponse(id,"Application not found",404);
        }

        Application app = appRepo.findFirstByAppID(id);
        app.setStatus(this.getStatus(app));
        logger.debug("Deleting " + app.toString());

        if (!app.isResourceOK()){

            String name = app.getAppName();

            if(app.getStatus().equals(BuildingStatus.CREATED) || app.getStatus().equals(BuildingStatus.INITIALIZING) || app.getStatus().equals(BuildingStatus.FAILED)){
                OpenbatonCreateServer server = deploymentMap.get(id);
                obmanager.deleteDescriptor(server.getNsdID());
                obmanager.deleteEvent(server.getEventAllocatedID());
                obmanager.deleteEvent(server.getEventErrorID());

                if (!app.getStatus().equals(BuildingStatus.FAILED) && obmanager.existRecord(server.getMediaServerID())) {
                    obmanager.deleteRecord(app.getNsrID());
                }
                deploymentMap.remove(app.getAppID());

            }

            appRepo.delete(app);
            return new NubomediaDeleteAppResponse(id,name,200);

        }

        if (app.getStatus().equals(BuildingStatus.PAAS_RESOURCE_MISSING)){
            obmanager.deleteRecord(app.getNsrID());
            appRepo.delete(app);

            return new NubomediaDeleteAppResponse(id,app.getAppName(),200);
        }

//        if (app.getStatus().equals(BuildingStatus.CREATED) || app.getStatus().equals(BuildingStatus.INITIALIZING)){
//
//            obmanager.deleteRecord(app.getNsrID());
//            return new NubomediaDeleteAppResponse(id,app.getAppName(),app.getProjectName(),200);
//
//        }


        obmanager.deleteRecord(app.getNsrID());
        HttpStatus resDelete = HttpStatus.BAD_REQUEST;
        try {
            resDelete = osmanager.deleteApplication(app.getAppName(), projectName);
        } catch (ResourceAccessException e){
            logger.info("PaaS Missing");
        }

        appRepo.delete(app);

        return new NubomediaDeleteAppResponse(id,app.getAppName(),resDelete.value());
    }

    @RequestMapping(value = "/secret", method = RequestMethod.POST)
    public @ResponseBody String createSecret(@RequestBody NubomediaCreateSecretRequest ncsr) throws UnauthorizedException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        logger.debug("Creating new secret for " + projectName + " with key " + ncsr.getPrivateKey());
        return osmanager.createSecret(ncsr.getPrivateKey(), projectName);
    }

    @RequestMapping(value = "/secret/{secretName}", method = RequestMethod.DELETE)
    public @ResponseBody NubomediaDeleteSecretResponse deleteSecret (@PathVariable("secretName") String secretName) throws UnauthorizedException {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        HttpStatus deleteStatus = osmanager.deleteSecret(secretName, projectName);
        return new NubomediaDeleteSecretResponse(secretName,projectName,deleteStatus.value());
    }

    private BuildingStatus getStatus(Application app) throws UnauthorizedException {

        BuildingStatus res = null;
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String projectName = userDetails.getUsername();

        switch (app.getStatus()) {
            case CREATED:
                res = obmanager.getStatus(app.getNsrID());
                break;
            case INITIALIZING:
                res = obmanager.getStatus(app.getNsrID());
                break;
            case INITIALISED:
                try {
                    res = osmanager.getStatus(app.getAppName(), app.getProjectName());
                } catch (ResourceAccessException e) {
                    res = BuildingStatus.PAAS_RESOURCE_MISSING;
                }
                break;
            case BUILDING:
                try {
                    res = osmanager.getStatus(app.getAppName(), app.getProjectName());
                } catch (ResourceAccessException e) {
                    res = BuildingStatus.PAAS_RESOURCE_MISSING;
                }
                break;
            case DEPLOYNG:
                try {
                    res = osmanager.getStatus(app.getAppName(), app.getProjectName());
                } catch (ResourceAccessException e) {
                    res = BuildingStatus.PAAS_RESOURCE_MISSING;
                }
                break;
            case FAILED:
                logger.debug("FAILED: app has resource ok? " + app.isResourceOK());
                if (!app.isResourceOK()) {
                    res = BuildingStatus.FAILED;
                    break;
                } else {
                    try {
                        res = osmanager.getStatus(app.getAppName(), app.getProjectName());
                    } catch (ResourceAccessException e) {
                        res = BuildingStatus.PAAS_RESOURCE_MISSING;
                    }
                }
                break;
            case RUNNING:
                try {
                    res = osmanager.getStatus(app.getAppName(), app.getProjectName());
                } catch (ResourceAccessException e) {
                    res = BuildingStatus.PAAS_RESOURCE_MISSING;
                }
                break;
            case PAAS_RESOURCE_MISSING:
                res = osmanager.getStatus(app.getAppName(), app.getProjectName());
                break;
        }

        return res;
    }

}

