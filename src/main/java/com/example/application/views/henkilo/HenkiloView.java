package com.example.application.views.henkilo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicBorders.ToggleButtonBorder;

import com.example.application.data.entity.Henkilo;
import com.example.application.data.entity.Mittaus;
import com.example.application.data.service.HenkiloRepository;
import com.example.application.data.service.MittausRepository;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.artur.helpers.CrudServiceDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.router.PageTitle;
import com.example.application.views.MainLayout;
import com.example.application.views.mittaus.MittausView;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.component.textfield.TextField;

@PageTitle("Henkilo")
@Route(value = "master-detail/:henkiloID?/:action?(edit)", layout = MainLayout.class)
public class HenkiloView extends Div implements BeforeEnterObserver {

    private final String HENKILO_ID = "henkiloID";
    private final String HENKILO_EDIT_ROUTE_TEMPLATE = "master-detail/%d/edit";

    private Grid<Henkilo> grid = new Grid<>(Henkilo.class, false);
    
    private TextField id;
    private TextField nimi;
    private TextField puhelinnumero;
    private TextField email;
    private TextField nimiHaku;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");
    private Button delete = new Button("Delete");
    private Button search = new Button("Hae");
    private Button empty = new Button("Tyhjennä");
    private Button all = new Button("Kaikki");

    // käytetään datan sitomiseen generaattorilta gridiin
    private BeanValidationBinder<Henkilo> binder;

    private Henkilo henkilo;

    private final HenkiloRepository henkiloRepository;
    private final MittausRepository mittausRepository;
    

    public HenkiloView(@Autowired HenkiloRepository henkiloRepository, MittausRepository mittausRepository) {
        this.henkiloRepository = henkiloRepository;
        this.mittausRepository = mittausRepository;
        addClassNames("henkilo-view", "flex", "flex-col", "h-full");
        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);
        
        Button toggleButton = new Button("Vaihda taustaväri", Click -> {
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
        grid.addColumn("nimi").setAutoWidth(true);
        grid.addColumn("puhelinnumero").setAutoWidth(true);
        grid.addColumn("email").setAutoWidth(true);
        // hakee kaikki henkilöt kannasta ja tuo taulukkoon
        grid.setItems(henkiloRepository.findAll());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(HENKILO_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(HenkiloView.class);
            }
        });
        

        // Configure Form
        binder = new BeanValidationBinder<>(Henkilo.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.forField(id).withConverter(new StringToIntegerConverter("Only numbers are allowed")).bind("id");
        
        binder.forField(nimi).asRequired("Nimi vaaditaan").bind("nimi");
        binder.forField(puhelinnumero).asRequired("Puhelinnumero vaaditaan").bind("puhelinnumero");
        binder.forField(email).asRequired("Sähköposti vaaditaan").bind("email");

        binder.bindInstanceFields(this);
        
        // Dialogi
        Dialog dialog = new Dialog();
        

        //Button button = new Button("Show dialog", e -> dialog.open());
        //add(dialog, button);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.henkilo == null) 
                {
                    this.henkilo = new Henkilo();
                }
                binder.writeBean(this.henkilo);

                // tallentaa tiedon kantaan
                henkiloRepository.save(this.henkilo);
                clearForm();
                refreshGrid();
                Notification.show("Henkilo tallennettu.");
                UI.getCurrent().navigate(HenkiloView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the henkilo details.");
            }
        });
        
        delete.addClickListener(e -> {
            try
            {
                binder.writeBean(this.henkilo);

                List<Mittaus> mittaukset = new ArrayList<Mittaus>();
				mittausRepository.findByHenkiloId(this.henkilo.getId()).forEach(mittaukset::add);
	
				 if (mittaukset != null)
				 {
					 // ALKAA
					 Dialog firstDialog = new Dialog();
					 
					 firstDialog.open();
					 
					 Button button = new Button("Open dialog");
					 
					 
					 
					 firstDialog.add(
					     new Text("This is the first dialog"),
					     button
					 );
					 firstDialog.setModal(false);
					 firstDialog.setDraggable(true);
					 firstDialog.setResizable(true);
					 
					 button.addClickListener(event -> dialog.close());

					 
					
					 // LOPPUU
					 
					  for (Mittaus m : mittaukset) 
						 {
							 mittausRepository.deleteById(m.getId());
						 }
				 }
			 
                henkiloRepository.deleteById(this.henkilo.getId());
                clearForm();
                refreshGrid();
                Notification.show("Henkilö poistettu.");
                UI.getCurrent().navigate(HenkiloView.class);
 	                
            } 
            catch (ValidationException validationException) 
            {
                Notification.show("An exception happened while trying to store the henkilo details.");
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

    private Object setStatus(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> henkiloId = event.getRouteParameters().getInteger(HENKILO_ID);
        if (henkiloId.isPresent()) {
            Optional<Henkilo> henkiloFromBackend = henkiloRepository.findById(henkiloId.get());
            if (henkiloFromBackend.isPresent()) {
                populateForm(henkiloFromBackend.get());
            } else {
                Notification.show(String.format("The requested henkilo was not found, ID = %d", henkiloId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(HenkiloView.class);
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
        nimi = new TextField("Nimi");
        puhelinnumero = new TextField("Puhelinnumero");
        email = new TextField("Email");
        Component[] fields = new Component[]{id, nimi, puhelinnumero, email};

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
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, delete, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        
        HorizontalLayout searchFields = new HorizontalLayout();
        nimiHaku = new TextField("Nimi");
 
        searchFields.add(nimiHaku);
        searchFields.add(search);
        searchFields.add(empty);
        searchFields.add(all);
        wrapper.add(searchFields);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        // Henkilo
        if (!nimiHaku.isEmpty()) 
        {
        	grid.setItems(henkiloRepository.findByNimiContaining(nimiHaku.getValue()));
        }
        else
    	{
    		grid.setItems(henkiloRepository.findAll());
    	}
    }

    private void clearForm() {
        populateForm(null);
    }
    
    private void clearSearch() {
    	nimiHaku.clear();
    }

    private void populateForm(Henkilo value) {
        this.henkilo = value;
        binder.readBean(this.henkilo);

    }
    
 
   
    	  
}
