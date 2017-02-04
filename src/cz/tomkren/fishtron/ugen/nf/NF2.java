package cz.tomkren.fishtron.ugen.nf;

import cz.tomkren.fishtron.types.Type;

/** Created by user on 3. 2. 2017. */

public class NF2 {

    private Renaming renaming_skol;
    private Renaming renaming_vars;



    public NF2(Type t) {

        MkNF renamings = new MkNF(t);
        renaming_skol = renamings.getRenaming_skol();
        renaming_vars = renamings.getRenaming_vars();
    }

    public Type toNF(Type t) {
        Type t1 = renaming_skol.applyAsSkolems(t);
        return renaming_vars.applyAsVars(t1);
    }

    public Type fromNF(Type t) {
        Type t1 = renaming_skol.applyReverseAsSkolems(t);
        return renaming_vars.applyReverseAsVars(t1);
    }

}