package com.tcpip147.tomcatconnector;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class TomcatSettingEditor extends SettingsEditor<TomcatConfiguration> {

    private final JPanel editor;
    private final TextFieldWithBrowseButton tfCatalinaHome;
    private final TextFieldWithBrowseButton tfCatalinaBase;
    private final RawCommandLineEditor tfVmOptions;
    private final JBTable tbDeploymentAssembly;
    private final DefaultTableModel tmDeploymentAssembly;

    public TomcatSettingEditor(@NotNull TomcatConfiguration configuration) {
        tfCatalinaHome = createDirectorySelector("Select Catalina Home");
        tfCatalinaBase = createDirectorySelector("Select Catalina Base");
        tfVmOptions = new RawCommandLineEditor();
        JPanel pDeploymentAssembly = createDeploymentAssemblyPanel();
        tbDeploymentAssembly = createDeploymentAssemblyTable(pDeploymentAssembly);
        tmDeploymentAssembly = (DefaultTableModel) tbDeploymentAssembly.getModel();

        editor = FormBuilder.createFormBuilder()
                .addLabeledComponent("Catalina home:", tfCatalinaHome)
                .addLabeledComponent("Catalina base:", tfCatalinaBase)
                .addLabeledComponent("Vm options:", tfVmOptions)
                .addComponent(new JBLabel("Deployment assembly:")).addComponent(pDeploymentAssembly).getPanel();
    }

    private TextFieldWithBrowseButton createDirectorySelector(String label) {
        TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
        textField.addBrowseFolderListener(label, null, null, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        return textField;
    }

    private JPanel createDeploymentAssemblyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultActionGroup group = new DefaultActionGroup();
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("DeploymentAssemblyActionToolbar", group, true);
        actionToolbar.getComponent().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new JBColor(new Color(30, 31, 34), new Color(30, 31, 34))));
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
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new JBColor(new Color(30, 31, 34), new Color(30, 31, 34))));
        panel.setPreferredSize(new Dimension(panel.getWidth(), 200));
        parent.add(panel, BorderLayout.CENTER);
        return table;
    }

    @Override
    protected void resetEditorFrom(@NotNull TomcatConfiguration configuration) {
        tfCatalinaHome.setText(configuration.getOptions().getCatalinaHome());
        tfCatalinaBase.setText(configuration.getOptions().getCatalinaBase());
        tfVmOptions.setText(configuration.getOptions().getVmOptions());
        setDeploymentAssemblyData(configuration.getOptions().getDeploymentAssembly());
    }

    @Override
    protected void applyEditorTo(@NotNull TomcatConfiguration configuration) throws ConfigurationException {
        configuration.getOptions().setCatalinaHome(tfCatalinaHome.getText());
        configuration.getOptions().setCatalinaBase(tfCatalinaBase.getText());
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
        for (String key : data.keySet()) {
            tmDeploymentAssembly.addRow(new Object[]{key, data.get(key)});
        }
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return editor;
    }
}
