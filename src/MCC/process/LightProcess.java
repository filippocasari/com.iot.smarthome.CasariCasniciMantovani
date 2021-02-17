package MCC.process;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.coap.EnergyResource;
import MCC.coap.SwitchResource;
import MCC.resource.actuator.SwitchActuator;
import MCC.resource.sensor.EnergySensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LightProcess extends CoapServer {
    private final static Logger logger = LoggerFactory.getLogger(LightProcess.class);

    public LightProcess() {
        super();
        String deviceId = String.format("dipi:iot:%s", UUID.randomUUID().toString());
        this.add(createLightResource(deviceId));
    }

    private CoapResource createLightResource(String deviceId){

        CoapResource lightsRootResource = new CoapResource("lights");

        EnergySensor lightsEnergySensor = new EnergySensor();
        SwitchActuator lightsSwitchActuator = new SwitchActuator();

        EnergyResource lightsEnergyResource = new EnergyResource(deviceId, "energy", lightsEnergySensor);
        SwitchResource lightsSwitchResource = new SwitchResource(deviceId, "switch", lightsSwitchActuator);
        if(!lightsSwitchResource.getOn()){
            lightsEnergyResource.setUpdatedEnergyValue(0.0);

        }

        lightsRootResource.add(lightsEnergyResource);
        lightsRootResource.add(lightsSwitchResource);

        //Handle Emulated Resource notification
        lightsSwitchActuator.addDataListener(new DataListener<Boolean>() {
            @Override
            public void onDataChanged(SmartObject<Boolean> resource, Boolean updatedValue) {
                logger.info("[LIGHTS-BEHAVIOUR] -> Updated Switch Value: {}", updatedValue);
                logger.info("[LIGHTS-BEHAVIOUR] -> Updating energy sensor configuration ...");
                lightsEnergySensor.setActive(updatedValue);
            }
        });

        return lightsRootResource;
    }

    public static void main(String[] args) {

        LightProcess lightCoapProcess = new LightProcess();
        lightCoapProcess.start();

        logger.info("Coap Server Started! Available resources: ");

        lightCoapProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });

    }
}