package com.example.application.views.mittaus;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.example.application.data.entity.Henkilo;
import com.example.application.data.entity.Mittaus;
import com.example.application.data.service.HenkiloRepository;
import com.example.application.data.service.MittausRepository;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.router.PageTitle;
import com.example.application.views.MainLayout;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;

@PageTitle("Mittaus")
@Route(value = "master-detail1/:mittausID?/:action?(edit)", layout = MainLayout.class)
public class MittausView extends Div implements BeforeEnterObserver {

    private final String MITTAUS_ID = "mittausID";
    private final String MITTAUS_EDIT_ROUTE_TEMPLATE = "master-detail1/%d/edit";

    private Grid<Mittaus> grid = new Grid<>(Mittaus.class, false);

    private TextField id;
    private TextField toimenpide;
    private TextField yksikko;
    private TextField tulos;
    private TextField toimenpideHaku;
    private DateTimePicker alkuPvmHaku;
    private DateTimePicker loppuPvmHaku;
    private DateTimePicker pvm;
    @PropertyId("henkilo")
    private Select<Henkilo> henkilo;
    private Select<Henkilo> henkiloHaku;
    
    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");
    private Button delete = new Button("Delete");
    private Button search = new Button("Hae");
    private Button empty = new Button("Tyhjennä");
    private Button all = new Button("Kaikki");

    private BeanValidationBinder<Mittaus> binder;

    private Mittaus mittaus;
   
    private List<Henkilo> henkilot;

    private final MittausRepository mittausRepository;
    private final HenkiloRepository henkiloRepository;

