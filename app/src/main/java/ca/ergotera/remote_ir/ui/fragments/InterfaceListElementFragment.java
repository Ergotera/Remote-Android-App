package ca.ergotera.remote_ir.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import ca.ergotera.remote_ir.app.R;
import ca.ergotera.remote_ir.misc.Logger;
import ca.ergotera.remote_ir.models.VirtualInterface;

/**
 * Fragment (UI and behaviour) displaying the list of the virtual interfaces of the user.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class InterfaceListElementFragment extends android.app.Fragment {

    private static final String CLASS_ID = InterfaceListElementFragment.class.getSimpleName();

    private View view;

    // Virtual interface model to represent
    private VirtualInterface interfaceModel;

    // UI elements
    private ConstraintLayout elementInterfaceInfoContainer;
    private TextView interfaceNameTextView;
    private TextView numberOfBtnsTextView;
    private ImageButton actionModeImgBtn;

    /**
     * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
     * to use this static method bundling up the parameters we need. We will then be able to
     * retrieve this data by using the getArguments().getTYPE(BUNDLE_IDENTIFIER) methods.
     *
     * @param id               id of the interface to be represented by the InterfaceListElement.
     * @param creationDate     creation date of the interface.
     * @param modificationDate modification date of the interface.
     * @param interfaceName    name of of the interface to be represented by the InterfaceListElement.
     * @param btnIds           ids of the buttons contained within the interface.
     * @return an instance of the fragment with the configured bundle.
     */
    public static InterfaceListElementFragment newInstance(int id, String creationDate, String modificationDate, String interfaceName, int[] btnIds) {
        InterfaceListElementFragment frag = new InterfaceListElementFragment();
        Bundle bundle = new Bundle(5);
        bundle.putInt(VirtualInterface.INTERFACE_ID, id);
        bundle.putString(VirtualInterface.INTERFACE_CREATION_DATE, creationDate);
        bundle.putString(VirtualInterface.INTERFACE_MODIFICATION_DATE, modificationDate);
        bundle.putString(VirtualInterface.INTERFACE_NAME, interfaceName);
        bundle.putIntArray(VirtualInterface.INTERFACE_BUTTONS, btnIds);
        frag.setArguments(bundle);
        return frag;
    }

    /**
     * Fragments should not have constructors, therefore, to pass arguments to a fragment, we need
     * to use this static method bundling up the parameters we need. We will then be able to
     * retrieve this data by using the getArguments().getTYPE(BUNDLE_IDENTIFIER) methods.
     *
     * @param virtualInterface interface to be represented by the InterfaceListElement.
     * @return an instance of the fragment with the configured bundle.
     */
    public static InterfaceListElementFragment newInstance(VirtualInterface virtualInterface) {
        InterfaceListElementFragment frag = new InterfaceListElementFragment();
        Bundle bundle = new Bundle(5);
        bundle.putInt(VirtualInterface.INTERFACE_ID, virtualInterface.getId());
        bundle.putString(VirtualInterface.INTERFACE_CREATION_DATE, virtualInterface.getCreationDate());
        bundle.putString(VirtualInterface.INTERFACE_MODIFICATION_DATE, virtualInterface.getModificationDate());
        bundle.putString(VirtualInterface.INTERFACE_NAME, virtualInterface.getName());
        bundle.putIntArray(VirtualInterface.INTERFACE_BUTTONS, virtualInterface.getButtonIds());
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.interfaceModel = new VirtualInterface();
        if (getArguments() != null) {
            this.interfaceModel.setId(getArguments().getInt(VirtualInterface.INTERFACE_ID));
            this.interfaceModel.setCreationDate(getArguments().getString(VirtualInterface.INTERFACE_CREATION_DATE));
            this.interfaceModel.setModificationDate(getArguments().getString(VirtualInterface.INTERFACE_MODIFICATION_DATE));
            this.interfaceModel.setName(getArguments().getString(VirtualInterface.INTERFACE_NAME));
            this.interfaceModel.setButtonIds(getArguments().getIntArray(VirtualInterface.INTERFACE_BUTTONS));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_interface_list_element, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeComponents();
    }

    private void initializeComponents() {
        findComponents();
        setupComponents();
    }

    private void findComponents() {
        this.elementInterfaceInfoContainer = (ConstraintLayout) view.findViewById(R.id.frag_interface_list_element_interface_info_container);
        this.interfaceNameTextView = (TextView) view.findViewById(R.id.frag_interface_list_element_interface_name);
        this.numberOfBtnsTextView = (TextView) view.findViewById(R.id.frag_interface_list_element_num_of_btns_value);
        this.actionModeImgBtn = (ImageButton) view.findViewById(R.id.frag_interface_list_element_action_mode_btn);
    }

    private void setupComponents() {
        setupListElementText();
        setupInterfaceListElementOnClick();
        setupActionImgBtnOnClick();
    }

    /**
     * Sets the image of the button list element if the virtual button data model has one.
     */
    private void setupListElementText() {
        this.interfaceNameTextView.setText(this.interfaceModel.getName());
        if (this.interfaceModel.getButtonIds() != null) {
            this.numberOfBtnsTextView.setText(Integer.toString(this.interfaceModel.getButtonIds().length));
        } else {
            this.numberOfBtnsTextView.setText(Integer.toString(-1));
            Logger.Error(CLASS_ID, "An error occured while trying to get the number of buttons in" +
                    " the interface '" + this.interfaceModel.getName() + "'.");
        }
    }

    /**
     * Sets the click listener for the button list element.
     */
    public void setupInterfaceListElementOnClick() {
        elementInterfaceInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager fragMgr = getFragmentManager();
                fragMgr.beginTransaction().replace(R.id.content_frame, CreateInterfaceFragment.newInstance(interfaceModel.getId())).addToBackStack(null).commit();
            }
        });
    }

    /**
     * Sets the click listener for the action image button.
     */
    private void setupActionImgBtnOnClick(){
        actionModeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager fragMgr = getFragmentManager();
                fragMgr.beginTransaction().replace(R.id.content_frame, ActionModeFragment.newInstance(interfaceModel.getId())).addToBackStack(null).commit();
            }
        });
    }
}
