/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009-2010, LINBIT HA-Solutions GmbH.
 * Copyright (C) 2009-2010, Rasto Levrinc
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
package lcmc.gui.resources;

import lcmc.gui.Browser;
import lcmc.gui.GuiComboBox;
import lcmc.data.VMSXML;
import lcmc.data.VMSXML.DiskData;
import lcmc.data.Host;
import lcmc.data.ConfigData;
import lcmc.data.AccessMode;
import lcmc.utilities.Tools;
import lcmc.utilities.MyButton;

import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.w3c.dom.Node;

/**
 * This class holds info about Virtual Disks.
 */
public final class VMSDiskInfo extends VMSHardwareInfo {
    /** Source file combo box, so that it can be disabled, depending on type. */
    private final Map<String, GuiComboBox> sourceFileCB =
                                            new HashMap<String, GuiComboBox>();
    /** Source block combo box, so that it can be disabled, depending on type.*/
    private final Map<String, GuiComboBox> sourceDeviceCB =
                                            new HashMap<String, GuiComboBox>();
    /** Target device combo box, that needs to be reloaded if target type has
     * changed. */
    private final Map<String, GuiComboBox> targetDeviceCB =
                                            new HashMap<String, GuiComboBox>();
    /** Driver name combo box. */
    private final Map<String, GuiComboBox> driverNameCB =
                                            new HashMap<String, GuiComboBox>();
    /** Driver type combo box. */
    private final Map<String, GuiComboBox> driverTypeCB =
                                            new HashMap<String, GuiComboBox>();
    /** Readonly combo box. */
    private final Map<String, GuiComboBox> readonlyCB =
                                            new HashMap<String, GuiComboBox>();
    /** Previous target bus value. */
    private String prevTargetBus = null;
    /** Parameters. */
    private static final String[] PARAMETERS = {DiskData.TYPE,
                                                DiskData.TARGET_BUS_TYPE,
                                                DiskData.TARGET_DEVICE,
                                                DiskData.SOURCE_FILE,
                                                DiskData.SOURCE_DEVICE,
                                                DiskData.DRIVER_NAME,
                                                DiskData.DRIVER_TYPE,
                                                DiskData.READONLY,
                                                DiskData.SHAREABLE};
    /** Block parameters. */
    private static final String[] BLOCK_PARAMETERS = {DiskData.TYPE,
                                                      DiskData.TARGET_BUS_TYPE,
                                                      DiskData.TARGET_DEVICE,
                                                      DiskData.SOURCE_DEVICE,
                                                      DiskData.DRIVER_NAME,
                                                      DiskData.DRIVER_TYPE,
                                                      DiskData.READONLY,
                                                      DiskData.SHAREABLE};
    /** File parameters. */
    private static final String[] FILE_PARAMETERS = {DiskData.TYPE,
                                                     DiskData.TARGET_DEVICE,
                                                     DiskData.SOURCE_FILE,
                                                     DiskData.TARGET_BUS_TYPE,
                                                     DiskData.DRIVER_NAME,
                                                     DiskData.DRIVER_TYPE,
                                                     DiskData.READONLY,
                                                     DiskData.SHAREABLE};
    /** Whether the parameter is enabled only in advanced mode. */
    private static final Set<String> IS_ENABLED_ONLY_IN_ADVANCED =
        new HashSet<String>(Arrays.asList(new String[]{
                                                DiskData.TARGET_DEVICE,
                                                DiskData.DRIVER_NAME,
                                                DiskData.DRIVER_TYPE}));
    /** Whether the parameter is required. */
    private static final Set<String> IS_REQUIRED =
        new HashSet<String>(Arrays.asList(new String[]{DiskData.TYPE}));
    /** Field type. */
    private static final Map<String, GuiComboBox.Type> FIELD_TYPES =
                                       new HashMap<String, GuiComboBox.Type>();
    /** Target devices depending on the target type. */
    private static final Map<String, String[]> TARGET_DEVICES_MAP =
                                           new HashMap<String, String[]>();
    static {
        FIELD_TYPES.put(DiskData.TYPE,
                        GuiComboBox.Type.RADIOGROUP);
        FIELD_TYPES.put(DiskData.SOURCE_FILE,
                        GuiComboBox.Type.COMBOBOX);
        FIELD_TYPES.put(DiskData.READONLY,
                        GuiComboBox.Type.CHECKBOX);
        FIELD_TYPES.put(DiskData.SHAREABLE,
                        GuiComboBox.Type.CHECKBOX);
        FIELD_TYPES.put(DiskData.TARGET_DEVICE,
                        GuiComboBox.Type.COMBOBOX);
    }
    /** Short name. */
    private static final Map<String, String> SHORTNAME_MAP =
                                                 new HashMap<String, String>();
    static {
        SHORTNAME_MAP.put(DiskData.TYPE, "Type");
        SHORTNAME_MAP.put(DiskData.TARGET_DEVICE, "Target Device");
        SHORTNAME_MAP.put(DiskData.SOURCE_FILE, "Source File");
        SHORTNAME_MAP.put(DiskData.SOURCE_DEVICE, "Source Device");
        SHORTNAME_MAP.put(DiskData.TARGET_BUS_TYPE, "Disk Type");
        SHORTNAME_MAP.put(DiskData.DRIVER_NAME, "Driver Name");
        SHORTNAME_MAP.put(DiskData.DRIVER_TYPE, "Driver Type");
        SHORTNAME_MAP.put(DiskData.READONLY, "Readonly");
        SHORTNAME_MAP.put(DiskData.SHAREABLE, "Shareable");
    }

