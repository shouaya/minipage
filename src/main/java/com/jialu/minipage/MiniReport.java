package com.jialu.minipage;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * TODO add YUIcompressor
 * @author EB060
 *
 */
class MiniReport implements ErrorReporter {

    private String format(String arg0, String arg1, int arg2, String arg3, int arg4) {
        return String.format("%s%s at line %d, column %d:\n%s",
            arg0,
            arg1 == null ? "" : ":" + arg1,
            arg2,
            arg4,
            arg3);
    }

    @Override
    public void warning(String arg0, String arg1, int arg2, String arg3, int arg4) {
        System.out.println("WARNING: " + format(arg0, arg1, arg2, arg3, arg4));
    }

    @Override
    public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
        System.out.println("ERROR: " + format(arg0, arg1, arg2, arg3, arg4));
    }

    @Override
    public EvaluatorException runtimeError(String arg0, String arg1, int arg2, String arg3, int arg4) {
        System.out.println("RUNTIME ERROR: " + format(arg0, arg1, arg2, arg3, arg4));
        return new EvaluatorException(arg0);
    }
}