package org.basex.gui;

import org.basex.gui.layout.*;

/**
 * This class provides a default implementation for GUI popup commands.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class GUIPopupCmd implements GUICommand {
  /** Shortcut. */
  private final BaseXKeys[] shortcut;
  /** Label. */
  private final String label;

  /**
   * Constructor.
   * @param lbl label
   * @param sc shortcut
   */
  protected GUIPopupCmd(final String lbl, final BaseXKeys... sc) {
    label = lbl;
    shortcut = sc;
  }

  /**
   * Executes the popup command.
   */
  public abstract void execute();

  @Override
  public final void execute(final GUI main) {
    if(enabled(main)) execute();
  }

  @Override
  public final boolean toggle() {
    return false;
  }

  @Override
  public final String label() {
    return label;
  }

  @Override
  public final String shortcut() {
    return null;
  }

  @Override
  public final BaseXKeys[] shortcuts() {
    return shortcut;
  }

  @Override
  public boolean enabled(final GUI main) {
    return true;
  }

  @Override
  public boolean selected(final GUI main) {
    return false;
  }
}
