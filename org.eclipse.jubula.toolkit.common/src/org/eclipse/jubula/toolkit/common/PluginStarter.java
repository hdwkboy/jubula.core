/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.jubula.toolkit.common;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jubula.tools.constants.ToolkitConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class PluginStarter extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = ToolkitConstants.PLUGIN_ID;

    /** The shared instance */
    private static PluginStarter plugin;

    /**
     * The constructor
     */
    public PluginStarter() {
        plugin = this;
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     * {@inheritDoc}
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     * {@inheritDoc}
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static PluginStarter getDefault() {
        return plugin;
    }

   
    

    /**
     * Gets all Toolkit Plugins using extension point "toolkitsupport"
     * @return an Array of IExtension
     */
    public IExtension[] findToolkitPlugins() {
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(
            ToolkitConstants.EXT_POINT_ID);
        return extensionPoint.getExtensions();
    }
  
    
    
}
