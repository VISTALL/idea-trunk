package org.jetbrains.plugins.groovy.mvc;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.refactoring.GroovyNamesUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MvcRunTargetDialog extends DialogWrapper {

  @NonNls private static final String POSSIBLE_TARGETS = "Possible Targets";

  private JPanel contentPane;
  private JLabel myTargetLabel;
  @SuppressWarnings({"UnusedDeclaration"}) private JPanel myFakePanel;
  private TextFieldWithAutoCompletion myTargetField;
  private final Module myModule;
  private final MvcFramework myFramework;
  @NonNls private static final String SCRIPTS_FOLDER = "/scripts";

  public MvcRunTargetDialog(@NotNull Module module, MvcFramework framework) {
    super(module.getProject(), true);
    myModule = module;
    myFramework = framework;
    setTitle("Run " + framework.getDisplayName() + " target");
    setUpDialog();
    setModal(true);
    init();
  }

  private void setUpDialog() {
    myTargetLabel.setLabelFor(myTargetField);
    myTargetField.setFocusable(true);
    myTargetField.setVariants(ContainerUtil.map(getVariants(), new Function<String, LookupElement>() {
      public LookupElement fun(final String s) {
        return LookupElementBuilder.create(s);
      }
    }));
  }

  private void showVariants() {
    String[] targetNames = getVariants();
    if (targetNames.length > 0) {
      showCompletionPopup(new JList(targetNames), POSSIBLE_TARGETS);
    }
  }

  private String[] getVariants() {
    String[] args = getTargetArguments();
    String[] targetNames = getAllTargetNames(myModule);
    if (args.length == 1 && args[0] != null) {
      final String prefix = args[0];
      List<String> stringList = ContainerUtil.findAll(targetNames, new Condition<String>() {
        public boolean value(final String s) {
          String trimmed = s.trim();
          return trimmed.startsWith(prefix) && !(trimmed.equals(prefix));
        }
      });
      targetNames = stringList.toArray(new String[stringList.size()]);
    }
    return targetNames;
  }

  String[] getTargetArguments() {
    String text = myTargetField.getText();
    Iterable<String> iterable = StringUtil.tokenize(text, " ");
    ArrayList<String> args = new ArrayList<String>();
    for (String s : iterable) {
      args.add(s);
    }
    return args.toArray(new String[args.size()]);
  }

  protected JComponent createCenterPanel() {
    return contentPane;
  }

  public JComponent getPreferredFocusedComponent() {
    return myTargetField;
  }

  private void showCompletionPopup(final JList list, String title) {

    final Runnable callback = new Runnable() {
      public void run() {
        String selectedValue = (String)list.getSelectedValue();
        if (selectedValue != null) {
          myTargetField.setText(selectedValue);
        }
      }
    };

    final PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
    if (title != null) {
      builder.setTitle(title);
    }

    final JBPopup popup = builder.setMovable(false).setResizable(false).setRequestFocus(true).
      setItemChoosenCallback(callback).createPopup();
    final JComponent component = popup.getContent();
    if (component != null) {

      final ActionListener listener = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          if (getTargetArguments().length <= 1) {
            Disposer.dispose(popup);
            final String text = myTargetField.getText();
            if (text.length() > 0) {
              myTargetField.setText(text.substring(0, text.length() - 1));
            }
            if (text.length() > 1) {
              showVariants();
            }
          }
        }
      };
      component
        .registerKeyboardAction(listener, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    popup.showUnderneathOf(myTargetField);
  }

  private void createUIComponents() {
    myTargetField = new TextFieldWithAutoCompletion(myModule.getProject());
    myFakePanel = myTargetField;
    myTargetField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      public void documentChanged(DocumentEvent e) {
        setOKActionEnabled(StringUtil.isNotEmpty(e.getDocument().getText()));
      }
    });
    setOKActionEnabled(false);
  }

  private String[] getAllTargetNames(@NotNull Module module) {
    final List<String> result = new ArrayList<String>();
    final VirtualFile sdkRoot = myFramework.getSdkRoot(module);
    if (sdkRoot != null) {
      addTargets(result, sdkRoot);
    }
    final VirtualFile root = myFramework.findAppRoot(module);
    if (root != null) {
      addTargets(result, root);
    }

    for (VirtualFile pluginRoot : myFramework.getPluginRoots(module)) {
      addTargets(result, pluginRoot);
    }

    Collections.sort(result, new Comparator<String>() {
      public int compare(final String o1, final String o2) {
        return o1.compareToIgnoreCase(o2);
      }
    });

    return result.toArray(new String[result.size()]);
  }

  private static void addTargets(final List<String> result, @Nullable final VirtualFile root) {
    if (root == null || !root.isDirectory()) {
      return;
    }

    final VirtualFile scripts = root.findChild(SCRIPTS_FOLDER);

    if (scripts == null || !scripts.isDirectory()) {
      return;
    }

    for (VirtualFile child : scripts.getChildren()) {
      if (!child.isDirectory() && GroovyFileType.DEFAULT_EXTENSION.equals(child.getExtension()) && !child.getName().startsWith("_")) {
        result.add(GroovyNamesUtil.camelToSnake(child.getNameWithoutExtension()));
      }
    }
  }

}
