package cz.tomkren.fishtron.ugen.data;

import cz.tomkren.fishtron.types.Sub;

import java.math.BigInteger;

/** Created by user on 2. 2. 2017.*/

public class Ts1Res {
    private final String s;
    private final Sub sigma;
    private final int nextVarId;

    public Ts1Res(String s, Sub sigma, int nextVarId) {
        this.s = s;
        this.sigma = sigma;
        this.nextVarId = nextVarId;
    }

    public String getSym() {return s;}
    public Sub getSigma() {return sigma;}
    public int getNextVarId() {return nextVarId;}

    public SubsRes toSubsRes() {
        return new SubsRes(BigInteger.ONE, sigma, nextVarId);
    }
}