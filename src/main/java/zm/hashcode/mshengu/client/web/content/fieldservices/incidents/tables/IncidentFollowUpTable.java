/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zm.hashcode.mshengu.client.web.content.fieldservices.incidents.tables;

import com.vaadin.ui.Embedded;
import com.vaadin.ui.Table;
import zm.hashcode.mshengu.app.facade.incident.IncidentFacade;
import zm.hashcode.mshengu.app.util.DateTimeFormatHelper;
import zm.hashcode.mshengu.app.util.UITableIconHelper;
import zm.hashcode.mshengu.client.web.MshenguMain;
import zm.hashcode.mshengu.domain.incident.Incident;
import zm.hashcode.mshengu.domain.incident.UserAction;

/**
 *
 * @author given
 */
public class IncidentFollowUpTable extends Table {

    private final MshenguMain main;
    private DateTimeFormatHelper formatHelper = new DateTimeFormatHelper();
    private UITableIconHelper iconHelper = new UITableIconHelper();
    private final String id;

    public IncidentFollowUpTable(MshenguMain app, final String id) {
        this.main = app;
        this.id = id;


        addContainerProperty("Updated On", String.class, null);
        addContainerProperty("Quality Assurance Date", String.class, null);
        addContainerProperty("Last Action Date", String.class, null);
        addContainerProperty("Comment", String.class, null);
        addContainerProperty("Responder", String.class, null);
        addContainerProperty("Status ", String.class, null);
        addContainerProperty("Active ", Embedded.class, null);


        // Allow selecting items from the table.
        setNullSelectionAllowed(false);

        setSelectable(true);
        // Send changes in selection immediately to server.
        setImmediate(true);
        setSizeFull();

        loadUserActions(id);;



    }

    public final void loadUserActions(String incidentId) {
        // Add Data Columns
        Incident incident = IncidentFacade.getIncidentService().findById(incidentId);
        removeAllItems();

        boolean active = true;
        if (incident.getUserAction() != null) {
            for (UserAction userAction : incident.getUserAction()) {
//                JOptionPane.showMessageDialog(null, incident.getRefNumber());
                addItem(new Object[]{
                            //                userAction.getStatus(),
                            formatHelper.getDayMonthYear(userAction.getActionDate()),
                            formatHelper.getDayMonthYear(userAction.getQualityAssuranceDate()),
                            formatHelper.getDayMonthYear(userAction.getResolvedDate()),
                            userAction.getComment(),
                            userAction.getStaffName(),
                            userAction.getUserActionStatusName(), //                incident.getComment()
                            iconHelper.getCheckOrBlank(active),
                        }, userAction.getId());
                active = false;
            }
        }
    }
}
