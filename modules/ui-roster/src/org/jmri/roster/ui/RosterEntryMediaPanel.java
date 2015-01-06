/*============================================================================*
 * WARNING      This class contains automatically modified code.      WARNING *
 *                                                                            *
 * The method initComponents() and the variable declarations between the      *
 * "// Variables declaration - do not modify" and                             *
 * "// End of variables declaration" comments will be overwritten if modified *
 * by hand. Using the NetBeans IDE to edit this file is strongly recommended. *
 *                                                                            *
 * See http://jmri.org/help/en/html/doc/Technical/NetBeansGUIEditor.shtml for *
 * more information.                                                          *
 *============================================================================*/
package org.jmri.roster.ui;

import java.io.Serializable;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterMediaPane;
import jmri.jmrit.roster.swing.RosterEntryNetBeansGlue;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood <randall.h.wood@alexandriasoftware.com>
 */
@MultiViewElement.Registration(
        displayName = "#LBL_RosterEntryMediaPanel",
        mimeType = "application/x-jmri-rosterentry-node",
        iconBase = "org/jmri/roster/ui/RosterEntry.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "RosterEntryMedia",
        position = 10)
@Messages("LBL_RosterEntryMediaPanel=Media")
//@ConvertAsProperties(dtd = "-//org.jmri.roster.ui//RosterEntryMediaPanel//EN", autostore = false)
public class RosterEntryMediaPanel extends TopComponent implements MultiViewElement, RosterEntryNetBeansGlue, Serializable {

    private static final Logger log = LoggerFactory.getLogger(RosterEntryMediaPanel.class);
    private static final long serialVersionUID = -2978649987411993013L;
    private final InstanceContent instanceContent;
    private final RosterEntry rosterEntry;
    private final Lookup lookup;

    /**
     * Creates new form RosterEntryMediaPanel
     */
    public RosterEntryMediaPanel(Lookup lookup) {
        this.lookup = lookup;
        this.rosterEntry = lookup.lookup(RosterEntry.class);
        this.instanceContent = lookup.lookup(InstanceContent.class);
        if (this.rosterEntry == null) {
            log.error("RosterEntry is null.");
        }
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new JScrollPane();
        rosterMediaPane = new RosterMediaPane(this.rosterEntry, this);

        jScrollPane1.setViewportView(rosterMediaPane);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane jScrollPane1;
    private RosterMediaPane rosterMediaPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return this.lookup;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback mvec) {
        if (this.rosterMediaPane.guiChanged(this.getRosterEntry())) {
            mvec.getTopComponent().setHtmlDisplayName("<html><b>" + this.getRosterEntry().getDisplayName() + "</b></html>");
        } else {
            mvec.getTopComponent().setHtmlDisplayName("<html>" + this.getRosterEntry().getDisplayName() + "</html>");
        }
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public RosterEntry getRosterEntry() {
        return this.rosterEntry;
    }

    @Override
    public InstanceContent getInstanceContent() {
        return this.instanceContent;
    }

    @Override
    public boolean save() {
        if (this.rosterMediaPane.guiChanged(rosterEntry)) {
            this.rosterMediaPane.update(this.rosterEntry);
            return true;
        }
        return false;
    }

    @Override
    public void savable() {
        this.getInstanceContent().add(new RosterEntrySavable(this));
    }
}