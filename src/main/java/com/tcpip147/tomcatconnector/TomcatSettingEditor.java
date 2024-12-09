package com.tcpip147.tomcatconnector;

import com.intellij.application.options.ModulesComboBox;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class TomcatSettingEditor extends SettingsEditor<TomcatConfiguration> {

    private final TomcatConfiguration configuration;
    private final JPanel editor;
    private final TextFieldWithBrowseButton tfCatalinaHome;
    private final TextFieldWithBrowseButton tfCatalinaBase;
    private final TextFieldWithBrowseButton tfDocBase;
    private final RawCommandLineEditor tfVmOptions;
    private final ModulesComboBox cbModules;
    private final JPanel pModules;
    private final JPanel pDeploymentAssembly;
    private final JBTable tbDeploymentAssembly;
    private final DefaultTableModel tmDeploymentAssembly;

    public TomcatSettingEditor(@NotNull TomcatConfiguration configuration) {
        this.configuration = configuration;
        tfCatalinaHome = createDirectorySelector("Select Catalina Home");
        tfCatalinaBase = createDirectorySelector("Select Catalina Base");
        tfDocBase = createDirectorySelector("Select Document Base");
        tfVmOptions = new RawCommandLineEditor();
        cbModules = createModulesComboBox();
        pModules = createModulesPanel();
        pDeploymentAssembly = createDeploymentAssemblyPanel();
        tbDeploymentAssembly = createDeploymentAssemblyTable(pDeploymentAssembly);
        tmDeploymentAssembly = (DefaultTableModel) tbDeploymentAssembly.getModel();

        editor = FormBuilder.createFormBuilder()
                .addLabeledComponent("Catalina Home:", tfCatalinaHome)
                .addLabeledComponent("Catalina Base:", tfCatalinaBase)
                .addLabeledComponent("Document Base:", tfDocBase)
                .addLabeledComponent("Vm Options:", tfVmOptions)
                .addLabeledComponent("Module:", pModules).addComponent(new JBLabel("Deployment Assembly:")).addComponent(pDeploymentAssembly).getPanel();
    }

    private TextFieldWithBrowseButton createDirectorySelector(String label) {
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(label, null, null, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        return textField;
    }

    private JPanel createModulesPanel() {
        JPanel pModules = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        pModules.add(cbModules, c);
        return pModules;
    }

    private ModulesComboBox createModulesComboBox() {
        ModulesComboBox cbModules = new ModulesComboBox();
        Module[] modules = ModuleManager.getInstance(configuration.getProject()).getModules();
        cbModules.setModules(Arrays.stream(modules).toList());
        return cbModules;
    }

    private JPanel createDeploymentAssemblyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultActionGroup group = new DefaultActionGroup();
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("DeploymentAssemblyActionToolbar", group, true);
        actionToolbar.getComponent().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(30, 31, 34)));
        group.add(new AnAction("Add", "Add", AllIcons.General.Add) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                tmDeploymentAssembly.addRow(new Vector<>());
            }
        });
        group.add(new AnAction("Delete", "Delete", AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                int[] indexes = tbDeploymentAssembly.getSelectedRows();
                for (int i = indexes.length - 1; i >= 0; i--) {
                    tmDeploymentAssembly.removeRow(indexes[i]);
                }
                int index = tbDeploymentAssembly.getRowCount();
                if (index > 0) {
                    tbDeploymentAssembly.setRowSelectionInterval(index - 1, index - 1);
                }
            }
        });
        actionToolbar.setTargetComponent(panel);
        panel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
        return panel;
    }

    private JBTable createDeploymentAssemblyTable(JPanel parent) {
        JBTable table = new JBTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.addColumn("Source");
        model.addColumn("Target");
        JBScrollPane panel = new JBScrollPane(table);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(30, 31, 34)));
        panel.setPreferredSize(new Dimension(panel.getWidth(), 130));
        parent.add(panel, BorderLayout.CENTER);
        return table;
    }

    @Override
    protected void resetEditorFrom(@NotNull TomcatConfiguration configuration) {
        cbModules.setSelectedModule(configuration.getConfigurationModule().getModule());
        tfCatalinaHome.setText(configuration.getOptions().getCatalinaHome());
        tfCatalinaBase.setText(configuration.getOptions().getCatalinaBase());
        tfDocBase.setText(configuration.getOptions().getDocBase());
        tfVmOptions.setText(configuration.getOptions().getVmOptions());
        setDeploymentAssemblyData(configuration.getOptions().getDeploymentAssembly());
    }

    @Override
    protected void applyEditorTo(@NotNull TomcatConfiguration configuration) throws ConfigurationException {
        configuration.getConfigurationModule().setModule(cbModules.getSelectedModule());
        configuration.getOptions().setCatalinaHome(tfCatalinaHome.getText());
        configuration.getOptions().setCatalinaBase(tfCatalinaBase.getText());
        configuration.getOptions().setDocBase(tfDocBase.getText());
        configuration.getOptions().setVmOptions(tfVmOptions.getText());
        configuration.getOptions().setDeploymentAssembly(getDeploymentAssemblyData());
    }

    private Map<String, String> getDeploymentAssemblyData() {
        Map<String, String> data = new LinkedHashMap<>();
        int count = tmDeploymentAssembly.getRowCount();
        for (int i = 0; i < count; i++) {
            String source = (String) tmDeploymentAssembly.getValueAt(i, 0);
            String target = (String) tmDeploymentAssembly.getValueAt(i, 1);
            data.put(source, target);
        }
        return data;
    }

    private void setDeploymentAssemblyData(Map<String, String> data) {
        tmDeploymentAssembly.setRowCount(0);
        for (Object key : data.keySet()) {
            tmDeploymentAssembly.addRow(new Object[]{key, data.get(key)});
        }
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return editor;
    }
}
