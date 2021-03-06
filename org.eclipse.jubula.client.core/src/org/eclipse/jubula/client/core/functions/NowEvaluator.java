/**
 * 
 */
package org.eclipse.jubula.client.core.functions;

import java.util.Date;

/**
 * @author al
 *
 */
public final class NowEvaluator implements IFunctionEvaluator {

    /**
     * 
     * {@inheritDoc}
     */
    public String evaluate(String[] arguments) {
        Date now = new Date();
        return String.valueOf(now.getTime());
    }

}
