package org.intermine.web.logic.export;

/*
 * Copyright (C) 2002-2008 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
import java.util.List;

import org.intermine.web.logic.results.ResultElement;


/**
 * Simple exporter interface. Objects implementing this interface are 
 * able to make export.
 * @author Jakub Kulaviak
 **/
public interface Exporter
{

    /**
     * Do export.
     * @param results to be exported
     */
    public void export(List<List<ResultElement>> results);
    
}
