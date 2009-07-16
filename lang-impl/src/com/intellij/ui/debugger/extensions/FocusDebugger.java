package com.intellij.ui.debugger.extensions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.debugger.UiDebuggerExtension;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

public class FocusDebugger implements UiDebuggerExtension, PropertyChangeListener, ListSelectionListener  {

  private static final Logger LOG = Logger.getInstance("#com.intellij.ui.debugger.extensions.FocusDebugger");

  private JComponent myComponent;

  private JList myLog;
  private DefaultListModel myLogModel;
  private JEditorPane myAllocation;

  public JComponent getComponent() {
    if (myComponent == null) {
      myComponent = init();
    }

    return myComponent;
  }

  private JComponent init() {
    final JPanel result = new JPanel(new BorderLayout());

    myLogModel = new DefaultListModel();
    myLog = new JList(myLogModel);
    myLog.setCellRenderer(new FocusElementRenderer());


    myAllocation = new JEditorPane();
    final DefaultCaret caret = new DefaultCaret();
    myAllocation.setCaret(caret);
    caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    myAllocation.setEditable(false);


    final Splitter splitter = new Splitter(true);
    splitter.setFirstComponent(new JScrollPane(myLog));
    splitter.setSecondComponent(new JScrollPane(myAllocation));

    myLog.addListSelectionListener(this);

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(this);

    result.add(splitter, BorderLayout.CENTER);


    final DefaultActionGroup group = new DefaultActionGroup();
    group.add(new ClearAction());

    result.add(ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, group, true).getComponent(), BorderLayout.NORTH);

    return result;
  }

  class ClearAction extends AnAction {
    ClearAction() {
      super("Clear", "", IconLoader.getIcon("/actions/cross.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      myLogModel.clear();
    }
  }

  public void valueChanged(ListSelectionEvent e) {
    if (myLog.getSelectedIndex() == -1) {
      myAllocation.setText(null);
    } else {
      FocusElement element = (FocusElement)myLog.getSelectedValue();
      final StringWriter s = new StringWriter();
      final PrintWriter writer = new PrintWriter(s);
      element.getAllocation().printStackTrace(writer);
      myAllocation.setText(s.toString());
    }
  }

  private boolean isInsideDebuggerDialog(Component c) {
    final Window debuggerWindow = SwingUtilities.getWindowAncestor(myComponent);
    if (!(debuggerWindow instanceof Dialog)) return false;

    return c == debuggerWindow || SwingUtilities.getWindowAncestor(c) == debuggerWindow;
  }

  public void propertyChange(PropertyChangeEvent evt) {
    final Object newValue = evt.getNewValue();
    final Object oldValue = evt.getOldValue();

    boolean affectsDebugger = false;

    if (newValue instanceof Component && isInsideDebuggerDialog((Component)newValue)) {
      affectsDebugger |= true;
    }

    if (oldValue instanceof Component && isInsideDebuggerDialog((Component)oldValue)) {
      affectsDebugger |= true;
    }



    final SimpleColoredText text = new SimpleColoredText();
    text.append(evt.getPropertyName(), maybeGrayOut(new SimpleTextAttributes(SimpleTextAttributes.STYLE_UNDERLINE, null), affectsDebugger));
    text.append(" newValue=", maybeGrayOut(SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES, affectsDebugger));
    text.append(evt.getNewValue() + "", maybeGrayOut(SimpleTextAttributes.REGULAR_ATTRIBUTES, affectsDebugger));
    text.append(" oldValue=" + evt.getOldValue(), maybeGrayOut(SimpleTextAttributes.REGULAR_ATTRIBUTES, affectsDebugger));


    myLogModel.addElement(new FocusElement(text, new Throwable()));
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (myLog != null && myLog.isShowing()) {
          final int h = myLog.getFixedCellHeight();
          myLog.scrollRectToVisible(new Rectangle(0, myLog.getPreferredSize().height - h, myLog.getWidth(), h));
          if (myLog.getModel().getSize() > 0) {
            myLog.setSelectedIndex(myLog.getModel().getSize() - 1);
          }
        }
      }
    });
  }

  private SimpleTextAttributes maybeGrayOut(SimpleTextAttributes attr, boolean greyOut) {
    return greyOut ? attr.derive(attr.getStyle(), Color.gray, attr.getBgColor(), attr.getWaveColor()) : attr;
  }

  class FocusElementRenderer extends ColoredListCellRenderer {
    @Override
    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
      clear();
      final FocusElement element = (FocusElement)value;
      final SimpleColoredText text = element.getText();
      final ArrayList<String> strings = text.getTexts();
      final ArrayList<SimpleTextAttributes> attributes = element.getText().getAttributes();
      for (int i = 0; i < strings.size(); i++) {
        append(strings.get(i), attributes.get(i));
      }
    }
  }

  class FocusElement {
    private SimpleColoredText myText;
    private Throwable myAllocation;

    FocusElement(SimpleColoredText text, Throwable allocation) {
      myText = text;
      myAllocation = allocation;
    }

    public SimpleColoredText getText() {
      return myText;
    }

    public Throwable getAllocation() {
      return myAllocation;
    }
  }


  public String getName() {
    return "Focus";
  }

  public void disposeUiResources() {
    myComponent = null;
    KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(this);
  }
}