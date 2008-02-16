/*******************************************************************************
 * Copyright (c) 2008 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightException;
import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.CopyrightSettings;

public class ApplyCopyrightWizard extends Wizard {
  protected ProjectSelectionWizardPage projectsPage;
  protected CopyrightSettingsPage settingsPage;
  protected ResourcesSelectionPage selectionPage;
  protected CopyrightSettings settings;

  public ApplyCopyrightWizard() {
    setWindowTitle(Messages.ApplyCopyrightWizard_title);
  }

  @Override
  public void addPages() {
    projectsPage = new ProjectSelectionWizardPage();
    addPage(projectsPage);

    settingsPage = new CopyrightSettingsPage();
    addPage(settingsPage);

    selectionPage = new ResourcesSelectionPage();
    addPage(selectionPage);

    IPageChangingListener listener = new IPageChangingListener() {
      public void handlePageChanging(PageChangingEvent event) {
        if ( event.getTargetPage() == selectionPage && settings.isChanged() ) {
          computeSelection();
        }
      }
    };
    ((WizardDialog) getContainer()).addPageChangingListener(listener);

    projectsPage.init(settings);
    settingsPage.init(settings);
  }

  private void computeSelection() {
    try {
      selectionPage.setSelection(CopyrightManager.selectResources(settings));
      settings.setChanged(false);
    } catch (CopyrightException e) {
      MessageDialog.openError(getShell(), Messages.ApplyCopyrightWizard_error, e.getMessage());
    }
  }

  public void init(IProject[] projects) {
    settings = new CopyrightSettings();
    settings.setProjects(projects);
  }

  public static void openWizard(Shell shell, IProject[] projects) {
    ApplyCopyrightWizard wizard = new ApplyCopyrightWizard();
    wizard.init(projects);
    WizardDialog dialog = new WizardDialog(shell, wizard);
    dialog.open();
  }

  @Override
  public boolean performFinish() {
    selectionPage.getSelection(settings);
    try {
      CopyrightManager.applyCopyright(settings);
    } catch (CopyrightException e) {
      MessageDialog.openError(getShell(), Messages.ApplyCopyrightWizard_error,
                              e.getMessage() + " - " + e.getCause().getMessage()); //$NON-NLS-1$
      return false;
    }
    return true;
  }
}