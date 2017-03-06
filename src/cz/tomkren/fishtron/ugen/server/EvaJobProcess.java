package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.fishtron.server.EvolutionJob;
import cz.tomkren.utils.F;
import cz.tomkren.utils.Log;
import org.json.JSONObject;

/** Created by tom on 05.03.2017. */

public class EvaJobProcess {

    enum Status {beforeStart, running, finished}

    private Status status;
    private final int jobId;
    private final EvaJob job;
    private final JSONObject jobOpts;
    private final StringBuffer output;

    EvaJobProcess(int jobId, EvaJob job, JSONObject jobOpts) {
        setStatus(Status.beforeStart);
        this.jobId = jobId;
        this.job = job;
        this.jobOpts = jobOpts;

        if (jobOpts.has("cmd")) {
            jobOpts.remove("cmd");
        }

        output = new StringBuffer();
    }

    public void start() {
        setStatus(Status.running);
        (new Thread(()->{
            job.run(jobOpts, this);
            setStatus(Status.finished);
        })).start();
    }

    public void log(Object x) {
        output.append(x).append('\n');
        Log.it(x);
    }

    public String getLog() {
        return output.toString();
    }

    private synchronized void setStatus(Status newStatus) {
        status = newStatus;
    }

    private synchronized Status getStatus() {
        return status;
    }

    public JSONObject toJson() {
        return F.obj(
                "jobId", jobId,
                "jobOpts", jobOpts,
                "jobStatus", getStatus().name()
        );
    }

}
