package com.company.Control_unit;

import it.unimore.dipi.iot.client.CoapGetSenmlClientProcess;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HEATINGConsumptionTask extends Thread {
    public int Consuption = 0;
    public static String URL;
    private final static Logger logger = LoggerFactory.getLogger(HEATINGConsumptionTask.class);

    public HEATINGConsumptionTask(String URLserver) {
        super("FRIDGE TASK CONSUPTION");
        URL = URLserver;

    }

    @Override
    public void run() {
        createGetRequestObserving();
    }

    private void createGetRequestObserving() {
        CoapClient client = new CoapClient(URL);

        logger.info("OBSERVING ... {}", URL);

        Request request = Request.newGet().setURI(URL).setObserve();
        request.setConfirmable(true);


        CoapObserveRelation relation = client.observe(request, new CoapHandler() {

            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                parseContent(content);
                logger.info("NOTIFICATION Body: " + content);
            }

            public void onError() {
                logger.error("OBSERVING FAILED");
            }
        });

        // Observes the coap resource for 30 seconds then the observing relation is deleted
        try {
            Thread.sleep(60 * 2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("CANCELLATION.....");
        relation.proactiveCancel();
    }

    private void parseContent(String content) {
        String[] Parsedtext = content.split("},");
        for(int i=0; i<Parsedtext.length-1; i++){

        }
    }
}
