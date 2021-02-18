package com.company.Control_unit;


//import com.company.Control_unit.ClientsType.GETClient;
//import com.company.Control_unit.ClientsType.POSTClient;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.CoapHandler;

public class MOVEMENTdetenctionTask implements Runnable {
    public Double Consuption = 0.0;
    public static String URLmovement;

    int count = 0;
    //private final static Logger logger = LoggerFactory.getLogger(LIGHTSConsumptionTask.class);

    public MOVEMENTdetenctionTask(String URLmovement) {

        this.URLmovement = URLmovement;

    }


    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URLmovement);
        System.out.println("OBSERVING MOVEMENT sensor... @ " + URLmovement);


        Request request = Request.newGet().setURI(URLmovement).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                if (content.equals("false")) {
                    ControlUnit.settingEcomodeOn();
                }

            }

            public void onError() {
                System.err.println("OBSERVING WASHER FAILED");
                //logger.error("OBSERVING LIGHTS FAILED");
            }
        });
        try {
            Thread.sleep(60 * 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Observes the coap resource for 30 seconds then the observing relation is deleted

        System.err.println("CANCELLATION...");
        //logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }


    @Override
    public void run() {
        createGetRequestObserving();
    }
}

