/*******************************************************************************
 * Copyright (c) 2010, 2015 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.papyrus.internal.uml.diagram.sequence.elk;

import java.io.Serializable;

/**
 * An interface for data types, which should be serializable using {@link #toString()} and
 * parsable using {@link #parse(String)}. The default constructor must always be
 * accessible and create an instance with default content.
 *
 * @kieler.design 2011-02-01 reviewed by cmot, soh
 * @kieler.rating proposed yellow 2012-11-02 cds
 * @author msp
 */
public interface IDataObject extends Serializable {

    /**
     * Parse the given string and set the content of this data object.
     *
     * @param string a string
     * @throws IllegalArgumentException if the string does not have the expected format
     */
    void parse(String string);

}
