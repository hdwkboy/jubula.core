/*******************************************************************************
 * Copyright (c) 2004, 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.client.analyze.impl.standard;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
/**
 * 
 * @author volker
 *
 */
public class Activator implements BundleActivator {
/**
 * 
 */
    private static BundleContext context;
/**
 * 
 * @return context
 */
    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    /**
     * @param bundleContext bundleContext
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    /**
     * @param bundleContext bundleContext
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
