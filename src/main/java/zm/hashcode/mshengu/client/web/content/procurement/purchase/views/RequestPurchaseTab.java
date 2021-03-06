/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zm.hashcode.mshengu.client.web.content.procurement.purchase.views;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import zm.hashcode.mshengu.app.facade.fleet.TruckFacade;
import zm.hashcode.mshengu.app.facade.people.PersonFacade;
import zm.hashcode.mshengu.app.facade.procurement.RequestFacade;
import zm.hashcode.mshengu.app.facade.procurement.RequestPurchaseItemFacade;
import zm.hashcode.mshengu.app.facade.serviceproviders.ServiceProviderFacade;
import zm.hashcode.mshengu.app.facade.serviceproviders.ServiceProviderProductFacade;
import zm.hashcode.mshengu.app.facade.ui.util.CostCentreCategoryTypeFacade;
import zm.hashcode.mshengu.app.facade.ui.util.CostCentreTypeFacade;
import zm.hashcode.mshengu.app.facade.ui.util.ItemCategoryTypeFacade;
import zm.hashcode.mshengu.app.util.SequenceHelper;
import zm.hashcode.mshengu.app.util.validation.OnSubmitValidationHelper;
import zm.hashcode.mshengu.app.util.validation.UIValidatorHelper;
import zm.hashcode.mshengu.client.web.MshenguMain;
import zm.hashcode.mshengu.client.web.content.procurement.purchase.PurchaseMenu;
import zm.hashcode.mshengu.client.web.content.procurement.purchase.form.RequestForm;
import zm.hashcode.mshengu.client.web.content.procurement.purchase.models.RequestBean;
import zm.hashcode.mshengu.client.web.content.procurement.purchase.table.PurchaseItemsTable;
import zm.hashcode.mshengu.domain.fleet.Truck;
import zm.hashcode.mshengu.domain.people.ContactPerson;
import zm.hashcode.mshengu.domain.people.Person;
import zm.hashcode.mshengu.domain.procurement.Request;
import zm.hashcode.mshengu.domain.procurement.RequestPurchaseItem;
import zm.hashcode.mshengu.domain.serviceprovider.ServiceProvider;
import zm.hashcode.mshengu.domain.serviceprovider.ServiceProviderProduct;
import zm.hashcode.mshengu.domain.ui.util.CostCentreCategoryType;
import zm.hashcode.mshengu.domain.ui.util.CostCentreType;
import zm.hashcode.mshengu.domain.ui.util.ItemCategoryType;

/**
 *
 * @author Luckbliss
 */
