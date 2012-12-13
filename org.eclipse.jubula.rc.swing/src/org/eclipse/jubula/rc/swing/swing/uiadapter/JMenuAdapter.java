/*******************************************************************************
 * Copyright (c) 2012 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation 
 *******************************************************************************/
package org.eclipse.jubula.rc.swing.swing.uiadapter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.eclipse.jubula.rc.common.uiadapter.interfaces.IMenuAdapter;
import org.eclipse.jubula.rc.common.uiadapter.interfaces.IMenuItemAdapter;
/**
 * Implementation of the menu interface for adapting the <code>JMenu</code>.
 * In Swing we have three implementations of the menu interface because
 * the Interface is used for the <code>JMenubar</code>, the <code>JPopupMenu</code> and the <code>JMenu</code>.
 * All these behave same in the implementation.
 * 
 * @author BREDEX GmbH
 *
 */
public class JMenuAdapter extends AbstractComponentAdapter
    implements IMenuAdapter {
    /** The JMenu from the AUT */
    private JMenu m_menu;
    
    /**
     * Creates an object with the adapted JMenu.
     * @param toAdapt graphics component which will be adapted
     */
    public JMenuAdapter(Object toAdapt) { 
        m_menu = (JMenu) toAdapt;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getRealComponent() {
        return m_menu;
    }
    
    /**
     * {@inheritDoc}
     */
    public IMenuItemAdapter[] getItems() {
        List adapters = new LinkedList();
        
        for (int i = 0; i < m_menu.getItemCount(); i++) {
            JMenuItem getted = m_menu.getItem(i);
            if (getted instanceof JMenuItem) {
                adapters.add(new JMenuItemAdapter(getted));
            }
            
        }
        

        IMenuItemAdapter[] allitems = null;
        if (adapters.size() > 0) {
            allitems = new IMenuItemAdapter[adapters.size()];
            int i = 0;
            for (Iterator iterator = adapters.iterator(); iterator.hasNext();) {
                Object object = (Object) iterator.next();
                
                allitems[i] = (IMenuItemAdapter) object;
                i++;
            }

        }
        
        
        return allitems;
    }
    /**
     * {@inheritDoc}
     */
    public int getItemCount() {
        return m_menu.getItemCount();
    }
    


}