    /** Preferred values. */
    private static final Map<String, String> PREFERRED_MAP =
                                                 new HashMap<String, String>();
    /** Defaults. */
    private static final Map<String, String> DEFAULTS_MAP =
                                                 new HashMap<String, String>();
    /** Possible values. */
    private static final Map<String, Object[]> POSSIBLE_VALUES =
                                              new HashMap<String, Object[]>();
    /** Default location for libvirt images. */
    public static final String LIBVIRT_IMAGE_LOCATION =
                                             "/var/lib/libvirt/images/";
    /** A map from target bus and type as it is saved to the string
     * representation that appears in the menus. */
    private static final Map<String, String> TARGET_BUS_TYPES =
                                                 new HashMap<String, String>();
    static {
        POSSIBLE_VALUES.put(DiskData.TYPE,
                            new StringInfo[]{
                                 new StringInfo("Image file", "file", null),
                                 new StringInfo("Disk/block device",
                                                "block",
                                                null)});
        POSSIBLE_VALUES.put(
                    DiskData.TARGET_BUS_TYPE,
                    new StringInfo[]{
                       new StringInfo("IDE Disk",    "ide/disk",    null),
                       new StringInfo("IDE CDROM",   "ide/cdrom",    null),
                       new StringInfo("Floppy Disk", "fdc/floppy", null),
                       new StringInfo("SCSI Disk",   "scsi/disk",   null),
                       new StringInfo("USB Disk",    "usb/disk",    null),
                       new StringInfo("Virtio Disk", "virtio/disk", null)});
        POSSIBLE_VALUES.put(DiskData.DRIVER_NAME, new String[]{null,
                                                               "qemu",
                                                               "file",
                                                               "phy"});
        POSSIBLE_VALUES.put(DiskData.DRIVER_TYPE, new String[]{null, "raw"});
        for (final StringInfo tbt : (StringInfo[]) POSSIBLE_VALUES.get(
                                                  DiskData.TARGET_BUS_TYPE)) {
            TARGET_BUS_TYPES.put(tbt.getStringValue(), tbt.toString());
        }
        DEFAULTS_MAP.put(DiskData.READONLY, "False");
        DEFAULTS_MAP.put(DiskData.SHAREABLE, "False");
        PREFERRED_MAP.put(DiskData.DRIVER_NAME, "qemu");
        TARGET_DEVICES_MAP.put("ide/disk",
                               new String[]{"hda", "hdb", "hdd"});
        TARGET_DEVICES_MAP.put("ide/cdrom",
                               new String[]{"hdc"});
        TARGET_DEVICES_MAP.put("fdc/floppy",
                               new String[]{"fda", "fdb", "fdc", "fdd"});
        TARGET_DEVICES_MAP.put("scsi/disk",
                               new String[]{"sda", "sdb", "sdc", "sdd"});
        TARGET_DEVICES_MAP.put("usb/disk",
                               new String[]{"sda", "sdb", "sdc", "sdd"});
        TARGET_DEVICES_MAP.put("virtio/disk",
                               new String[]{"vda", "vdb", "vdc", "vdd", "vde"});
    }
    /** Table panel. */
    private JComponent tablePanel = null;