public class RequestPurchaseTab extends VerticalLayout implements
        Button.ClickListener, Property.ValueChangeListener {

    private RequestForm form;
    private final MshenguMain main;
    private PurchaseItemsTable table;
    private String productId;
    private String keep = null;
    private BigDecimal total = BigDecimal.ZERO;
    private DecimalFormat f = new DecimalFormat("###.00");
    private SequenceHelper sequenceHelper = new SequenceHelper();
    private ServiceProvider serviceProvider;

    public RequestPurchaseTab(MshenguMain app) {
        setSizeFull();
        main = app;
        form = new RequestForm(app);
        table = new PurchaseItemsTable(main);
        addComponent(form);
        addComponent(table);
        addListeners();
    }

    private void addListeners() {
        //Register Button Listeners
        form.save.addClickListener((Button.ClickListener) this);
        form.approval.addClickListener((Button.ClickListener) this);
        //form.editItemsButton.addClickListener((Button.ClickListener) this);
        form.name.addValueChangeListener((Property.ValueChangeListener) this);
        form.itemDescription.addValueChangeListener((Property.ValueChangeListener) this);
        form.quantity.addValueChangeListener((Property.ValueChangeListener) this);
        table.addValueChangeListener((Property.ValueChangeListener) this);
        form.costCentre.addValueChangeListener((Property.ValueChangeListener) this);
        form.costCategory.addValueChangeListener((Property.ValueChangeListener) this);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == form.save) {
            addItemsToTable(form.binder);
        } else if (source == form.approval) {
            sendRequest(form.binder);
            getHome();
        }

    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        final Property property = event.getProperty();
        if (property == form.name) {
            table.removeAllItems();
            form.itemDescription.removeAllItems();
            String supplierId = form.name.getValue().toString();
            serviceProvider = ServiceProviderFacade.getServiceProviderService().findById(supplierId);
            if (!serviceProvider.getServiceProviderProduct().isEmpty()) {
                setReadOnlyFalse();
                resetValues();
                if (serviceProvider.getContactPerson() != null) {
                    ContactPerson contactPerson = serviceProvider.getContactPerson();
                    form.address.setValue(contactPerson.getAddress1());
                    form.number.setValue(contactPerson.getMainNumber());
                    form.postalCode.setValue(contactPerson.getCode());
                }
                if (keep != null) {
                    form.description.setRequired(false);
                    form.itemPurchaseLayout.removeComponent(form.description);
                    form.itemDescription.setRequired(true);
                    form.itemPurchaseLayout.addComponent(form.itemDescription, 0, 4);

                }
                for (ServiceProviderProduct product : serviceProvider.getServiceProviderProduct()) {
                    form.itemDescription.addItem(product.getId());
                    form.itemDescription.setItemCaption(product.getId(), product.getProductName());
                    keep = null;
                }
                setReadOnlyTrue();
            } else {
                setReadOnlyFalse();
                resetValues();
                if (serviceProvider.getContactPerson() != null) {
                    ContactPerson contactPerson = serviceProvider.getContactPerson();
                    form.address.setValue(contactPerson.getAddress1());
                    form.number.setValue(contactPerson.getMainNumber());
                    form.postalCode.setValue(contactPerson.getCode());
                }
                form.address.setReadOnly(true);
                form.number.setReadOnly(true);
                if (keep == null) {
                    form.itemDescription.setRequired(false);
                    form.itemPurchaseLayout.removeComponent(form.itemDescription);
                    form.description.setRequired(true);
                    form.itemPurchaseLayout.addComponent(form.description, 0, 4);
                    keep = "keep";
                }
            }
        } else if (property == form.itemDescription) {
            if (form.itemDescription.getValue() != null) {
                setReadOnlyFalse();
                ServiceProviderProduct product = ServiceProviderProductFacade.getServiceProviderProductService().findById(form.itemDescription.getValue().toString());
                if (product.getItemNumber() != null) {
                    form.itemNumber.setValue(product.getItemNumber());
                }
                if (product.getUnit() != null) {
                    form.unit.setValue(product.getUnit());
                }
                if (product.getVolume() != null) {
                    DecimalFormat d = new DecimalFormat("#,###");
                    form.volume.setValue(d.format(Double.parseDouble(product.getVolume().toString())));
                }
                if (product.getPrice() != null) {
                    form.unitPrice.setValue(f.format(product.getPrice()));
                }
                productId = product.getId();
                setReadOnlyTrue();
            }
        } else if (property == form.quantity) {
            if (!form.quantity.getValue().toString().isEmpty() && !form.itemNumber.getValue().toString().isEmpty() && !form.unitPrice.getValue().toString().equals("0")) {
                setReadOnlyFalse();
                BigDecimal subtotal = new BigDecimal(form.unitPrice.getValue());
                subtotal = subtotal.multiply(new BigDecimal(form.quantity.getValue()));
                form.subTotal.setValue(f.format(subtotal));
                if (!serviceProvider.isRegisteredForVat()) {
                    subtotal = subtotal.multiply(new BigDecimal(1.14));
                }
                form.total.setValue(f.format(subtotal));
                //form.editItemsButton.setVisible(true); 
                setReadOnlyTrue();
            }
        } else if (property == form.costCentre) {
            if (form.costCentre.getValue() != null) {
                CostCentreType centreType = CostCentreTypeFacade.getCostCentreTypeService().findById(form.costCentre.getValue().toString());
                addItemsToCostCentreCategoryCombobox(centreType);
            }
        } else if (property == form.costCategory) {
            if (form.costCategory.getValue() != null) {
                CostCentreType centreType = CostCentreTypeFacade.getCostCentreTypeService().findById(form.costCentre.getValue().toString());
                if ("fleet maintenance".equalsIgnoreCase(centreType.getName())) {
                    if (centreType.getCategoryTypes() != null) {
                        for (CostCentreCategoryType type : centreType.getCategoryTypes()) {
                            if (type.getName().equalsIgnoreCase("vehicles")) {
                                addItemsToCostCentreCategoryItemCombobox(type);
                            }
                        }
                    }

                } else {
                    CostCentreCategoryType categoryType = CostCentreCategoryTypeFacade.getCostCentreCategoryTypeService().findById(form.costCategory.getValue().toString());
                    addItemsToCostCentreCategoryItemCombobox(categoryType);
                }
            }
        }
    }

    private void addItemsToCostCentreCategoryCombobox(CostCentreType centreType) {
        form.itemCategory.removeAllItems();
        form.costCategory.removeAllItems();
        if ("fleet maintenance".equalsIgnoreCase(centreType.getName())){ //validate category if cost centre is             
            form.costCategory.setRequired(true);
            form.costCategory.setRequiredError("Cost Category Type is required for Fleet Maintenance.");      
        }else{
            form.costCategory.setRequired(false);
            form.costCategory.removeStyleName("invalid");
        }
        if (centreType.getCategoryTypes() != null) {
            if ("fleet maintenance".equalsIgnoreCase(centreType.getName())) {
                List<Truck> truckList = TruckFacade.getTruckService().findAll();
                for (Truck truck : truckList) {
                    String truckName = truck.getVehicleNumber() + " - (" + truck.getNumberPlate() + ")";
                    form.costCategory.addItem(truck.getId());
                    form.costCategory.setItemCaption(truck.getId(), truckName);
                }
            }
            for (CostCentreCategoryType categoryType : centreType.getCategoryTypes()) {
                form.costCategory.addItem(categoryType.getId());
                form.costCategory.setItemCaption(categoryType.getId(), categoryType.getName());
            }
        }
    }

    private void addItemsToCostCentreCategoryItemCombobox(CostCentreCategoryType categoryType) {
        form.itemCategory.removeAllItems();
        if (categoryType.getItemCategoryTypes() != null && form.costCentre.getValue() != null) {
            for (ItemCategoryType type : categoryType.getItemCategoryTypes()) {
                form.itemCategory.addItem(type.getId());
                form.itemCategory.setItemCaption(type.getId(), type.getName());
            }
        }
    }

    private void addItemsToTable(FieldGroup binder) {
        try {
            binder.commit();
            RequestPurchaseItem requestPurchaseItem = getEntity(binder);
            RequestPurchaseItemFacade.getRequestPurchaseItemService().persist(requestPurchaseItem);
            table.loadTable(requestPurchaseItem, form);
            if (requestPurchaseItem.getItemDescription() != null) {
                form.itemNumber.setReadOnly(false);
                form.unit.setReadOnly(false);
                form.volume.setReadOnly(false);
                form.unitPrice.setReadOnly(false);
                form.subTotal.setReadOnly(false);
                form.total.setReadOnly(false);
                resetValues();
                form.subTotal.setReadOnly(true);
                form.total.setReadOnly(true);
            } else {
                setReadOnlyFalse();
                resetValues();
                setReadOnlyTrue();
            }

            form.approval.setVisible(true);
            //form.editItemsButton.setVisible(false);
            Notification.show("Record ADDED!", Notification.Type.TRAY_NOTIFICATION);
        } catch (FieldGroup.CommitException e) {
            Collection<Field<?>> fields = binder.getFields();
            OnSubmitValidationHelper helper = new OnSubmitValidationHelper(fields, form.errorMessage);
            helper.doValidation();
            Notification.show("Please Correct Red Colored Inputs!", Notification.Type.TRAY_NOTIFICATION);
        } catch (Exception e) {
            Notification.show("Values MISSING .. !", Notification.Type.TRAY_NOTIFICATION);
        }
    }

    private void getHome() {
        main.content.setSecondComponent(new PurchaseMenu(main, "LANDING"));
    }

    private RequestPurchaseItem getEntity(FieldGroup binder) {
        RequestPurchaseItem requestPurchaseItem = null;
        RequestBean bean = ((BeanItem<RequestBean>) binder.getItemDataSource()).getBean();
        if (productId != null) {
            ServiceProviderProduct product = ServiceProviderProductFacade.getServiceProviderProductService().findById(productId);
            requestPurchaseItem = new RequestPurchaseItem.Builder(removeSpecialCharacters(bean.getQuantity() + ""))
                    .product(product)
                    .subTotal(bean.getTotal())
                    .build();
        } else {
            requestPurchaseItem = new RequestPurchaseItem.Builder(removeSpecialCharacters(bean.getQuantity() + ""))
                    .itemDescription(removeSpecialCharacters(bean.getDescription()))
                    .itemNumber(removeSpecialCharacters(bean.getItemNumber()))
                    .unit(removeSpecialCharacters(bean.getUnit()))
                    .unitPrice(bean.getUnitPrice())
                    .volume(removeSpecialCharacters(bean.getVolume()))
                    .subTotal(bean.getTotal())
                    .build();
        }
        return requestPurchaseItem;
    }

    private void resetValues() {
        form.description.setValue("");
        form.itemNumber.setValue("");
        form.quantity.setValue("");
        form.unit.setValue("");
        form.volume.setValue("");
        form.unitPrice.setValue("");
        form.subTotal.setValue("");
        form.total.setValue("");
    }

    private void setReadOnlyFalse() {
        form.ordernumber.setReadOnly(false);
        form.address.setReadOnly(false);
        form.postalCode.setReadOnly(false);
        form.number.setReadOnly(false);
        form.itemNumber.setReadOnly(false);
        form.unit.setReadOnly(false);
        form.volume.setReadOnly(false);
        form.unitPrice.setReadOnly(false);
        form.subTotal.setReadOnly(false);
        form.total.setReadOnly(false);
    }

    private void setReadOnlyTrue() {
        form.ordernumber.setReadOnly(true);
        form.address.setReadOnly(true);
        form.postalCode.setReadOnly(true);
        form.number.setReadOnly(true);
        form.itemNumber.setReadOnly(true);
        form.unit.setReadOnly(true);
        form.volume.setReadOnly(true);
        form.unitPrice.setReadOnly(true);
        form.subTotal.setReadOnly(true);
        form.total.setReadOnly(true);
    }

    private void sendRequest(FieldGroup binder) {

        Request request = getRequestEntity(binder);
        RequestFacade.getRequestService().persist(request);
        Notification.show("Record ADDED!", Notification.Type.TRAY_NOTIFICATION);
    }

    private Request getRequestEntity(FieldGroup binder) {
        RequestBean bean = ((BeanItem<RequestBean>) binder.getItemDataSource()).getBean();
        Set<RequestPurchaseItem> items = new HashSet<>();
        for (Object id : table.getItemIds()) {
            RequestPurchaseItem item = RequestPurchaseItemFacade.getRequestPurchaseItemService().findById(id.toString());
            items.add(item);
            total = total.add(item.getSubTotal());
        }
        ServiceProvider provider = ServiceProviderFacade.getServiceProviderService().findById(bean.getCompanyName());
        Person person = PersonFacade.getPersonService().findById(bean.getRequestingPerson());
        DecimalFormat f = new DecimalFormat("### ###.00");
        CostCentreType costCentreType = null;
        ItemCategoryType itemCategoryType = null;
        CostCentreCategoryType costCentreCategoryType = null;
        if (bean.getCostCentre() != null) {
            costCentreType = CostCentreTypeFacade.getCostCentreTypeService().findById(bean.getCostCentre());
        }

        if (bean.getItemCategory() != null) {
            itemCategoryType = ItemCategoryTypeFacade.getItemCategoryTypeService().findById(bean.getItemCategory());
        }
        if (bean.getCostCategory() != null) {
            costCentreCategoryType = CostCentreCategoryTypeFacade.getCostCentreCategoryTypeService().findById(bean.getCostCategory());
        }

        CostCentreType centreType = CostCentreTypeFacade.getCostCentreTypeService().findById(form.costCentre.getValue().toString());
        if ("fleet maintenance".equalsIgnoreCase(centreType.getName()) && bean.getItemCategory() != null) {
                Request request = new Request.Builder(person)
                        .approvalStatus(false)
                        .items(items)
                        .serviceProvider(provider)
                        .truck(TruckFacade.getTruckService().findById(bean.getCostCategory()))
                        .costCentreType(costCentreType)
                        .itemCategoryType(itemCategoryType)
                        .deliveryInstructions(removeSpecialCharacters(bean.getDeliveryInstructions()))
                        .orderDate(new Date())
                        .deliveryDate(bean.getOrderDate())
                        .serviceProviderSupplierId(provider.getId())
                        .truckId(TruckFacade.getTruckService().findById(bean.getCostCategory()).getId())
                        .total(total)
                        .build();
                return request;
        } else if ("fleet maintenance".equalsIgnoreCase(centreType.getName())) {
            Request request = new Request.Builder(person)
                    .approvalStatus(false)
                    .items(items)
                    .serviceProvider(provider)
                    .truck(TruckFacade.getTruckService().findById(bean.getCostCategory()))
                    .costCentreType(costCentreType)
                    .itemCategoryType(itemCategoryType)
                    .deliveryInstructions(removeSpecialCharacters(bean.getDeliveryInstructions()))
                    .orderDate(new Date())
                    .deliveryDate(bean.getOrderDate())
                    .serviceProviderSupplierId(provider.getId())
                    .truckId(TruckFacade.getTruckService().findById(bean.getCostCategory()).getId())
                    .total(total)
                    .build();
            return request;
        } else if (bean.getItemCategory() != null) {
            Request request = new Request.Builder(person)
                    .approvalStatus(false)
                    .items(items)
                    .serviceProvider(provider)
                    .categoryType(costCentreCategoryType)
                    .costCentreType(costCentreType)
                    .itemCategoryType(itemCategoryType)
                    .deliveryInstructions(removeSpecialCharacters(bean.getDeliveryInstructions()))
                    .orderDate(new Date())
                    .deliveryDate(bean.getOrderDate())
                    .serviceProviderSupplierId(provider.getId())
                    .total(total)
                    .build();
            return request;
        } else {
            Request request = new Request.Builder(person)
                    .approvalStatus(false)
                    .items(items)
                    .serviceProvider(provider)
                    .categoryType(costCentreCategoryType)
                    .costCentreType(costCentreType)
                    .itemCategoryType(itemCategoryType)
                    .deliveryInstructions(removeSpecialCharacters(bean.getDeliveryInstructions()))
                    .orderDate(new Date())
                    .deliveryDate(bean.getOrderDate())
                    .serviceProviderSupplierId(provider.getId())
                    .total(total)
                    .build();
            return request;
        }
    }
    
    private String removeSpecialCharacters(String remove) {
        return remove.replaceAll("[^\\w\\s\\-_]", "");
    }
}
