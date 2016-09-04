
package cz.tomkren.fishtron.latticegen;


import cz.tomkren.fishtron.types.Sub;
import cz.tomkren.fishtron.types.Type;
import cz.tomkren.fishtron.types.Types;
import cz.tomkren.utils.AA;
import cz.tomkren.utils.AB;
import cz.tomkren.utils.F;

import com.google.common.base.Joiner;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/** Created by user on 27. 7. 2016.*/

// TODO opakujou se tu kody, předelat na abstract dědičnost asi...

interface AppTree {

    static AppTree mk(String sym, Type type) {
        return new AppTree.Leaf(sym, type);
    }

    static AppTree mk(AppTree funTree, AppTree argTree, Type type) {
        return new AppTree.App(funTree, argTree, type);
    }

    Type getType();
    void deskolemize(Set<Integer> ids);
    void applySub(Sub sub);
    String toRawString();

    boolean isStrictlyWellTyped();
    JSONObject getTypeTrace();

    void updateDebugInfo(Function<JSONObject,JSONObject> updateFun);

    class Leaf implements AppTree {
        private String sym;
        private Type type;
        private JSONObject debugInfo;

        private Leaf(String sym, Type type) {
            this.sym = sym;
            this.type = type;
            this.debugInfo = null;
        }

        @Override public Type getType() {return type;}
        @Override public void deskolemize(Set<Integer> ids) {type = type.deskolemize(ids);}
        @Override public void applySub(Sub sub) {type = sub.apply(type);}
        @Override public String toString() {return sym;}
        @Override public String toRawString() {return sym;}
        @Override public boolean isStrictlyWellTyped() {return true;}

        @Override public JSONObject getTypeTrace() {
            JSONObject typeTrace = F.obj("node",sym, "type",type.toJson());
            if (debugInfo != null) {typeTrace.put("debugInfo", debugInfo);}
            return typeTrace;
        }

        @Override
        public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
            debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
        }
    }

    class App implements AppTree {

        private AppTree funTree;
        private AppTree argTree;
        private Type type;
        private JSONObject debugInfo;


        private App(AppTree funTree, AppTree argTree, Type type) {
            this.funTree = funTree;
            this.argTree = argTree;
            this.type = type;
            this.debugInfo = null;
        }

        @Override public Type getType() {return type;}

        @Override
        public boolean isStrictlyWellTyped() {
            return isRootStrictlyWellTyped() && funTree.isStrictlyWellTyped() && argTree.isStrictlyWellTyped();
        }

        private boolean isRootStrictlyWellTyped() {
            Type funType = funTree.getType();
            Type argType = argTree.getType();
            AA<Type> fun = Types.splitFunType(funType);
            return isSameType(fun._1(), argType) && isSameType(fun._2(), type);
        }

        @Override
        public JSONObject getTypeTrace() {
            JSONObject typeTrace = F.obj(
                    "node","@",
                    "type",type.toJson(),
                    "fun",funTree.getTypeTrace(),
                    "arg",argTree.getTypeTrace()
            );

            if (!isRootStrictlyWellTyped()) {
                typeTrace.put("error",true);
            }

            if (debugInfo != null) {
                typeTrace.put("debugInfo", debugInfo);
            }

            return typeTrace;
        }

        private boolean isSameType(Type t1, Type t2) {
            return t1.toString().equals(t2.toString());
        }

        @Override
        public String toRawString() {
            return "("+funTree.toRawString()+" "+argTree.toRawString()+")";
        }

        @Override
        public String toString() {
            AB<AppTree.Leaf,List<AppTree>> p = getFunLeafWithArgs();
            return "("+p._1()+" "+ Joiner.on(' ').join(p._2())+")";
        }

        private AB<Leaf,List<AppTree>> getFunLeafWithArgs() {
            AppTree acc = this;
            List<AppTree> argTrees = new ArrayList<>();

            while (acc instanceof App) {
                App app = (App) acc;
                argTrees.add(app.argTree);
                acc = app.funTree;
            }

            Collections.reverse(argTrees);
            return new AB<>((Leaf)acc, argTrees);
        }

        @Override
        public void deskolemize(Set<Integer> ids) {
            type = type.deskolemize(ids);
            funTree.deskolemize(ids);
            argTree.deskolemize(ids);
        }

        @Override
        public void applySub(Sub sub) {
            type = sub.apply(type);
            funTree.applySub(sub);
            argTree.applySub(sub);
        }

        @Override
        public void updateDebugInfo(Function<JSONObject, JSONObject> updateFun) {
            debugInfo = updateFun.apply(debugInfo == null ? new JSONObject() : debugInfo);
        }
    }

}