package cz.tomkren.fishtron.sandbox2;

import cz.tomkren.fishtron.eva.FitIndiv;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.Log;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**  Created by tom on 24. 6. 2016.*/

public interface Dag_IEvalManager<Indiv extends FitIndiv> extends EvalManager<Indiv> {

    JSONObject getAllParamsInfo(String datasetFilename) throws XmlRpcException;
    int getCoreCount();
    Object submit(List<AB<Indiv,JSONObject>> indivs);
    EvalResult<Indiv> getEvaluated();
    String quitServer();

    @Override
    default EvalResult<Indiv> justAskForResults() {
        return getEvaluated();
    }

    @Override
    default EvalResult<Indiv> evalIndividuals(List<AB<Indiv,JSONObject>> indivs) {
        submit(indivs);
        return getEvaluated();
    }

    @Override
    default int getEvalPoolSize(int suggestedPoolSize) {
        return getCoreCount();
    }

}
