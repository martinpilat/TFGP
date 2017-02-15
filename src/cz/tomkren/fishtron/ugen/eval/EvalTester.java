package cz.tomkren.fishtron.ugen.eval;

import cz.tomkren.fishtron.ugen.AppTree;
import cz.tomkren.fishtron.ugen.Gamma;
import cz.tomkren.fishtron.ugen.Gen;
import cz.tomkren.utils.Checker;
import cz.tomkren.utils.Log;

/** Created by user on 14. 2. 2017. */

public class EvalTester {

    public static void main(String[] args) {
        Checker ch = new Checker();

        EvalLib lib = EvalLib.mk(
                "0", 0.0,
                "s", (Fun)  x -> (double)x + 1,
                "+", (Fun2) x -> (y -> (double)x + (double)y)
        );

        Gamma gamma = Gamma.mk(
                "0", "Num",
                "s", "Num -> Num",
                "+", "Num -> (Num -> Num)"
        );

        Gamma gamma2 = Gamma.mk(
                "0",      "Zero",
                "s",      "n -> (S n)",
                "++",     "(Plus x y z) -> (x -> (y -> z))",
                "plus_0", "Plus x Zero x",
                "plus_n", "(Plus x y z) -> (Plus x (S y) (S z))"
        );


        testLib(ch, 64, lib, gamma, "Num", true);
        testLib(ch, 4, lib, gamma2, "(S (S Zero))", true); // todo pro typ "n" to spadne na asserci v genOne že rawType je stejnej jako toho stromu (což je dobře ale vyřešit)


        ch.results();
    }


    private static void testLib(Checker ch, int k_max, EvalLib lib, Gamma gamma, String goalStr, boolean testEvaluation) {

        Log.it("Goal = "+goalStr);

        Gen gen = new Gen(gamma, ch.getRandom());
        boolean allTreesAreWellTyped = true;

        for (int k = 1; k <= k_max; k++) {
            AppTree tree = gen.genOne(k, goalStr);

            if (tree != null) {

                if (!tree.isStrictlyWellTyped(gamma)) {
                    ch.fail("Tree is not well-typed: "+tree);
                    allTreesAreWellTyped = false;
                }

                String resultStr = "";
                if (testEvaluation) {
                    Object result = lib.eval(tree);
                    resultStr = result + " \t = \t ";
                }

                Log.it("("+k+") \t "+ resultStr + tree);

            } else {
                Log.it("("+k+") \t N/A");
            }


        }

        Log.it();
        ch.is(allTreesAreWellTyped, "All trees are well-typed.");
        Log.it("----------------------------------------------");
        Log.it();
    }

}