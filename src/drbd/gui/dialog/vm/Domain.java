/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package drbd.gui.dialog.vm;

import drbd.utilities.Tools;
import drbd.gui.resources.VMSVirtualDomainInfo;
import drbd.gui.dialog.WizardDialog;
import drbd.data.VMSXML;

import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import java.awt.Component;
import java.awt.Dimension;

/**
 * An implementation of a dialog where user can enter a new domain.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public class Domain extends VMConfig {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Input pane cache for back button. */
    private JComponent inputPane = null;
    /** Configuration options of the new domain. */
    private static final String[] PARAMS = {VMSXML.VM_PARAM_NAME,
                                            VMSXML.VM_PARAM_VCPU,
                                            VMSXML.VM_PARAM_CURRENTMEMORY,
                                            VMSXML.VM_PARAM_BOOT,
                                            VMSXML.VM_PARAM_ARCH,
                                            VMSXML.VM_PARAM_EMULATOR};
    /** Next dialog object. */
    private WizardDialog nextDialogObject = null;

    /** Prepares a new <code>Domain</code> object. */
    public Domain(final WizardDialog previousDialog,
                    final VMSVirtualDomainInfo vmsVirtualDomainInfo) {
        super(previousDialog, vmsVirtualDomainInfo);
    }

    /** Next dialog. */
    public final WizardDialog nextDialog() {
        if (nextDialogObject == null) {
            nextDialogObject =
                    new InstallationDisk(this, getVMSVirtualDomainInfo());
        }
        return nextDialogObject;
    }

    /**
     * Returns the title of the dialog. It is defined as
     * Dialog.vm.Domain.Title in TextResources.
     */
    protected final String getDialogTitle() {
        return Tools.getString("Dialog.vm.Domain.Title");
    }

    /**
     * Returns the description of the dialog. It is defined as
     * Dialog.vm.Domain.Description in TextResources.
     */
    protected final String getDescription() {
        return Tools.getString("Dialog.vm.Domain.Description");
    }

    /** Inits dialog. */
    protected final void initDialog() {
        super.initDialog();
        enableComponentsLater(new JComponent[]{buttonClass(nextButton())});
        enableComponents();
        final VMSVirtualDomainInfo vdi = getVMSVirtualDomainInfo();
        final boolean enable = vdi.checkResourceFieldsCorrect(null, PARAMS);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                buttonClass(nextButton()).setEnabled(enable);
            }
        });
    }

    /** Returns input pane where user can configure a vm. */
    protected final JComponent getInputPane() {
        final VMSVirtualDomainInfo vdi = getVMSVirtualDomainInfo();
        vdi.selectMyself();
        if (inputPane != null) {
            return inputPane;
        }
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        final JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        vdi.waitForInfoPanel();
        vdi.addWizardParams(
                          optionsPanel,
                          PARAMS,
                          buttonClass(nextButton()),
                          Tools.getDefaultInt("Dialog.vm.Resource.LabelWidth"),
                          Tools.getDefaultInt("Dialog.vm.Resource.FieldWidth"),
                          null);
        vdi.paramComboBoxGet(VMSXML.VM_PARAM_BOOT, "wizard").setValue("CD-ROM");

        panel.add(optionsPanel);

        final JScrollPane sp = new JScrollPane(panel);
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 200));
        sp.setPreferredSize(new Dimension(Short.MAX_VALUE, 200));
        inputPane = sp;
        return sp;
    }
}