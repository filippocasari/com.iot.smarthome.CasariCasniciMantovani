package MCC.CoapResource;

import MCC.DataListener;
import MCC.SmartObject;
import MCC.EmulatedResource.Sensor.EnergySensor;
import MCC.EmulatedResource.Sensor.TemperatureSensor;
import MCC.Utils.CoreInterfaces;
import MCC.Utils.SenMLPack;
import MCC.Utils.SenMLRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class EnergyResource extends CoapResource {

    private final static Logger logger = LoggerFactory.getLogger(EnergyResource.class);
    private static final String OBJECT_TITLE = "EnergyConsumptionSensor";
    private static final Number SENSOR_VERSION = 0.1;

    private String UNIT = "Wh";
    private EnergySensor rawSensor;
    private ObjectMapper objectMapper;

    public Double updatedEnergyValue = 0.0;
    private String deviceId;


    public void setConsumptionNull(){
        this.updatedEnergyValue = 0.0;
    }

    public Double getUpdatedEnergyValue() {
        return updatedEnergyValue;
    }

    public void setUpdatedEnergyValue(Double updatedEnergyValue) {
        this.updatedEnergyValue = updatedEnergyValue;
    }

    public EnergyResource(String deviceId, String name, EnergySensor rawSensor) {

        super(name);

        if(rawSensor != null && deviceId != null){

            this.deviceId = deviceId;

            this.rawSensor = rawSensor;

            //Jackson Object Mapper + Ignore Null Fields in order to properly generate the SenML Payload
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true); // enable observing
            setObserveType(CoAP.Type.CON); // configure the notification type to CONs

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", rawSensor.getType());
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));
        }
        else
            logger.error("Error -> NULL Raw Reference !");

        this.rawSensor.addDataListener(new DataListener<Double>() {
            @Override
            public void onDataChanged(SmartObject<Double> resource, Double updatedValue) {
                updatedEnergyValue = updatedValue;
                changed();
            }
        });

    }

    private Optional<String> getJsonSenmlResponse(){

        try{

            SenMLPack senMLPack = new SenMLPack();

            SenMLRecord senMLRecord = new SenMLRecord();
            senMLRecord.setBn(String.format("%s:%s", this.deviceId, this.getName()));
            senMLRecord.setBver(SENSOR_VERSION);
            senMLRecord.setU(UNIT);
            senMLRecord.setV(updatedEnergyValue);
            senMLRecord.setT(System.currentTimeMillis());

            senMLPack.add(senMLRecord);

            return Optional.of(this.objectMapper.writeValueAsString(senMLPack));

        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public void handleGET(CoapExchange exchange){

        // the Max-Age value should match the update interval
        exchange.setMaxAge(TemperatureSensor.UPDATE_PERIOD);

        //If the request specify the MediaType as JSON or JSON+SenML
        if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON ||
                exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON){

            Optional<String> senmlPayload = getJsonSenmlResponse();

            if(senmlPayload.isPresent())
                exchange.respond(CoAP.ResponseCode.CONTENT, senmlPayload.get(), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }
        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedEnergyValue), MediaTypeRegistry.TEXT_PLAIN);

    }
}