    public MittausView(@Autowired MittausRepository mittausRepository, HenkiloRepository henkiloRepository) {
        this.mittausRepository = mittausRepository;
        this.henkiloRepository = henkiloRepository;
        addClassNames("mittaus-view", "flex", "flex-col", "h-full");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        
        henkilot = henkiloRepository.findAll();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        
        Button toggleButton = new Button("Vaihda taustaväri", Click-> {
    		ThemeList themeList = UI.getCurrent().getElement().getThemeList();
    		
    		if (themeList.contains(Lumo.DARK)) {
    			themeList.remove(Lumo.DARK);
    		} else {
    			themeList.add(Lumo.DARK);
    		}
    	});
    	
    	add(toggleButton);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("id").setAutoWidth(true);
        grid.addColumn("toimenpide").setAutoWidth(true);
        grid.addColumn("yksikko").setAutoWidth(true);
        grid.addColumn("tulos").setAutoWidth(true);
        grid.addColumn("pvm").setAutoWidth(true);
        grid.addColumn("henkilo.id").setAutoWidth(true);
        grid.setItems(mittausRepository.findAll());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(MITTAUS_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MittausView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Mittaus.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.forField(id).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("id");
        
        binder.forField(toimenpide).asRequired("Toimenpide vaaditaan").bind("toimenpide");
        binder.forField(yksikko).asRequired("Yksikko vaaditaan").bind("yksikko");
        binder.forField(tulos).asRequired("Tulos vaaditaan").bind("tulos");
        //binder.forField(pvm).asRequired("Päivämäärä vaaditaan").bind("pvm");
        // binder.forField(henkilo).asRequired("Henkilo vaaditaan").bind(Mittaus::getHenkilo, Mittaus::setHenkilo);

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.mittaus == null) {
                    this.mittaus = new Mittaus();
                
	                binder.writeBean(this.mittaus);
	
	                mittausRepository.save(this.mittaus);
	                clearForm();
	                refreshGrid();
	                Notification.show("Mittaus tallennettu.");
	                UI.getCurrent().navigate(MittausView.class);
                }
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the mittaus details.");
            }
        });
        
        delete.addClickListener(e -> {
            try {
               
                binder.writeBean(this.mittaus);

                mittausRepository.deleteById(this.mittaus.getId());
                clearForm();
                refreshGrid();
                Notification.show("Mittaus poistettu.");
                UI.getCurrent().navigate(MittausView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the mittaus details.");
            }
        });
        
        search.addClickListener(e -> {
        	refreshGrid();
        });
        
        empty.addClickListener(e-> {
        	clearSearch();
        });

        all.addClickListener(e -> {
        	clearSearch();
            refreshGrid();
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> mittausId = event.getRouteParameters().getInteger(MITTAUS_ID);
        if (mittausId.isPresent()) {
            Optional<Mittaus> mittausFromBackend = mittausRepository.findById(mittausId.get());
            if (mittausFromBackend.isPresent()) {
                populateForm(mittausFromBackend.get());
            } else {
                Notification.show(String.format("The requested mittaus was not found, ID = %d", mittausId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MittausView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        id = new TextField("Id");
        id.setReadOnly(true);
        toimenpide = new TextField("Toimenpide");
        yksikko = new TextField("Yksikko");
        tulos = new TextField("Tulos");
        // henkilo_id = new TextField("Henkilo ID");
        pvm = new DateTimePicker("Pvm");
        henkilo = new Select<>();
        henkilo.setLabel("Henkilo");
        
        henkilo.setItemLabelGenerator(Henkilo::getNimi);
        henkilo.setItems(henkilot);
          
        Component[] fields = new Component[]{id, toimenpide, yksikko, tulos, pvm, henkilo};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY );
        buttonLayout.add(save, delete, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        
        HorizontalLayout searchFields = new HorizontalLayout();
        toimenpideHaku = new TextField("Toimenpide");
        alkuPvmHaku = new DateTimePicker("AlkuPvmHaku");
        loppuPvmHaku = new DateTimePicker("LoppuPvmHaku");
        henkiloHaku = new Select<>();
        henkiloHaku.setLabel("Henkilö");
        henkiloHaku.setItemLabelGenerator(Henkilo::getNimi);
        henkiloHaku.setItems(henkilot);

        searchFields.add(henkiloHaku);
        searchFields.add(toimenpideHaku);
        searchFields.add(alkuPvmHaku);
        searchFields.add(loppuPvmHaku);
        searchFields.add(search);
        searchFields.add(empty);
        searchFields.add(all);
        wrapper.add(searchFields);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        // Henkilo
        if (!henkiloHaku.isEmpty() && toimenpideHaku.isEmpty() && alkuPvmHaku.isEmpty() && loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByHenkilo(henkiloHaku.getValue()));
        }
        // Toimenpide
        else if (henkiloHaku.isEmpty() && !toimenpideHaku.isEmpty() && alkuPvmHaku.isEmpty() && loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByToimenpideContaining(toimenpideHaku.getValue()));
        }
        // Alkupvm ja Loppupvm
        else if (henkiloHaku.isEmpty() && toimenpideHaku.isEmpty() && !alkuPvmHaku.isEmpty() && !loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByPvmBetween(alkuPvmHaku.getValue(), loppuPvmHaku.getValue()));
        }
        // Henkilo ja Toimenpide
        else if (!henkiloHaku.isEmpty() && !toimenpideHaku.isEmpty() && alkuPvmHaku.isEmpty() && loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByHenkiloAndToimenpideContaining(henkiloHaku.getValue(), toimenpideHaku.getValue()));
        }
        // Henkilo, Alkupvm ja Loppupvm
        else if (!henkiloHaku.isEmpty() && toimenpideHaku.isEmpty() && !alkuPvmHaku.isEmpty() && !loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByPvmBetweenAndHenkilo(alkuPvmHaku.getValue(), loppuPvmHaku.getValue(), henkiloHaku.getValue()));
        }
        // Toimenpide, Alkupvm ja Loppupvm
        else if (henkiloHaku.isEmpty() && !toimenpideHaku.isEmpty() && !alkuPvmHaku.isEmpty() && !loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByPvmBetweenAndToimenpideContaining(alkuPvmHaku.getValue(), loppuPvmHaku.getValue(), toimenpideHaku.getValue()));
        }
        // Henkilo, Toimenpide, Alkupvm ja Loppupvm
        else if (!henkiloHaku.isEmpty() && !toimenpideHaku.isEmpty() && !alkuPvmHaku.isEmpty() && !loppuPvmHaku.isEmpty()) 
        {
        	grid.setItems(mittausRepository.findByPvmBetweenAndHenkiloAndToimenpideContaining(alkuPvmHaku.getValue(), loppuPvmHaku.getValue(), henkiloHaku.getValue(), toimenpideHaku.getValue()));
        }
        else
    	{
    		grid.setItems(mittausRepository.findAll());
    	}
    		
    }
    
    private void clearSearch() {
    	henkiloHaku.clear();
        toimenpideHaku.clear();
        alkuPvmHaku.clear();
        loppuPvmHaku.clear();
    }

    private void clearForm() {
        populateForm(null);
        
    }

    private void populateForm(Mittaus value) {
        this.mittaus = value;
        binder.readBean(this.mittaus);

    }
}
