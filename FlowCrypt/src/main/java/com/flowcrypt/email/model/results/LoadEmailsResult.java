/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.model.results;

import com.flowcrypt.email.api.email.model.GeneralMessageDetails;

import java.util.List;

/**
 * This class describe a model object, which contains information about an end position of
 * already loaded emails and last loaded emails;
 *
 * @author DenBond7
 * Date: 22.05.2017
 * Time: 15:59
 * E-mail: DenBond7@gmail.com
 */

public class LoadEmailsResult {
  private int endPositionOfLoadedEmails;
  private List<GeneralMessageDetails> generalMessageDetailsList;

  public LoadEmailsResult(int endPositionOfLoadedEmails, List<GeneralMessageDetails>
      generalMessageDetailsList) {
    this.endPositionOfLoadedEmails = endPositionOfLoadedEmails;
    this.generalMessageDetailsList = generalMessageDetailsList;
  }

  /**
   * Get information about an end position of
   * already loaded emails.
   *
   * @return <tt>int</tt> An end position.
   */
  public int getEndPositionOfLoadedEmails() {
    return endPositionOfLoadedEmails;
  }

  /**
   * Get last loaded emails.
   *
   * @return <tt>List<GeneralMessageDetails></tt> Last loaded emails
   */
  public List<GeneralMessageDetails> getGeneralMessageDetailsList() {
    return generalMessageDetailsList;
  }
}
