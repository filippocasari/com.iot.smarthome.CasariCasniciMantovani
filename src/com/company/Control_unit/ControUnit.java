package com.company.Control_unit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


class ControlUnit {
    private final static Logger logger = LoggerFactory.getLogger(ControlUnit.class);

    private static final String COAP_ENDPOINT_ENERGY_LIGHTS = "coap://127.0.0.1:5683/lights/energy";
    private static final String COAP_ENDPOINT_SWITCH_LIGHTS = "coap://127.0.0.1:5683/lights/switch";
    private static final String COAP_ENDPOINT_SWITCH_TV = "coap://127.0.0.1:5683/tv/switch";
    private static final String COAP_ENDPOINT_ENERGY_TV = "coap://127.0.0.1:5683/tv/energy";
    private static final String COAP_ENDPOINT_ENERGY_HEATING = "coap://192.168.0.169:5683/heating-system/energy";
    //private static final String COAP_ENDPOINT_SWITCH_FRIDGE = "coap://127.0.0.1:5683/fridge/switch";
    private static final String COAP_ENDPOINT_SWITCH_HEATING = "coap://127.0.0.1:5683/heating-system/switch";
    private static final String COAP_ENDPOINT_ENERGY_FRIDGE = "coap://127.0.0.1:5683/fridge/energy";
    private boolean EcoMode = false;

    public ControlUnit(SimTime simTime) throws BrokenBarrierException, InterruptedException {

        //Creation of Energy Consumption Monitoring Tasks
        TVConsumptionTask tvConsuptionTask = new TVConsumptionTask(COAP_ENDPOINT_ENERGY_TV, COAP_ENDPOINT_SWITCH_TV);
        FRIDGEConsumptionTask fridgeConsumptionTask = new FRIDGEConsumptionTask(COAP_ENDPOINT_ENERGY_FRIDGE);
        LIGHTSConsumptionTask lightsConsumptionTask = new LIGHTSConsumptionTask(COAP_ENDPOINT_ENERGY_LIGHTS, COAP_ENDPOINT_SWITCH_LIGHTS);
        HEATINGConsumptionTask heatingConsumptionTask = new HEATINGConsumptionTask(COAP_ENDPOINT_ENERGY_HEATING, COAP_ENDPOINT_SWITCH_HEATING);

        simTime.setSpeed(20);
        simTime.start();


        Runnable PeriodicTaskEcoMode = () -> {
            while(true){
                if (checkEcoMode(simTime)) {
                    logger.info("Periodic Consumption Control...");
                    System.out.println(simTime.getDay()+", "+simTime.getHour()+" : "+simTime.getMinute());
                }

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }}
        };
        Thread periodicTaskEcoMode = new Thread(PeriodicTaskEcoMode);

        periodicTaskEcoMode.start();
        //create new Task for Energy Consumption
        Thread t1 = new Thread(fridgeConsumptionTask);
        Thread t2 = new Thread(lightsConsumptionTask);
        Thread t3 = new Thread(tvConsuptionTask);
        Thread t4 = new Thread(heatingConsumptionTask);
        //start thread for observable resource energy
        t1.start();
        t2.start();
        t3.start();
        t4.start();





    }


    public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
        SimTime simTime = new SimTime();

        System.out.println("Starting Time...\nDay: " + simTime.getDay().toString());
        System.out.println("\tHour: " + simTime.getHour());
        System.out.println("\tMinute: " + simTime.getMinute());
        System.out.println("\tSecond: " + simTime.getSecond());

        ControlUnit controUnit = new ControlUnit(simTime);


    }

    public static boolean checkConsumption(Double TotalConsumption, Double InstantConsumption) {
        return TotalConsumption > 50 || InstantConsumption > 2.0;

    }

    public boolean checkEcoMode(SimTime simTime) {
        EcoMode = simTime.getDay().toString().equals("Sunday")
                || (simTime.getHour() > 22 || simTime.getHour() < 6);
        logger.info("Eco Mode: " + EcoMode);
        return EcoMode;

    }


}
