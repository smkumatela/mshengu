/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zm.hashcode.mshengu.client.web.content.fieldservices.servicesperformed.views;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.springframework.util.StringUtils;
import zm.hashcode.mshengu.app.facade.customer.CustomerFacade;
import zm.hashcode.mshengu.app.facade.products.SiteFacade;
import zm.hashcode.mshengu.app.facade.products.SiteServiceContractLifeCycleFacade;
import zm.hashcode.mshengu.app.facade.ui.location.AddressFacade;
import zm.hashcode.mshengu.app.facade.ui.location.LocationFacade;
import zm.hashcode.mshengu.client.web.MshenguMain;
import zm.hashcode.mshengu.client.web.content.fieldservices.servicesperformed.ServicePerformedMenu;
import zm.hashcode.mshengu.client.web.content.fieldservices.servicesperformed.tables.SiteServiceLogExceptionTable;
import zm.hashcode.mshengu.client.web.content.fieldservices.site.forms.SiteDetailsForm;
import zm.hashcode.mshengu.client.web.content.fieldservices.site.models.SiteDetailsBean;
import zm.hashcode.mshengu.domain.customer.Customer;
import zm.hashcode.mshengu.domain.products.Site;
import zm.hashcode.mshengu.domain.products.SiteServiceContractLifeCycle;
import zm.hashcode.mshengu.domain.products.SiteServiceLog;
import zm.hashcode.mshengu.domain.products.SiteUnit;
import zm.hashcode.mshengu.domain.ui.location.Address;
import zm.hashcode.mshengu.domain.ui.location.Location;

/**
 *
 * @author Ferox
 */
