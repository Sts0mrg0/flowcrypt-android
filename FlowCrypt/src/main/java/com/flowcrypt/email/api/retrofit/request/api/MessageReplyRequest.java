/*
 * © 2016-2018 FlowCrypt Limited. Limitations apply. Contact human@flowcrypt.com
 * Contributors: DenBond7
 */

package com.flowcrypt.email.api.retrofit.request.api;

import com.flowcrypt.email.api.retrofit.ApiName;
import com.flowcrypt.email.api.retrofit.request.BaseRequest;
import com.flowcrypt.email.api.retrofit.request.model.MessageReplyModel;

/**
 * This class describes a request to the https://flowcrypt.com/api/message/reply API.
 * <p>
 * <code>POST /message/reply  {
 * "short" (<type 'str'>)  # original message short id
 * "token" (<type 'str'>)  # message token
 * "message" (<type 'str'>)  # encrypted message
 * "subject" (<type 'str'>)  # subject of sent email
 * "from" (<type 'str'>)  # sender (user of the web app)
 * "to" (<type 'str'>)  # recipient (CryptUp user and the sender of the original message)
 * }</code>
 *
 * @author Denis Bondarenko
 * Date: 13.07.2017
 * Time: 16:32
 * E-mail: DenBond7@gmail.com
 */

public class MessageReplyRequest extends BaseRequest<MessageReplyModel> {
  public MessageReplyRequest(MessageReplyModel requestModel) {
    super(ApiName.POST_MESSAGE_REPLY, requestModel);
  }
}
