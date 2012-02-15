/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.jubula;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>UI Category</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.jubula.UICategory#getUiComponents <em>Ui Components</em>}</li>
 *   <li>{@link org.eclipse.jubula.UICategory#getCategories <em>Categories</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jubula.JubulaPackage#getUICategory()
 * @model
 * @generated
 */
public interface UICategory extends NamedElement {
    /**
     * Returns the value of the '<em><b>Ui Components</b></em>' containment reference list.
     * The list contents are of type {@link org.eclipse.jubula.UIComponent}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Ui Components</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Ui Components</em>' containment reference list.
     * @see org.eclipse.jubula.JubulaPackage#getUICategory_UiComponents()
     * @model containment="true"
     * @generated
     */
    EList<UIComponent> getUiComponents();

    /**
     * Returns the value of the '<em><b>Categories</b></em>' containment reference list.
     * The list contents are of type {@link org.eclipse.jubula.UICategory}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Categories</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Categories</em>' containment reference list.
     * @see org.eclipse.jubula.JubulaPackage#getUICategory_Categories()
     * @model containment="true"
     * @generated
     */
    EList<UICategory> getCategories();

} // UICategory
