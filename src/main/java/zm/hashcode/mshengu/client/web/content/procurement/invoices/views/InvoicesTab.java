/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zm.hashcode.mshengu.client.web.content.procurement.invoices.views;

import com.vaadin.data.Property;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import zm.hashcode.mshengu.app.facade.procurement.RequestFacade;
import zm.hashcode.mshengu.client.web.MshenguMain;
import zm.hashcode.mshengu.client.web.content.procurement.invoices.form.InvoicesForm;
import zm.hashcode.mshengu.client.web.content.procurement.invoices.table.InvoiceTable;
import zm.hashcode.mshengu.domain.procurement.Request;

/**
 *
 * @author Luckbliss
 */
public class InvoicesTab extends VerticalLayout implements Property.ValueChangeListener {

    private InvoicesForm form;
    private MshenguMain main;
    private InvoiceTable table;

    public InvoicesTab(MshenguMain main) {
        setSizeFull();
        form = new InvoicesForm();
        table = new InvoiceTable(this, main);
        this.main = main;
        addComponent(form);
        addComponent(table);
        addListeners();
        getGrandTotal();
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        final Property property = event.getProperty();
        if (property == form.supplier) {
            getValues();
        } else if (property == form.month) {
            getValues();
        } else if (property == form.year) {
            getValues();
        }
    }

    private void getValues() {
        if (form.supplier.getValue() != null) {
            String supplierId = form.supplier.getValue().toString();
            table.removeAllItems();
            List<Request> requests = null;
            if (supplierId.equalsIgnoreCase("all")) {
                requests = RequestFacade.getRequestService().getProcessedRequestsWithInvoiceNumber();
            } else {
                requests = RequestFacade.getRequestService().getServiceProviderProcessedRequestsWithInvoiceNumber(supplierId);
            }
            table.loadTable(requests);
            getGrandTotal();
        } else {
            Notification.show("Enter all values", Notification.Type.TRAY_NOTIFICATION);

        }
    }

    private void addListeners() {
        //Register Button Listeners
        form.supplier.addValueChangeListener((Property.ValueChangeListener) this);
        form.month.addValueChangeListener((Property.ValueChangeListener) this);
        form.year.addValueChangeListener((Property.ValueChangeListener) this);
    }

    public final void getGrandTotal() {
        form.mtdTotal.setValue(form.total + table.getGrandTotal());
    }
}
