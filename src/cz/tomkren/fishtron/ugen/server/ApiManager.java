package cz.tomkren.fishtron.ugen.server;

import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Created by Tomáš Křen on 19.2.2017.*/

class ApiManager implements Api {

    private final Map<String,Api> apiCmds;


    ApiManager(JobManager jobMan) {
        apiCmds = new HashMap<>();

        addApiCmd("run",  jobMan::runJob);
        addApiCmd("job",  jobMan::getJobInfo);
        addApiCmd("log",  jobMan::getJobLog);
        addApiCmd("jobs", jobMan::getJobsInfo);
    }

    private void addApiCmd(String cmdName, Api apiCmd) {
        apiCmds.put(cmdName, apiCmd);
    }

    private void addApiCmd(String cmdName, Supplier<JSONObject> apiCmd) {
        addApiCmd(cmdName, (p,q) -> apiCmd.get());
    }


    JSONObject process(String path, String query) {

        String[] pathParts = path.split("/");
        JSONArray pathJson = F.jsonMap(F.filter(Arrays.asList(pathParts), x->!x.equals("")), x->x);

        if (query == null) {
            if (pathJson.length() == 0) {
                return mkIndexResponse();
            } else {
                return process(pathJson, null);
            }
        }

        try {
            JSONObject jsonRequest = new JSONObject(query);
            return process(pathJson, jsonRequest);
        } catch (JSONException e) {
            try {
                JSONObject jsonRequest = parseStdQuery(query, "&");
                return process(pathJson, jsonRequest);
            } catch (ParseException pe) {
                return mkErrorResponse("Unsupported query detected, query must be a JSON or stdQuery.");
            }
        } catch (Exception e) {
            return mkUnexpectedErrorResponse(e);
        }
    }

    @Override
    public JSONObject process(JSONArray path, JSONObject query) {

        if (query == null) {
            query = F.obj();
        }

        String cmd = null;

        if (path.length() > 0) {
            cmd = path.getString(0); // higher priority than cmd field in query
            query.put("cmd", cmd);   // and it overrides it, so we know that actual cmd is in the query
        } else if (query.has("cmd") && query.get("cmd") instanceof String) {
            cmd = query.getString("cmd");
        }

        if (cmd != null) {

            Api apiCmd = apiCmds.get(cmd);

            if (apiCmd != null) {
                return apiCmd.process(path, query);
            }
        }

        return F.obj(
                "status", "error",
                "msg", "Unsupported command.",
                "path", path,
                "query", query
        );
    }

    private static JSONObject mkIndexResponse() {
        return Api.addOk(F.obj("msg","Welcome to EvaServer API!"));
    }

    static JSONObject mkErrorResponse(String msg) {
        return F.obj(
                "status","error",
                "msg",msg
        );
    }

    private static JSONObject mkUnexpectedErrorResponse(Exception e) {
        String msg = (e.getMessage() == null ? "" : " ... " + e.getMessage());
        return mkErrorResponse("Unexpected error occurred: " + e.toString() + msg);
    }

    private static JSONObject parseStdQuery(String query, String sepRegexp) throws ParseException {

        String[] parts = query.split(sepRegexp);
        JSONObject ret = new JSONObject();

        int offset = 0;
        for (String part : parts) {
            String[] subParts = part.split("=");
            if (subParts.length == 2) {
                ret.put(subParts[0], subParts[1]);
            } else {
                throw new ParseException("Wrong format of query part: "+part, offset);
            }
            offset += part.length();
        }

        return ret;
    }


}