public class SiteSiteServiceExceptionReportTab extends VerticalLayout implements
        Button.ClickListener, Property.ValueChangeListener {

    private final MshenguMain main;
    private final SiteDetailsForm form;
    private final SiteServiceLogExceptionTable table;
    private String parentId = null;

    public SiteSiteServiceExceptionReportTab(MshenguMain app) {
        main = app;
        form = new SiteDetailsForm();
        table = new SiteServiceLogExceptionTable(main);
        setCaption("Site Units");
        setSizeFull();
//        addComponent(form);
        addComponent(table);
        addListeners();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == form.save) {
            saveForm(form.binder);
        } else if (source == form.edit) {
            setEditFormProperties();
        } else if (source == form.cancel) {
            getHome();
        } else if (source == form.update) {
            saveEditedForm(form.binder);
        } else if (source == form.delete) {
            deleteForm(form.binder);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        final Property property = event.getProperty();
        if (property == table) {
            final Site site = SiteFacade.getSiteService().findById(table.getValue().toString());
            form.binder.setItemDataSource(new BeanItem<>(getBean(site)));
            setReadFormProperties();
//        } else if (property == selectedCustomer.comboBoxSelectCustomer) {
//            String custId = getCustomerId();
//            if (selectedCustomer.comboBoxSelectCustomer.getValue().toString() != null) {
//
//                Notification.show("Customer  Selected!", Notification.Type.TRAY_NOTIFICATION);
//                table.removeValueChangeListener((Property.ValueChangeListener) this);
//                Customer customer  = CustomerFacade.getCustomerService().findById(selectedCustomer.comboBoxSelectCustomer.getValue().toString());
//                table.loadCustomerSites(customer.getSites());
//                table.addValueChangeListener((Property.ValueChangeListener) this);
//            }
        }
    }

    private void saveForm(FieldGroup binder) {
        try {
            if (!StringUtils.isEmpty(getParentId())) {
                binder.commit();
                Site site = getNewEntity(binder);
                SiteFacade.getSiteService().persist(site);
                updateCustomerSites(site, "add");
                getHome();
                Notification.show("Record ADDED!", Notification.Type.TRAY_NOTIFICATION);
            } else {

                Notification.show("Please select a customer 1!", Notification.Type.TRAY_NOTIFICATION);
            }

        } catch (FieldGroup.CommitException e) {
            Notification.show("Values MISSING!", Notification.Type.TRAY_NOTIFICATION);
            getHome();
        }
    }

    private void saveEditedForm(FieldGroup binder) {
        try {
            if (!StringUtils.isEmpty(getParentId())) {

                binder.commit();
                SiteFacade.getSiteService().merge(getUpdateEntity(binder));
                getHome();
                Notification.show("Record UPDATED!", Notification.Type.TRAY_NOTIFICATION);
            } else {

                Notification.show("Please select a customer!", Notification.Type.TRAY_NOTIFICATION);
            }
        } catch (FieldGroup.CommitException e) {
            Notification.show("Values MISSING!", Notification.Type.TRAY_NOTIFICATION);
            getHome();
        }
    }

    private void deleteForm(FieldGroup binder) {
        Notification.show("Deliting Sites is not Allowed!", Notification.Type.TRAY_NOTIFICATION);
        getHome();
        // SiteFacade.getSiteService().delete(getNewEntity(binder));
        getHome();
    }

    private void getHome() {
        main.content.setSecondComponent(new ServicePerformedMenu(main, "LANDING"));
        loadTable();
    }

    private void setEditFormProperties() {
        form.binder.setReadOnly(false);
        form.save.setVisible(false);
        form.edit.setVisible(false);
        form.cancel.setVisible(true);
        form.delete.setVisible(false);
        form.update.setVisible(true);
    }

    private void setReadFormProperties() {
        form.binder.setReadOnly(true);
        //Buttons Behaviour
        form.save.setVisible(false);
        form.edit.setVisible(true);
        form.cancel.setVisible(true);
        form.delete.setVisible(true);
        form.update.setVisible(false);

    }

    private void addListeners() {
        //Register Button Listeners
        form.save.addClickListener((Button.ClickListener) this);
        form.edit.addClickListener((Button.ClickListener) this);
        form.cancel.addClickListener((Button.ClickListener) this);
        form.update.addClickListener((Button.ClickListener) this);
        form.delete.addClickListener((Button.ClickListener) this);
        //Register Table Listerners
        table.addValueChangeListener((Property.ValueChangeListener) this);
//        selectedCustomer.comboBoxSelectCustomer.addValueChangeListener((Property.ValueChangeListener) this);
    }

    /**
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
        loadTable();

    }

    private Site getNewEntity(FieldGroup binder) {

        Set<SiteServiceContractLifeCycle> serviceContractLifeCyclesList = new HashSet<>();
        final SiteDetailsBean siteBean = ((BeanItem<SiteDetailsBean>) binder.getItemDataSource()).getBean();

//        final Address address = saveAddress(siteBean, null); //
//        final ContactPerson contactPerson = saveContactPerson("", "");
//        final ServiceProvider serviceProvider = saveServiceProvider("");
        Set<SiteServiceLog> siteServiceLog = new HashSet<>();

        Location location = LocationFacade.getLocationService().findById(siteBean.getLocationId());
        serviceContractLifeCyclesList.addAll(saveSiteServiceContractLifeCycle(siteBean)); //Save the site service contract life cycle

        final Site site = new Site.Builder(siteBean.getName())
//                .address(address)
                .location(location)
//                .contactPerson(contactPerson)
//                .serviceProvider(serviceProvider)
                .status("")
                .siteServiceLog(siteServiceLog)
                .siteServiceContractLifeCycle(serviceContractLifeCyclesList)
                .build();

        return site;

    }

    private Site getUpdateEntity(FieldGroup binder) {


        Set<SiteServiceContractLifeCycle> serviceContractLifeCyclesList = new HashSet<>();
        final SiteDetailsBean siteBean = ((BeanItem<SiteDetailsBean>) binder.getItemDataSource()).getBean();
        if (!StringUtils.isEmpty(siteBean.getId())) {
            Site siteToUpdate = SiteFacade.getSiteService().findById(siteBean.getId());

            if (!StringUtils.isEmpty(siteToUpdate)) {
                final Address address = saveAddress(siteBean, siteToUpdate.getAddressId()); //
                Location location = LocationFacade.getLocationService().findById(siteBean.getLocationId());
                serviceContractLifeCyclesList.addAll(saveSiteServiceContractLifeCycle(siteBean)); //Save the site service contract life cycle

                final Site site = new Site.Builder(siteBean.getName())
                        .site(siteToUpdate)
                        .address(address)
                        .location(location)
                        .siteServiceContractLifeCycle(serviceContractLifeCyclesList)
                        .build();

                return site;
            } else {

                Notification.show("Site Not Found!", Notification.Type.TRAY_NOTIFICATION);
                return null;
            }

        } else {

            Notification.show("Site Not Found!", Notification.Type.TRAY_NOTIFICATION);
            return null;
        }

    }

    private Address saveAddress(SiteDetailsBean siteBean, String addressId) {
        final Address address = new Address.Builder(siteBean.getStreetAddress())
                .postalCode(siteBean.getPostalCode())
                .id(addressId)
                .build();

        if (!StringUtils.isEmpty(addressId)) {
            AddressFacade.getAddressService().persist(address);
        } else {
            AddressFacade.getAddressService().merge(address);
        }

        return address;
    }



    private Set<SiteServiceContractLifeCycle> saveSiteServiceContractLifeCycle(SiteDetailsBean siteBean) {

        Set<SiteServiceContractLifeCycle> serviceContractLifeCyclesList = new HashSet<>();
        Set<SiteUnit> siteUnitsList = new HashSet<>();

        if (!StringUtils.isEmpty(siteBean.getId())) { //new site
            Site site = SiteFacade.getSiteService().findById(siteBean.getId());
            if (!StringUtils.isEmpty(site.getSiteServiceContractLifeCycle())) {
                serviceContractLifeCyclesList.addAll(site.getSiteServiceContractLifeCycle());
                siteUnitsList.addAll(getSiteUnits(siteBean.getId()));
            }
        }

        SiteServiceContractLifeCycle serviceContractLifeCycles = new SiteServiceContractLifeCycle.Builder(new Date())
                .expectedNumberOfUnits(siteBean.getExpectedNumberOfUnits())
                .numberOfUnits(siteUnitsList.size()) // the actual number of units in this site life cycle
                .monday(siteBean.isMonday())
                .tuesday(siteBean.isTuesday())
                .wednesday(siteBean.isWednesday())
                .thursday(siteBean.isThursday())
                .friday(siteBean.isFriday())
                .saturday(siteBean.isSaturday())
                .sunday(siteBean.isSunday())
                .siteUnit(siteUnitsList)
                .build();

        SiteServiceContractLifeCycleFacade.getSiteServiceContractService().persist(serviceContractLifeCycles);
        serviceContractLifeCyclesList.add(serviceContractLifeCycles);

        return serviceContractLifeCyclesList;
    }

    private Set<SiteUnit> getSiteUnits(String siteId) {
        SiteServiceContractLifeCycle serviceContractLifeCycles = SiteFacade.getSiteService().getSitetCurrentContract(siteId);
        Set<SiteUnit> siteUnitsList = new HashSet<>();

        if (!StringUtils.isEmpty(serviceContractLifeCycles.getSiteUnit())) {
            siteUnitsList.addAll(serviceContractLifeCycles.getSiteUnit());
        }

        return siteUnitsList;
    }

    private SiteDetailsBean getBean(Site site) {
        SiteDetailsBean bean = new SiteDetailsBean();
        SiteServiceContractLifeCycle contractLifeCycle = SiteFacade.getSiteService().getSitetCurrentContract(site.getId());

        bean.setName(site.getName());
        bean.setStreetAddress(site.getAddressStreetAddress());
        bean.setPostalCode(site.getAddressPostalCode());
        bean.setLocationId(site.getLocationId());
        bean.setId(site.getId());
        if (contractLifeCycle != null) {
            bean.setExpectedNumberOfUnits(contractLifeCycle.getExpectedNumberOfUnits());
            bean.setNumberOfUnits(contractLifeCycle.getNumberOfUnits()); // the actual number of units in this site life cycle
            bean.setMonday(contractLifeCycle.isMonday());
            bean.setTuesday(contractLifeCycle.isTuesday());
            bean.setWednesday(contractLifeCycle.isWednesday());
            bean.setThursday(contractLifeCycle.isThursday());
            bean.setFriday(contractLifeCycle.isFriday());
            bean.setSaturday(contractLifeCycle.isSaturday());
            bean.setSunday(contractLifeCycle.isSunday());
        }
        return bean;
    }

    private void updateCustomerSites(Site site, String action) {

        try {
            Set<Site> siteList = new HashSet<>();

            if (!StringUtils.isEmpty(getParentId())) {
                Customer originalCustomer = CustomerFacade.getCustomerService().findById(getParentId());
                if (!StringUtils.isEmpty(originalCustomer.getSites())) {
                    siteList.addAll(originalCustomer.getSites());
                }

                if (action.equalsIgnoreCase("Add")) {
                    siteList.add(site);
                } else if (action.equalsIgnoreCase("Remove")) {
                    siteList.remove(site);
                }


                Customer newCustomer = new Customer.Builder(originalCustomer.getName())
                        .customer(originalCustomer)
                        .sites(siteList)
                        .build();

                CustomerFacade.getCustomerService().merge(newCustomer);
            }
        } catch (Exception e) {
            Notification.show("Error assosiating site with customer!", Notification.Type.TRAY_NOTIFICATION);
            getHome();
        }
    }

    private void loadTable() {
        Customer customer = CustomerFacade.getCustomerService().findById(getParentId());

        table.removeValueChangeListener((Property.ValueChangeListener) this);
        table.removeAllItems();

        if (customer.getSites() != null) {
            //  table.loadCustomerSites(customer.getSites());
        }
        table.addValueChangeListener((Property.ValueChangeListener) this);
    }
    
        
    
     public void loadServiceLogDetails(String siteId, Date startDate, Date endDate){
         table.loadServiceLogDetails(siteId, startDate, endDate);
//        table
    }
}
