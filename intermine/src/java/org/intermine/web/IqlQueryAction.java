package org.intermine.web;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;

import org.intermine.metadata.Model;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.query.iql.IqlQuery;
import org.intermine.objectstore.query.Query;

/**
 * Implementation of <strong>Action</strong> that runs a Query
 *
 * @author Andrew Varley
 */

public class IqlQueryAction extends LookupDispatchAction
{

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward run(ActionMapping mapping,
                             ActionForm form,
                             HttpServletRequest request,
                             HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        ObjectStore os = (ObjectStore) servletContext.getAttribute(Constants.OBJECTSTORE);
        Model model = (Model) os.getModel();

        IqlQueryForm queryform = (IqlQueryForm) form;

        try {
            Query q = new IqlQuery(queryform.getQuerystring(), model.getPackageName()).toQuery();
            session.setAttribute(Constants.QUERY, q);

            return mapping.findForward("runquery");
        } catch (java.lang.IllegalArgumentException e) {
            ActionErrors errors = new ActionErrors();
            ActionError error = new ActionError("errors.iqlquery.illegalargument", e.getMessage());
            errors.add("iqlquery", error);
            saveErrors(request, errors);
            return mapping.findForward("buildiqlquery");
        }
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     *
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward view(ActionMapping mapping,
                              ActionForm form,
                              HttpServletRequest request,
                              HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        ObjectStore os = (ObjectStore) servletContext.getAttribute(Constants.OBJECTSTORE);
        Model model = (Model) os.getModel();
        
        IqlQueryForm queryform = (IqlQueryForm) form;

        String queryString = queryform.getQuerystring();

        try {
            if (queryString == null || queryString.length() == 0) {
                session.setAttribute(Constants.QUERY, null);
            } else {
                Query q = new IqlQuery(queryString, model.getPackageName()).toQuery();
                session.setAttribute(Constants.QUERY, q);
            }

            return mapping.findForward("buildquery");
        } catch (java.lang.IllegalArgumentException e) {
            ActionErrors errors = new ActionErrors();
            ActionError error = new ActionError("errors.iqlquery.illegalargument", e.getMessage());
            errors.add("iqlquery", error);
            saveErrors(request, errors);
            return mapping.findForward("buildiqlquery");
        }
    }

    /**
     * Distributes the actions to the necessary methods, by providing a Map from action to
     * the name of a method.
     *
     * @return a Map
     */
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("button.run", "run");
        map.put("button.view", "view");
        return map;
    }
}
