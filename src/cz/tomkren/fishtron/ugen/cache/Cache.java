package cz.tomkren.fishtron.ugen.cache;

import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.fishtron.ugen.cache.data.SubData;
import cz.tomkren.fishtron.ugen.data.PreSubsRes;
import cz.tomkren.fishtron.ugen.Mover;
import cz.tomkren.fishtron.ugen.data.SubsRes;
import cz.tomkren.fishtron.ugen.data.Ts1Res;
import cz.tomkren.utils.F;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.*;

/** Created by user on 13. 9. 2016.*/

public class Cache {

    private Gen gen;

    private Map<String, TypeData> typeDataMap;
    private List<SubData> subsList;
    private Map<String, Integer> sub2id;

    public Cache(Gen gen) {
        this.gen = gen;
        typeDataMap = new HashMap<>();
        subsList = new ArrayList<>();
        sub2id = new HashMap<>();
    }

    // -- main public interface ----------------------------------------------------------------------

    public List<Ts1Res> ts1(Type t, int n) {
        List<Ts1Res> ts1results_0 = getTypeData(t).ts1(t, gen, this);
        return Mover.moveTs1Results_0(t, n, ts1results_0);
     }

    public List<SubsRes> subs(int k, Type t, int n) {
        List<SubsRes> results_0 = getSizeTypeData(k, t).getSubsData(gen, this, k, t);
        return Mover.moveSubsResults_0(t, n, results_0);
    }

    public BigInteger getNum(int k, Type t) {
        return getSizeTypeData(k, t).computeNum(gen, this, k, t);
    }

    // -- private methods ---------------------------------------------------------------------------

    private TypeData getTypeData(Type t) {
        return typeDataMap.computeIfAbsent(t.toString(), t_str -> new TypeData());
    }

    private SizeData getSizeTypeData(int k, Type t) {
        return getTypeData(t).getSizeData(k);
    }

    int addSub(Sub sub) {
        String sub_str = sub.toString();
        Integer sub_id = sub2id.get(sub_str);
        if (sub_id == null) {
            sub_id = subsList.size();
            subsList.add(new SubData(sub));
            sub2id.put(sub_str, sub_id);
        } else {
            subsList.get(sub_id).incrementNumUsed();
        }
        return sub_id;
    }

    Sub getSub(int sub_id) {
        return subsList.get(sub_id).getSub();
    }


    // -- Stats ------------------------------------------------------

    public int getNumCachedSubs() {
        return subsList.size();
    }

    public String getCachedSubsStats() {

        double sumNumUsed = 0.0;
        int maxNumUsed = 0;

        for (SubData subData : subsList) {
            int numUsed = subData.getNumUsed();
            sumNumUsed += numUsed;
            if (numUsed > maxNumUsed) {
                maxNumUsed = numUsed;
            }
        }
        int numSubs = subsList.size();
        double meanNumUsed = sumNumUsed / numSubs;

        return "numCachedSubs : "+numSubs+"\n"+
               "sumNumUsed    : "+sumNumUsed+"\n"+
               "meanNumUsed   : "+meanNumUsed+"\n"+
               "maxNumUsed    : "+maxNumUsed;
    }

    // -- toJson method and its utils ---------------------------------------------------------------

    public JSONObject toJson() {
        return F.obj(
                "types", typesToJson(typeDataMap),
                "subs",  subsToJson(subsList)
        );
    }

    private static JSONObject typesToJson(Map<String,TypeData> typeDataMap) {
        return F.jsonMap(typeDataMap, TypeData::toJson);
    }

    private static JSONArray subsToJson(List<SubData> subsList) {
        return F.jsonMap(subsList, SubData::toJson);
    }

    private static JSONObject subsToJson_debugVersion(List<Sub> subsList) {
        JSONObject ret = new JSONObject();
        for (int i = 0; i < subsList.size(); i++) {
            ret.put(Integer.toString(i), subsList.get(i).toJson());
        }
        return ret;
    }


}