    /** Creates the VMSDiskInfo object. */
    VMSDiskInfo(final String name, final Browser browser,
                       final VMSVirtualDomainInfo vmsVirtualDomainInfo) {
        super(name, browser, vmsVirtualDomainInfo);
    }

    /** Adds disk table with only this disk to the main panel. */
    @Override
    protected void addHardwareTable(final JPanel mainPanel) {
        tablePanel = getTablePanel("Disk",
                                   VMSVirtualDomainInfo.DISK_TABLE,
                                   getNewBtn(getVMSVirtualDomainInfo()));
        if (getResource().isNew()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    tablePanel.setVisible(false);
                }
            });
        }
        mainPanel.add(tablePanel);
    }

    /** Returns service icon in the menu. */
    @Override
    public ImageIcon getMenuIcon(final boolean testOnly) {
        return BlockDevInfo.HARDDISK_ICON;
    }

    /** Returns long description of the specified parameter. */
    @Override
    protected String getParamLongDesc(final String param) {
        return getParamShortDesc(param);
    }

    /** Returns short description of the specified parameter. */
    @Override
    protected String getParamShortDesc(final String param) {
        final String name = SHORTNAME_MAP.get(param);
        if (name == null) {
            return param;
        }
        return name;
    }

    /** Returns preferred value for specified parameter. */
    @Override
    protected String getParamPreferred(final String param) {
        return PREFERRED_MAP.get(param);
    }

    /** Returns default value for specified parameter. */
    @Override
    protected String getParamDefault(final String param) {
        return DEFAULTS_MAP.get(param);
    }

    /** Returns parameters. */
    @Override
    public String[] getParametersFromXML() {
        return PARAMETERS.clone();
    }

    /** Returns real parameters. */
    @Override
    public String[] getRealParametersFromXML() {
        if ("block".equals(getComboBoxValue(DiskData.TYPE))) {
            return BLOCK_PARAMETERS.clone();
        } else {
            return FILE_PARAMETERS.clone();
        }
    }

    /** Returns possible choices for drop down lists. */
    @Override
    protected Object[] getParamPossibleChoices(final String param) {
        if (DiskData.SOURCE_FILE.equals(param)) {
            final Set<String> sourceFileDirs = new TreeSet<String>();
            sourceFileDirs.add(LIBVIRT_IMAGE_LOCATION);
            for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
                final VMSXML vmsxml = getBrowser().getVMSXML(h);
                sourceFileDirs.addAll(vmsxml.getsourceFileDirs());
            }
            return sourceFileDirs.toArray(new String[sourceFileDirs.size()]);
        } else if (DiskData.SOURCE_DEVICE.equals(param)) {
            for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
                final VMSXML vmsxml = getBrowser().getVMSXML(h);
                final List<String> bds = new ArrayList<String>();
                bds.add(null);
                if (vmsxml != null) {
                    for (final BlockDevInfo bdi
                            : h.getBrowser().getBlockDevInfos()) {
                        if (bdi.getBlockDevice().isDrbd()) {
                            bds.add(bdi.getDrbdVolumeInfo().getDevice());
                        } else {
                            bds.add(bdi.getName());
                        }
                    }
                    return bds.toArray(new String[bds.size()]);
                }
            }
        }
        return POSSIBLE_VALUES.get(param);
    }

    /** Returns section to which the specified parameter belongs. */
    @Override
    protected String getSection(final String param) {
        return "Disk Options";
    }

    /** Returns true if the specified parameter is required. */
    @Override
    protected boolean isRequired(final String param) {
        final String type = getComboBoxValue(DiskData.TYPE);
        if ((DiskData.SOURCE_FILE.equals(param) && "file".equals(type))
            || (DiskData.SOURCE_DEVICE.equals(param) && "block".equals(type))) {
            if ("ide/cdrom".equals(getComboBoxValue(DiskData.TARGET_BUS_TYPE))
                || "fdc/floppy".equals(getComboBoxValue(
                                                 DiskData.TARGET_BUS_TYPE))) {
                return false;
            } else {
                return true;
            }
        }
        return IS_REQUIRED.contains(param);
    }

    /** Returns true if the specified parameter is integer. */
    @Override
    protected boolean isInteger(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is label. */
    @Override
    protected boolean isLabel(final String param) {
        return false;
    }

    /** Returns true if the specified parameter is of time type. */
    @Override
    protected boolean isTimeType(final String param) {
        return false;
    }

    /** Returns whether parameter is checkbox. */
    @Override
    protected boolean isCheckBox(final String param) {
        return false;
    }

    /** Returns the type of the parameter. */
    @Override
    protected String getParamType(final String param) {
        return "undef"; // TODO:
    }

    /** Returns type of the field. */
    @Override
    protected GuiComboBox.Type getFieldType(final String param) {
        return FIELD_TYPES.get(param);
    }

    /** Returns device parameters. */
    @Override
    protected Map<String, String> getHWParameters(final boolean allParams) {
        Tools.invokeAndWait(new Runnable() {
            public void run() {
                getInfoPanel();
            }
        });
        final String[] params = getRealParametersFromXML();
        final Map<String, String> parameters = new HashMap<String, String>();
        for (final String param : params) {
            final String value = getComboBoxValue(param);
            if (DiskData.TYPE.equals(param)) {
                parameters.put(param, value);
            } else if (DiskData.TARGET_BUS_TYPE.equals(param)) {
                if (value == null) {
                    parameters.put(DiskData.TARGET_BUS, null);
                    parameters.put(DiskData.TARGET_TYPE, null);
                } else {
                    final String[] values = value.split("/");
                    if (values.length == 2) {
                        parameters.put(DiskData.TARGET_BUS, values[0]);
                        parameters.put(DiskData.TARGET_TYPE, values[1]);
                    } else {
                        Tools.appWarning("cannot parse: "
                                         + param
                                         + " = "
                                         + value);
                    }
                }
            } else if (allParams) {
                if (Tools.areEqual(getParamDefault(param), value)) {
                    parameters.put(param, null);
                } else {
                    parameters.put(param, value);
                }
            } else if (!Tools.areEqual(getParamSaved(param), value)
                       || DiskData.SOURCE_FILE.equals(param)
                       || DiskData.SOURCE_DEVICE.equals(param)) {
                if (Tools.areEqual(getParamDefault(param), value)) {
                    parameters.put(param, null);
                } else {
                    parameters.put(param, value);
                }
            }
        }
        parameters.put(DiskData.SAVED_TARGET_DEVICE, getName());
        setName(getParamSaved(DiskData.TARGET_DEVICE));
        return parameters;
    }

    /** Applies the changes. */
    @Override
    void apply(final boolean testOnly) {
        if (testOnly) {
            return;
        }
        Tools.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                getApplyButton().setEnabled(false);
                getRevertButton().setEnabled(false);
            }
        });
        getInfoPanel();
        waitForInfoPanel();
        final Map<String, String> parameters =
                                    getHWParameters(getResource().isNew());
        final String[] params = getRealParametersFromXML();
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                final String domainName =
                                getVMSVirtualDomainInfo().getDomainName();
                final Node domainNode = vmsxml.getDomainNode(domainName);
                modifyXML(vmsxml, domainNode, domainName, parameters);
                vmsxml.saveAndDefine(domainNode, domainName);
            }
        }
        getResource().setNew(false);
        getBrowser().reload(getNode(), false);
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            getBrowser().periodicalVMSUpdate(h);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tablePanel.setVisible(true);
            }
        });
        if (!testOnly) {
            storeComboBoxValues(params);
        }
        checkResourceFieldsChanged(null, params);
    }

    /** Modify device xml. */
    @Override
    protected void modifyXML(final VMSXML vmsxml,
                             final Node node,
                             final String domainName,
                             final Map<String, String> params) {
        if (vmsxml != null) {
            vmsxml.modifyDiskXML(node, domainName, params);
        }
    }

    /** Returns data for the table. */
    @Override
    protected Object[][] getTableData(final String tableName) {
        if (VMSVirtualDomainInfo.HEADER_TABLE.equals(tableName)) {
            return getVMSVirtualDomainInfo().getMainTableData();
        } else if (VMSVirtualDomainInfo.DISK_TABLE.equals(tableName)) {
            if (getResource().isNew()) {
                return new Object[][]{};
            }
            return new Object[][]{getVMSVirtualDomainInfo().getDiskDataRow(
                                        getName(),
                                        null,
                                        getVMSVirtualDomainInfo().getDisks(),
                                        true)};
        }
        return new Object[][]{};
    }

    /** Returns whether this parameter is advanced. */
    @Override
    protected boolean isAdvanced(final String param) {
        return false;
    }

    /** Returns access type of this parameter. */
    @Override
    protected ConfigData.AccessType getAccessType(final String param) {
        return ConfigData.AccessType.ADMIN;
    }
    /** Returns true if the value of the parameter is ok. */
    @Override
    protected boolean checkParam(final String param, final String newValue) {
        if (DiskData.TYPE.equals(param)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (final String p : sourceFileCB.keySet()) {
                        sourceFileCB.get(p).setVisible(
                                                "file".equals(newValue));
                    }
                    for (final String p : sourceDeviceCB.keySet()) {
                        sourceDeviceCB.get(p).setVisible(
                                                "block".equals(newValue));
                    }
                }
            });
            checkOneParam(DiskData.SOURCE_FILE);
            checkOneParam(DiskData.SOURCE_DEVICE);
        } else if (DiskData.TARGET_BUS_TYPE.equals(param)) {
            final Set<String> devices = new LinkedHashSet<String>();
            devices.add(null);
            if (newValue != null) {
                final String[] targetDevices =
                                            TARGET_DEVICES_MAP.get(newValue);
                if (targetDevices != null) {
                    for (final String dev : targetDevices) {
                        if (!getVMSVirtualDomainInfo().isDevice(dev)) {
                            devices.add(dev);
                        }
                    }
                }
            }
            final String saved = getParamSaved(DiskData.TARGET_DEVICE);
            String selected = null;
            devices.add(saved);
            if (saved != null) {
                selected = saved;
            } else if (devices.size() > 1) {
                selected = devices.toArray(new String[devices.size()])[1];
            }
            if (prevTargetBus == null
                || !prevTargetBus.equals(selected)) {
                final String sel = selected;
                for (final String p : targetDeviceCB.keySet()) {
                    //SwingUtilities.invokeLater(new Runnable() {
                    //    @Override
                    //    public void run() {
                            targetDeviceCB.get(p).reloadComboBox(
                                sel,
                                devices.toArray(new String[devices.size()]));
                    //    }
                    //});
                }
                prevTargetBus = selected;
            }
            if (getParamSaved(DiskData.DRIVER_NAME) == null) {
                if ("ide/cdrom".equals(newValue)) {
                    for (final String p : readonlyCB.keySet()) {
                        readonlyCB.get(p).setValue("True");
                    }
                    for (final String p : driverTypeCB.keySet()) {
                        if (getResource().isNew()) {
                            driverTypeCB.get(p).setValue("raw");
                        } else {
                            if (driverTypeCB.get(p).getValue() != null) {
                                driverTypeCB.get(p).setValue(null);
                            }
                        }
                    }
                } else if ("virtio/disk".equals(newValue)) {
                    for (final String p : driverTypeCB.keySet()) {
                        driverTypeCB.get(p).setValue("raw");
                    }
                } else {
                    for (final String p : readonlyCB.keySet()) {
                        readonlyCB.get(p).setValue("False");
                        if (getResource().isNew()) {
                            driverTypeCB.get(p).setValue("raw");
                        }
                    }
                }
            }
            checkOneParam(DiskData.SOURCE_FILE);
            checkOneParam(DiskData.SOURCE_DEVICE);
        }
        if (isRequired(param) && (newValue == null || "".equals(newValue))) {
            return false;
        }
        return true;
    }

    /** Whether the parameter should be enabled. */
    @Override
    protected String isEnabled(final String param) {
        return null;
    }

    /** Whether the parameter should be enabled only in advanced mode. */
    @Override
    protected boolean isEnabledOnlyInAdvancedMode(final String param) {
         return IS_ENABLED_ONLY_IN_ADVANCED.contains(param);
    }

    /** Updates parameters. */
    void updateParameters() {
        final Map<String, DiskData> disks =
                                        getVMSVirtualDomainInfo().getDisks();
        if (disks != null) {
            final DiskData diskData = disks.get(getName());
            if (diskData != null) {
                for (final String param : getParametersFromXML()) {
                    final String oldValue = getParamSaved(param);
                    String value = getParamSaved(param);
                    final GuiComboBox cb = paramComboBoxGet(param, null);
                    for (final Host h
                            : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
                        final VMSXML vmsxml = getBrowser().getVMSXML(h);
                        if (vmsxml != null) {
                            final String savedValue =
                                               diskData.getValue(param);
                            if (savedValue != null) {
                                value = savedValue;
                            }
                        }
                    }
                    if (!Tools.areEqual(value, oldValue)) {
                        getResource().setValue(param, value);
                        if (cb != null) {
                            /* only if it is not changed by user. */
                            cb.setValue(value);
                        }
                    }
                }
            }
        }
        updateTable(VMSVirtualDomainInfo.HEADER_TABLE);
        updateTable(VMSVirtualDomainInfo.DISK_TABLE);
        //checkResourceFieldsChanged(null, getParametersFromXML());
        setApplyButtons(null, getRealParametersFromXML());
    }

    /** Returns combo box for parameter. */
    @Override
    protected GuiComboBox getParamComboBox(final String param,
                                           final String prefix,
                                           final int width) {
        if (DiskData.SOURCE_FILE.equals(param)) {
            final String sourceFile = getParamSaved(DiskData.SOURCE_FILE);
            final String regexp = "[^/]$";
            final MyButton fileChooserBtn = new MyButton("Browse...");
            fileChooserBtn.miniButton();
            final GuiComboBox paramCB = new GuiComboBox(
                                  sourceFile,
                                  getParamPossibleChoices(param),
                                  null, /* units */
                                  getFieldType(param),
                                  regexp,
                                  width,
                                  null, /* abbrv */
                                  new AccessMode(getAccessType(param),
                                                 false), /* only adv. mode */
                                  fileChooserBtn);
            paramCB.setAlwaysEditable(true);
            if (prefix == null) {
                sourceFileCB.put("", paramCB);
            } else {
                sourceFileCB.put(prefix, paramCB);
            }
            if (Tools.isWindows()) {
                /* does not work on windows and I tries, ultimatly because
                   FilePane.usesShellFolder(fc) in BasicFileChooserUI returns
                   true and it is not possible to descent into a directory.
                   TODO: It may work in the future.
                */
                paramCB.setTFButtonEnabled(false);
            }
            fileChooserBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    final Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String file;
                            final String oldFile = paramCB.getStringValue();
                            if (oldFile == null || "".equals(oldFile)) {
                                file = LIBVIRT_IMAGE_LOCATION;
                            } else {
                                file = oldFile;
                            }
                            startFileChooser(paramCB, file);
                        }
                    });
                    t.start();
                }
            });
            paramComboBoxAdd(param, prefix, paramCB);
            return paramCB;
        } else {
            final GuiComboBox paramCB =
                                 super.getParamComboBox(param, prefix, width);
            if (DiskData.TYPE.equals(param)
                || DiskData.TARGET_BUS_TYPE.equals(param)) {
                paramCB.setAlwaysEditable(false);
            } else if (DiskData.SOURCE_DEVICE.equals(param)) {
                if (prefix == null) {
                    sourceDeviceCB.put("", paramCB);
                } else {
                    sourceDeviceCB.put(prefix, paramCB);
                }
            } else if (DiskData.TARGET_DEVICE.equals(param)) {
                if (prefix == null) {
                    targetDeviceCB.put("", paramCB);
                } else {
                    targetDeviceCB.put(prefix, paramCB);
                }
            } else if (DiskData.DRIVER_NAME.equals(param)) {
                if (prefix == null) {
                    driverNameCB.put("", paramCB);
                } else {
                    driverNameCB.put(prefix, paramCB);
                }
            } else if (DiskData.DRIVER_TYPE.equals(param)) {
                if (prefix == null) {
                    driverTypeCB.put("", paramCB);
                } else {
                    driverTypeCB.put(prefix, paramCB);
                }
            } else if (DiskData.READONLY.equals(param)) {
                if (prefix == null) {
                    readonlyCB.put("", paramCB);
                } else {
                    readonlyCB.put(prefix, paramCB);
                }
            }
            return paramCB;
        }
    }

    /** Removes this disk without confirmation dialog. */
    @Override
    protected void removeMyselfNoConfirm(final boolean testOnly) {
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            final VMSXML vmsxml = getBrowser().getVMSXML(h);
            if (vmsxml != null) {
                final Map<String, String> parameters =
                                                new HashMap<String, String>();
                parameters.put(DiskData.SAVED_TARGET_DEVICE, getName());
                vmsxml.removeDiskXML(getVMSVirtualDomainInfo().getDomainName(),
                                     parameters);
            }
        }
        for (final Host h : getVMSVirtualDomainInfo().getDefinedOnHosts()) {
            getBrowser().periodicalVMSUpdate(h);
        }
        removeNode();
    }

    /** Returns string representation. */
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder(30);
        final String name = getName();
        if (name == null) {
            return "new disk...";
        }
        s.append(name);
        s.append(" (");
        final String saved = getParamSaved(DiskData.TARGET_BUS_TYPE);
        if (saved == null) {
            s.append("new...");
        } else if (TARGET_BUS_TYPES.containsKey(saved)) {
            s.append(TARGET_BUS_TYPES.get(saved));
        } else {
            s.append(saved);
        }
        s.append(')');
        return s.toString();
    }

    /**
     * Returns whether this item is removeable (null), or string why it isn't.
     */
    @Override
    protected String isRemoveable() {
        return null;
    }

    /** Returns "add new" button. */
    static MyButton getNewBtn(final VMSVirtualDomainInfo vdi) {
        final MyButton newBtn = new MyButton("Add Disk");
        newBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        vdi.addDiskPanel();
                    }
                });
                t.start();
            }
        });
        return newBtn;
    }

    /** Returns the regexp of the parameter. */
    @Override
    protected String getParamRegexp(final String param) {
        if ("file".equals(getComboBoxValue(DiskData.TYPE))
            && DiskData.SOURCE_FILE.equals(param)) {
            return ".*[^/]$";
        }
        return super.getParamRegexp(param);
    }
}
