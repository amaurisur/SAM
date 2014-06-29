package org.basex.gui;

import javax.swing.*;

import org.basex.gui.layout.*;

/**
 * This is the toolbar of the main window.
 * The toolbar contents are defined in {@link GUIConstants#TOOLBAR}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class GUIToolBar extends JToolBar {
  /** Toolbar commands. */
  private final GUICommand[] cmd;
  /** Reference to main window. */
  private final GUI gui;

  /**
   * Default constructor.
   * @param tb toolbar commands
   * @param main reference to the main window
   */
  GUIToolBar(final GUICommand[] tb, final GUI main) {
    setFloatable(false);
    cmd = tb;
    gui = main;

    for(final GUICommand c : cmd) {
      if(c == null) {
        addSeparator();
      } else {
        final AbstractButton button = BaseXButton.command(c, gui);
        button.setFocusable(false);
        add(button);
      }
    }
  }

  /**
   * Refreshes the buttons.
   */
  void refresh() {
    for(int b = 0; b < cmd.length; ++b) {
      if(cmd[b] != null) {
        final AbstractButton button = (AbstractButton) getComponent(b);
        button.setEnabled(cmd[b].enabled(gui));
        button.setSelected(cmd[b].selected(gui));
      }
    }
  }
}
