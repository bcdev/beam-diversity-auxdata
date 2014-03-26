package org.esa.beam.ui;

import org.esa.beam.framework.gpf.ui.DefaultSingleTargetProductDialog;
import org.esa.beam.framework.ui.command.CommandEvent;
import org.esa.beam.operator.MasterOp;
import org.esa.beam.visat.actions.AbstractVisatAction;

/**
 * Diversity auxdata Action class
 *
 * @author Olaf Danne
 */
public class DiversityAuxdataAction extends AbstractVisatAction {

    @Override
    public void actionPerformed(CommandEvent event) {
        final String version = MasterOp.VERSION;
        final String helpId = event.getCommand().getHelpId();
        final DefaultSingleTargetProductDialog productDialog = new DefaultSingleTargetProductDialog(
                "Diversity.Auxdata", getAppContext(),
                "Diversity Auxdata Preparation - v" + version, helpId);
        productDialog.setTargetProductNameSuffix("_AUX");
        productDialog.show();
    }

}
